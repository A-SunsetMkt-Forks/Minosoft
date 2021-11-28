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

package de.bixilon.minosoft.gui.rendering.gui.elements.primitive

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

open class ImageElement(
    hudRenderer: HUDRenderer,
    texture: AbstractTexture,
    uvStart: Vec2 = Vec2.EMPTY,
    uvEnd: Vec2 = Vec2(1.0f, 1.0f),
    size: Vec2i = texture.size,
    tint: RGBColor = ChatColors.WHITE,
) : Element(hudRenderer) {
    override var initialCacheSize: Int = GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6
    var texture: AbstractTexture = texture
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvStart: Vec2 = uvStart
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvEnd: Vec2 = uvEnd
        set(value) {
            field = value
            cacheUpToDate = false
        }

    override var size: Vec2i
        get() = super.size
        set(value) {
            super.size = value
            cacheUpToDate = false
        }

    override var prefSize: Vec2i
        get() = size
        set(value) {
            size = value
        }

    var tint: RGBColor = tint
        set(value) {
            field = value
            cacheUpToDate = false
        }

    init {
        this.size = size
    }

    constructor(hudRenderer: HUDRenderer, texture: TextureLike, size: Vec2i = texture.size, tint: RGBColor = ChatColors.WHITE) : this(hudRenderer, texture.texture, texture.uvStart, texture.uvEnd, size, tint)

    constructor(hudRenderer: HUDRenderer, texture: AbstractTexture, uvStart: Vec2i, uvEnd: Vec2i, size: Vec2i = texture.size, tint: RGBColor = ChatColors.WHITE) : this(hudRenderer, texture, Vec2(uvStart) * texture.singlePixelSize, Vec2(uvEnd) * texture.singlePixelSize, size, tint)

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        consumer.addQuad(offset, offset + size, z, texture, uvStart, uvEnd, tint, options)
        return 1
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false
}