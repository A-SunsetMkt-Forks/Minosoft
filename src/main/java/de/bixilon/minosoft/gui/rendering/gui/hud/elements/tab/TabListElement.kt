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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2i
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class TabListElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val header = TextElement(hudRenderer, "", background = false, fontAlignment = HorizontalAlignments.CENTER, parent = this)
    val footer = TextElement(hudRenderer, "", background = false, fontAlignment = HorizontalAlignments.CENTER, parent = this)

    private val background = ColorElement(hudRenderer, Vec2i.EMPTY, color = RGBColor(0, 0, 0, 120))

    private var entriesSize = Vec2i.EMPTY
    private val entries: MutableMap<UUID, TabListEntryElement> = synchronizedMapOf()
    private var toRender: List<TabListEntryElement> = listOf()
    private val lock = ReentrantLock()
    var needsApply = false
        private set
    private var columns = 0

    val pingBarsAtlasElements: Array<HUDAtlasElement> = arrayOf(
        hudRenderer.atlasManager["minecraft:tab_list_ping_0"]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_1"]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_2"]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_3"]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_4"]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_5"]!!,
    )

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        background.render(offset, z, consumer, options)

        offset.y += BACKGROUND_PADDING // No need for x, this is done with the CENTER offset calculation

        val size = size

        header.size.let {
            header.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, it.x), 0), z, consumer, options)
            offset.y += it.y
        }

        val offsetBefore = Vec2i(offset)
        offset.x += HorizontalAlignments.CENTER.getOffset(size.x, entriesSize.x)

        for ((index, entry) in toRender.withIndex()) {
            entry.render(offset, z + 1, consumer, options)
            offset.y += TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                offset.x += entry.width + ENTRY_HORIZONTAL_SPACING
                offset.y = offsetBefore.y
            }
        }
        offset.x = offsetBefore.x
        offset.y = offsetBefore.y + (columns > 1).decide(ENTRIES_PER_COLUMN, toRender.size) * (TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING)


        footer.size.let {
            footer.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, it.x)), z, consumer, options)
            offset.y += it.y
        }

        return TextElement.LAYERS + 1 // ToDo
    }

    override fun forceSilentApply() {
        val size = Vec2i.EMPTY

        size.y += header.size.y

        var toRender: MutableList<TabListEntryElement> = mutableListOf()
        toRender += entries.toSynchronizedMap().values

        lock.lock()
        toRender.sort()
        lock.unlock()

        // Minecraft limits it to 80 items. Imho this is removing a feature, but some servers use a custom tab list plugin and then players are duplicated, etc
        // ToDo: Detect custom tab list, e.g. check player names for non valid chars, etc
        toRender = toRender.subList(0, minOf(toRender.size, MAX_ENTRIES))


        val previousSize = Vec2i(size)
        var columns = toRender.size / ENTRIES_PER_COLUMN
        if (toRender.size % ENTRIES_PER_COLUMN > 0) {
            columns++
        }

        var column = 0
        val widths = IntArray(columns)
        var currentMaxPrefWidth = 0
        var totalEntriesWidth = 0


        // Check width
        for ((index, entry) in toRender.withIndex()) {
            val prefWidth = entry.prefSize

            currentMaxPrefWidth = maxOf(currentMaxPrefWidth, prefWidth.x)
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                widths[column] = currentMaxPrefWidth
                totalEntriesWidth += currentMaxPrefWidth
                currentMaxPrefWidth = 0
                column++
            }
        }
        if (currentMaxPrefWidth != 0) {
            widths[column] = currentMaxPrefWidth
            totalEntriesWidth += currentMaxPrefWidth
        }
        size.y += (columns > 1).decide(ENTRIES_PER_COLUMN, toRender.size) * (TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING)

        size.y -= ENTRY_VERTICAL_SPACING // Remove already added space again


        // add horizontal spacing to columns
        if (columns >= 2) {
            totalEntriesWidth += (columns - 1) * ENTRY_HORIZONTAL_SPACING
        }

        this.entriesSize = Vec2i(totalEntriesWidth, size.y - previousSize.y)
        size.x = maxOf(size.x, totalEntriesWidth)


        // apply width to every cell
        column = 0
        for ((index, entry) in toRender.withIndex()) {
            entry.width = widths[column]
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                column++
            }
        }

        this.toRender = toRender

        size.y += footer.size.y

        size.x = maxOf(size.x, header.size.x, footer.size.x)


        this.columns = columns
        size += (BACKGROUND_PADDING * 2)
        this.size = size
        background.size = size

        cacheUpToDate = false
        needsApply = false
    }

    override fun silentApply(): Boolean {
        // ToDo: Check for changes
        for (element in toRender) {
            element.silentApply()
        }
        return true
    }

    fun update(uuid: UUID) {
        val item = hudRenderer.connection.tabList.tabListItemsByUUID[uuid] ?: return
        val entry = entries.getOrPut(uuid) { TabListEntryElement(hudRenderer, this, item, 0) }
        lock.lock()
        entry.silentApply()
        lock.unlock()
        needsApply = true
    }

    fun remove(uuid: UUID) {
        entries -= uuid
        needsApply = true
    }

    override fun onChildChange(child: Element) {
        needsApply = true
    }


    companion object {
        private const val ENTRIES_PER_COLUMN = 20
        private const val ENTRY_HORIZONTAL_SPACING = 5
        private const val ENTRY_VERTICAL_SPACING = 1
        private const val BACKGROUND_PADDING = 3
        private const val MAX_ENTRIES = 80
    }
}