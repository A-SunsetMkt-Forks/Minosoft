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

package de.bixilon.minosoft.data.registries.blocks.types.building.plants

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.FullOutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class FernBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), ShearsRequirement, BlockWithItem<Item>, FullOutlinedBlock, RandomOffsetBlock, InstantBreakableBlock {
    override val randomOffset get() = RandomOffsetTypes.XYZ
    override val item: Item = this::item.inject(identifier)

    override fun getOutlineShape(connection: PlayConnection, blockState: BlockState) = SHAPE

    companion object {
        private val SHAPE = VoxelShape(0.125, 0.0, 0.125, 0.875, 0.8125, 0.875)

    }

    open class DeadBush(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FernBlock(identifier, settings) {

        companion object : BlockFactory<DeadBush> {
            override val identifier = minecraft("dead_bush")

            override fun build(registries: Registries, settings: BlockSettings) = DeadBush(settings = settings)
        }
    }

    open class Grass(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FernBlock(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName get() = minecraft("tall_grass")

        companion object : BlockFactory<Grass> {
            override val identifier = minecraft("grass")

            override fun build(registries: Registries, settings: BlockSettings) = Grass(settings = settings)
        }
    }

    open class Fern(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FernBlock(identifier, settings) {

        companion object : BlockFactory<Fern> {
            override val identifier = minecraft("fern")

            override fun build(registries: Registries, settings: BlockSettings) = Fern(settings = settings)
        }
    }
}