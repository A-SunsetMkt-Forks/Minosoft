/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.wood

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterloggableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.transparency.TransparentBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsRequirement
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.CustomBlockCulling

abstract class LeavesBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), CustomBlockCulling, FullBlock, TransparentBlock, BlockStateBuilder, ShearsRequirement, WaterloggableBlock {
    override val hardness get() = 0.2f

    override fun buildState(settings: BlockStateSettings): BlockState {
        return PropertyBlockState(this, settings)
    }

    override fun shouldCull(state: BlockState, face: BakedFace, directions: Directions, neighbour: BlockState): Boolean {
        return neighbour.block != this
    }
}
