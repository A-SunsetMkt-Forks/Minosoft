/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.Colors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.FloatOpenGLUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.IntOpenGLUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.FloatOpenGLVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureManager
import de.bixilon.minosoft.gui.rendering.system.opengl.vendor.*
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.*
import java.nio.ByteBuffer

class OpenGLRenderSystem(
    private val renderWindow: RenderWindow,
) : RenderSystem {
    override val shaders: MutableSet<Shader> = mutableSetOf()
    private val capabilities: MutableSet<RenderingCapabilities> = synchronizedSetOf()
    override lateinit var vendor: OpenGLVendor
        private set

    var blendingSource = BlendingFunctions.ONE
        private set
    var blendingDestination = BlendingFunctions.ZERO
        private set

    override var shader: Shader? = null
        set(value) {
            if (value === field) {
                return
            }
            value ?: error("Shader is null!")

            check(value is OpenGLShader) { "Can not use non OpenGL shader in OpenGL render system!" }
            check(value.loaded) { "Shader not loaded!" }
            check(value in shaders) { "Shader not part of this context!" }

            value.unsafeUse()

            field = value
        }


    override fun init() {
        GL.createCapabilities()

        this.vendorString = glGetString(GL_VENDOR) ?: "UNKNOWN"
        val vendorString = vendorString.lowercase()

        vendor = when {
            vendorString.contains("nvidia") -> NvidiaOpenGLVendor
            vendorString.contains("intel") -> MesaOpenGLVendor
            vendorString.contains("amd") || vendorString.contains("ati") -> ATIOpenGLVendor // ToDo
            else -> OtherOpenGLVendor
        }

        this.version = glGetString(GL_VERSION) ?: "UNKNOWN"
        this.gpuType = glGetString(GL_RENDERER) ?: "UNKNOWN"

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            renderWindow.queue += {
                glViewport(0, 0, it.size.x, it.size.y)
            }
        })
    }

    override fun enable(capability: RenderingCapabilities) {
        this[capability] = true
    }

    override fun disable(capability: RenderingCapabilities) {
        this[capability] = false
    }

    override fun set(capability: RenderingCapabilities, status: Boolean) {
        val enabled = capabilities.contains(capability)
        if ((enabled && status) || (!status && !enabled)) {
            return
        }

        val glCapability = capability.gl

        if (status) {
            glEnable(glCapability)
            capabilities += capability
        } else {
            glDisable(glCapability)
            capabilities -= capability
        }
    }

    override fun get(capability: RenderingCapabilities): Boolean {
        return capabilities.contains(capability)
    }

    override fun set(source: BlendingFunctions, destination: BlendingFunctions) {
        if (blendingDestination == destination && blendingSource == source) {
            return
        }
        blendingSource = source
        blendingDestination = destination
        glBlendFunc(source.gl, destination.gl)
    }

    override fun setBlendFunc(sourceRGB: BlendingFunctions, destinationRGB: BlendingFunctions, sourceAlphaFactor: BlendingFunctions, destinationAlphaFactor: BlendingFunctions) {
        glBlendFuncSeparate(sourceRGB.gl, destinationRGB.gl, sourceAlphaFactor.gl, destinationAlphaFactor.gl)
    }

    override var depth: DepthFunctions = DepthFunctions.LESS
        set(value) {
            if (field == value) {
                return
            }
            glDepthFunc(value.gl)
            field = value
        }

    override var depthMask: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            glDepthMask(value)
            field = value
        }

    override var polygonMode: PolygonModes = PolygonModes.FILL
        set(value) {
            if (field == value) {
                return
            }
            glPolygonMode(FaceTypes.FRONT_AND_BACK.gl, value.gl)
            field = value
        }

    override val usedVRAM: Long
        get() = vendor.usedVRAM

    override val availableVRAM: Long
        get() = vendor.availableVRAM

    override val maximumVRAM: Long
        get() = vendor.maximumVRAM

    override lateinit var vendorString: String
        private set
    override lateinit var version: String
        private set
    override lateinit var gpuType: String
        private set

    override fun readPixels(start: Vec2i, end: Vec2i, type: PixelTypes): ByteBuffer {
        val buffer: ByteBuffer = BufferUtils.createByteBuffer((end.x - start.x) * (end.y - start.y) * type.bytes)
        glReadPixels(start.x, start.y, end.x, end.y, type.gl, GL_UNSIGNED_BYTE, buffer)
        return buffer
    }

    override fun createShader(resourceLocation: ResourceLocation): OpenGLShader {
        return OpenGLShader(renderWindow, resourceLocation)
    }

    override fun createVertexBuffer(structure: MeshStruct, data: FloatArray, primitiveType: PrimitiveTypes): FloatVertexBuffer {
        return FloatOpenGLVertexBuffer(structure, data, primitiveType)
    }

    override fun createFloatUniformBuffer(bindingIndex: Int, data: FloatArray): FloatUniformBuffer {
        return FloatOpenGLUniformBuffer(bindingIndex, data)
    }

    override fun createIntUniformBuffer(bindingIndex: Int, data: IntArray): IntUniformBuffer {
        return IntOpenGLUniformBuffer(bindingIndex, data)
    }

    override fun createTextureManager(): TextureManager {
        return OpenGLTextureManager(renderWindow)
    }

    override var clearColor: RGBColor = Colors.TRUE_BLACK
        set(value) {
            if (value == field) {
                return
            }
            glClearColor(clearColor.floatRed, clearColor.floatGreen, clearColor.floatBlue, clearColor.floatAlpha)

            field = value
        }

    override fun clear(vararg buffers: IntegratedBufferTypes) {
        var bits = 0
        for (buffer in buffers) {
            bits = bits or buffer.gl
        }
        glClear(bits)
    }

    companion object {
        private val RenderingCapabilities.gl: Int
            get() {
                return when (this) {
                    RenderingCapabilities.BLENDING -> GL_BLEND
                    RenderingCapabilities.DEPTH_TEST -> GL_DEPTH_TEST
                    RenderingCapabilities.FACE_CULLING -> GL_CULL_FACE
                    else -> throw IllegalArgumentException("OpenGL does not support capability: $this")
                }
            }

        private val BlendingFunctions.gl: Int
            get() {
                return when (this) {
                    BlendingFunctions.ZERO -> GL_ZERO
                    BlendingFunctions.ONE -> GL_ONE
                    BlendingFunctions.SOURCE_COLOR -> GL_SRC_COLOR
                    BlendingFunctions.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                    BlendingFunctions.DESTINATION_COLOR -> GL_DST_COLOR
                    BlendingFunctions.ONE_MINUS_DESTINATION_COLOR -> GL_ONE_MINUS_DST_COLOR
                    BlendingFunctions.SOURCE_ALPHA -> GL_SRC_ALPHA
                    BlendingFunctions.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                    BlendingFunctions.DESTINATION_ALPHA -> GL_DST_ALPHA
                    BlendingFunctions.ONE_MINUS_DESTINATION_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                    BlendingFunctions.CONSTANT_COLOR -> GL_CONSTANT_COLOR
                    BlendingFunctions.ONE_MINUS_CONSTANT_COLOR -> GL_ONE_MINUS_CONSTANT_COLOR
                    BlendingFunctions.CONSTANT_ALPHA -> GL_CONSTANT_ALPHA
                    BlendingFunctions.ONE_MINUS_CONSTANT_ALPHA -> GL_ONE_MINUS_CONSTANT_ALPHA
                    else -> throw IllegalArgumentException("OpenGL does not support blending function: $this")
                }
            }

        private val DepthFunctions.gl: Int
            get() {
                return when (this) {
                    DepthFunctions.NEVER -> GL_NEVER
                    DepthFunctions.LESS -> GL_LESS
                    DepthFunctions.EQUAL -> GL_EQUAL
                    DepthFunctions.LESS_OR_EQUAL -> GL_LEQUAL
                    DepthFunctions.GREATER -> GL_GREATER
                    DepthFunctions.NOT_EQUAL -> GL_NOTEQUAL
                    DepthFunctions.GREATER_OR_EQUAL -> GL_GEQUAL
                    DepthFunctions.ALWAYS -> GL_ALWAYS
                    else -> throw IllegalArgumentException("OpenGL does not support depth function: $this")
                }
            }

        private val PixelTypes.gl: Int
            get() {
                return when (this) {
                    PixelTypes.RED -> GL_RED
                    PixelTypes.GREEN -> GL_GREEN
                    PixelTypes.BLUE -> GL_BLUE
                    PixelTypes.ALPHA -> GL_ALPHA
                    PixelTypes.RGB -> GL_RGB
                    PixelTypes.RGBA -> GL_RGBA
                    else -> throw IllegalArgumentException("OpenGL does not support pixel type: $this")
                }
            }

        private val PolygonModes.gl: Int
            get() {
                return when (this) {
                    PolygonModes.FILL -> GL_FILL
                    PolygonModes.LINE -> GL_LINE
                    PolygonModes.POINT -> GL_POINT
                    else -> throw IllegalArgumentException("OpenGL does not support polygon mode: $this")
                }
            }

        private val FaceTypes.gl: Int
            get() {
                return when (this) {
                    FaceTypes.FRONT_LEFT -> GL_FRONT_LEFT
                    FaceTypes.FRONT_RIGHT -> GL_FRONT_RIGHT
                    FaceTypes.BACK_LEFT -> GL_BACK_LEFT
                    FaceTypes.BACK_RIGHT -> GL_BACK_RIGHT
                    FaceTypes.FRONT -> GL_FRONT
                    FaceTypes.BACK -> GL_BACK
                    FaceTypes.LEFT -> GL_LEFT
                    FaceTypes.RIGHT -> GL_RIGHT
                    FaceTypes.FRONT_AND_BACK -> GL_FRONT_AND_BACK
                    else -> throw IllegalArgumentException("OpenGL does not support face type: $this")
                }
            }

        private val IntegratedBufferTypes.gl: Int
            get() {
                return when (this) {
                    IntegratedBufferTypes.DEPTH_BUFFER -> GL_DEPTH_BUFFER_BIT
                    IntegratedBufferTypes.ACCUM_BUFFER -> GL_ACCUM_BUFFER_BIT
                    IntegratedBufferTypes.STENCIL_BUFFER -> GL_STENCIL_BUFFER_BIT
                    IntegratedBufferTypes.COLOR_BUFFER -> GL_COLOR_BUFFER_BIT
                    else -> throw IllegalArgumentException("OpenGL does not support integrated buffer type: $this")
                }
            }
    }
}