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

package de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid

import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.ChildAlignable

class GridColumnConstraint(
    var prefWidth: Int = 0,
    var maxWidth: Int = Int.MAX_VALUE,
    var grow: GridGrow = GridGrow.ALWAYS,
    var alignment: ElementAlignments = ElementAlignments.LEFT,
) : ChildAlignable {
    override var childAlignment: ElementAlignments by this::alignment

    var width: Int = 0
}
