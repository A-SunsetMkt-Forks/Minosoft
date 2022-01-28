/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.InputSpecialKey
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

open class ButtonElement(
    guiRenderer: GUIRenderer,
    text: Any,
    disabled: Boolean = false,
    var onSubmit: () -> Unit,
) : Element(guiRenderer) {
    protected val textElement = TextElement(guiRenderer, text, background = false).apply { parent = this@ButtonElement }
    private val disabledAtlas = guiRenderer.atlasManager["button_disabled"]
    private val normalAtlas = guiRenderer.atlasManager["button_normal"]
    private val hoveredAtlas = guiRenderer.atlasManager["button_hovered"]

    private var _dynamicSized = true
    var dynamicSized: Boolean
        get() = _dynamicSized
        set(value) {
            if (_dynamicSized == value) {
                return
            }

            textElement.prefMaxSize = if (value) {
                Vec2i(-1, -1)
            } else {
                size - Vec2i(TEXT_PADDING * 2, TEXT_PADDING * 2)
            }
            _dynamicSized = value
            forceApply()
        }


    override var size: Vec2i
        get() = super.size
        set(value) {
            _dynamicSized = false
            super.size = value // will call forceApply
        }

    var disabled: Boolean = disabled
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    var hovered: Boolean = false
        private set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }


    init {
        size = textElement.size + Vec2i(TEXT_PADDING * 2, TEXT_PADDING * 2)
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val texture = when {
            disabled -> disabledAtlas
            hovered -> hoveredAtlas
            else -> normalAtlas
        } ?: guiRenderer.renderWindow.WHITE_TEXTURE

        val size = size
        val background = AtlasImageElement(guiRenderer, texture)
        background.size = size
        var zUsed = background.render(offset, z, consumer, options)
        val textSize = textElement.size
        zUsed += textElement.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, textSize.x), VerticalAlignments.CENTER.getOffset(size.y, textSize.y)), z + zUsed, consumer, options)
        return zUsed
    }

    override fun forceSilentApply() {
        textElement.silentApply()
        cacheUpToDate = false
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions) {
        if (disabled) {
            return
        }
        if (button != MouseButtons.LEFT) {
            return
        }
        if (action != MouseActions.PRESS) {
            return
        }

        submit()
    }

    override fun onSpecialKey(key: InputSpecialKey, type: KeyChangeTypes) {
        if (!hovered) {
            return
        }
        if (disabled) {
            return
        }
        if (key != InputSpecialKey.KEY_ENTER) {
            return
        }
        if (type != KeyChangeTypes.PRESS) {
            return
        }
        submit()
    }

    override fun onMouseEnter(position: Vec2i) {
        hovered = true
    }

    override fun onMouseLeave() {
        hovered = false
    }

    open fun submit() {
        onSubmit()
        if (guiRenderer.connection.profiles.audio.gui.button) {
            guiRenderer.connection.world.play2DSound(CLICK_SOUND)
        }
    }

    override fun onChildChange(child: Element) {
        if (child == textElement) {
            if (dynamicSized) {
                size = textElement.size + Vec2i(TEXT_PADDING * 2, TEXT_PADDING * 2)
            }
            cacheUpToDate = false
        }
        parent?.onChildChange(this)
    }

    private companion object {
        val CLICK_SOUND = "minecraft:ui.button.click".toResourceLocation()
        const val TEXT_PADDING = 4
    }
}
