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

package de.bixilon.minosoft.gui.rendering.hud.nodes.layout

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.Node
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeSizing
import glm_.vec2.Vec2i

class RowLayout(
    renderWindow: RenderWindow,
    sizing: NodeSizing = NodeSizing(),
    initialCacheSize: Int = DEFAULT_INITIAL_CACHE_SIZE,
) : AbsoluteLayout(renderWindow, sizing, initialCacheSize) {

    fun addRow(node: Node) {
        addChild(Vec2i(0, sizing.currentSize.y + node.sizing.margin.top + node.sizing.padding.top), node)
        apply()
    }
}