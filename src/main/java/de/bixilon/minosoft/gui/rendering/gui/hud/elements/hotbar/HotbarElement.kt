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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Arms
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.left
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.right
import glm_.vec2.Vec2i
import java.lang.Integer.max

class HotbarElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val core = HotbarCoreElement(hudRenderer)

    val offhand = HotbarOffhandElement(hudRenderer)
    private var renderOffhand = false

    val hoverText = FadingTextElement(hudRenderer, text = "", fadeInTime = 300, stayTime = 3000, fadeOutTime = 500, background = false, noBorder = true)
    private var hoverTextShown = false

    private val itemText = FadingTextElement(hudRenderer, text = "", fadeInTime = 300, stayTime = 1500, fadeOutTime = 500, background = false, noBorder = true)
    private var lastItemStackNameShown: ItemStack? = null
    private var lastItemSlot = -1
    private var itemTextShown = true

    private var renderElements = setOf(
        itemText,
        hoverText,
    )

    override var cacheEnabled: Boolean = false // ToDo: Cache correctly

    init {
        core.parent = this
        itemText.parent = this
        hoverText.parent = this
        offhand.parent = this
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val size = size
        var maxZ = 0


        if (hoverTextShown) {
            hoverText.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, hoverText.size.x), 0), z, consumer, options)
            offset.y += hoverText.size.y + HOVER_TEXT_OFFSET
        }
        if (itemTextShown) {
            itemText.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, itemText.size.x), 0), z, consumer, options)
            offset.y += itemText.size.y + ITEM_NAME_OFFSET
        }

        val coreOffset = offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, core.size.x), 0)

        if (renderOffhand) {
            val offhandOffset = Vec2i.EMPTY
            if (offhand.offArm == Arms.LEFT) {
                offhandOffset.x = -offhand.size.x - offhand.margin.right
            } else {
                offhandOffset.x = core.size.x + offhand.margin.left
            }
            offhandOffset.y = core.size.y - offhand.size.y
            maxZ = max(maxZ, offhand.render(coreOffset + offhandOffset, z, consumer, options))
        }

        maxZ = max(maxZ, core.render(coreOffset, z, consumer, options))

        return maxZ
    }

    override fun forceSilentApply() {
        for (element in renderElements) {
            element.silentApply()
        }

        val size = Vec2i(core.size)

        renderOffhand = hudRenderer.connection.player.inventory[InventorySlots.EquipmentSlots.OFF_HAND] != null

        if (renderOffhand) {
            size.x += offhand.size.x
            size.y = max(size.y, offhand.size.y)
        }

        itemTextShown = !itemText.hidden
        if (itemTextShown) {
            size.y += itemText.size.y + ITEM_NAME_OFFSET
            size.x = max(size.x, itemText.size.x)
        }

        hoverTextShown = !hoverText.hidden
        if (hoverTextShown) {
            size.y += hoverText.size.y + HOVER_TEXT_OFFSET
            size.x = max(size.x, hoverText.size.x)
        }

        _size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        val itemSlot = hudRenderer.connection.player.selectedHotbarSlot
        val currentItem = hudRenderer.connection.player.inventory.getHotbarSlot(itemSlot)
        if (currentItem != lastItemStackNameShown || itemSlot != lastItemSlot) {
            lastItemStackNameShown = currentItem
            lastItemSlot = itemSlot
            currentItem?.displayName?.let { itemText.text = it } // ToDo: This calls silentApply again...
            if (currentItem == null) {
                itemText.hide()
            } else {
                itemText.show()
            }
        }

        forceSilentApply() // ToDo: Check stuff
        return true
    }

    override fun onChildChange(child: Element) {
        silentApply() // ToDo: Check
        parent?.onChildChange(this)
    }

    override fun tick() {
        silentApply()
        hoverText.tick()
        itemText.tick()
        core.tick()
        offhand.tick()
    }

    companion object {
        private const val HOVER_TEXT_OFFSET = 15
        private const val ITEM_NAME_OFFSET = 5
    }
}
