package de.bixilon.minosoft.gui.rendering.shader

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBFragmentShader.GL_FRAGMENT_SHADER_ARB
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.ARBVertexShader.GL_VERTEX_SHADER_ARB
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil

class Shader(private val vertexPath: String, private val fragmentPath: String) {
    private var programId = 0

    fun load(): Int {
        val vertexShader = ShaderUtil.createShader(vertexPath, GL_VERTEX_SHADER_ARB)
        val fragmentShader = ShaderUtil.createShader(fragmentPath, GL_FRAGMENT_SHADER_ARB)
        programId = glCreateProgramObjectARB()

        if (programId.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glAttachObjectARB(programId, vertexShader)
        glAttachObjectARB(programId, fragmentShader)
        glLinkProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(programId))
        }

        glValidateProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_VALIDATE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(programId))
        }
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        return programId
    }

    fun use(): Shader {
        if (usedShader !== this) {
            glUseProgram(programId)
            usedShader = this
        }
        return this
    }

    private fun getUniformLocation(variableName: String): Int {
        return glGetUniformLocation(programId, variableName)
    }

    fun setFloat(uniformName: String, value: Float) {
        glUniform1f(getUniformLocation(uniformName), value)
    }

    fun setInt(uniformName: String, value: Int) {
        glUniform1i(getUniformLocation(uniformName), value)
    }

    fun set4f(uniformName: String, floats: FloatArray) {
        glUniform4f(getUniformLocation(uniformName), floats[0], floats[1], floats[2], floats[3])
    }

    fun setMat4(uniformName: String, mat4: Mat4) {
        glUniformMatrix4fv(getUniformLocation(uniformName), false, mat4 to BufferUtils.createFloatBuffer(16))
    }

    fun setVec3(uniformName: String, vec3: Vec3) {
        glUniform3f(getUniformLocation(uniformName), vec3.x, vec3.y, vec3.z)
    }

    fun setArray(uniformName: String, array: Array<*>) {
        for ((i, value) in array.withIndex()) {
            val currentUniformName = "$uniformName[$i]"
            val currentUniformLocation = getUniformLocation(currentUniformName)
            when (value) {
                is Array<*> -> setArray(currentUniformName, value)
                is Int -> glUniform1i(currentUniformLocation, value)
                is Float -> glUniform1f(currentUniformLocation, value)
                is Mat4 -> glUniformMatrix4fv(currentUniformLocation, false, value to BufferUtils.createFloatBuffer(16))
                is Vec3 -> glUniform3f(currentUniformLocation, value.x, value.y, value.z)
                is Vec2 -> glUniform2f(currentUniformLocation, value.x, value.y)
            }
        }
    }


    companion object {
        private var usedShader: Shader? = null
    }
}
