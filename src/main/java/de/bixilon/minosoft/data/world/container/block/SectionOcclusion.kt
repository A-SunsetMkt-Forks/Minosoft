/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.PotentialFullOpaqueBlock
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.allocator.ShortAllocator
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import java.util.*

class SectionOcclusion(
    val provider: BlockSectionDataProvider,
) {
    private var occlusion = EMPTY
    private var calculate = false

    fun clear(notify: Boolean) {
        update(EMPTY, notify)
    }

    fun onSet(previous: BlockState?, value: BlockState?) {
        if (previous.isFullyOpaque() == value.isFullyOpaque()) {
            return
        }
        recalculate(true)
    }

    fun recalculate(notify: Boolean) {
        if (!calculate) return

        if (provider.isEmpty) {
            clear(notify)
            return
        }
        val array = ALLOCATOR.allocate(ProtocolDefinition.BLOCKS_PER_SECTION)
        try {
            val regions = floodFill(array)
            update(calculateOcclusion(regions), notify)
        } finally {
            ALLOCATOR.free(array)
        }
    }

    private inline fun ShortArray.setIfUnset(position: InSectionPosition, region: Int): Boolean {
        if (this[position.index] != EMPTY_REGION) {
            return true
        }
        val state = provider[position]
        if (state.isFullyOpaque()) {
            this[position.index] = INVALID_REGION
            return true
        }
        this[position.index] = region.toShort()
        return false
    }

    private fun startTrace(regions: ShortArray, position: InSectionPosition) {
        if (regions.setIfUnset(position, position.index)) return

        // no need to trace negative coordinates initially
        if (position.x < ProtocolDefinition.SECTION_MAX_X) trace(regions, position.plusX(), position.index)
        if (position.z < ProtocolDefinition.SECTION_MAX_Z) trace(regions, position.plusZ(), position.index)
        if (position.y < ProtocolDefinition.SECTION_MAX_Y) trace(regions, position.plusY(), position.index)
    }

    private fun trace(regions: ShortArray, position: InSectionPosition, region: Int) {
        if (regions.setIfUnset(position, region)) return

        if (position.x > 0) trace(regions, position.minusX(), region)
        if (position.x < ProtocolDefinition.SECTION_MAX_X) trace(regions, position.plusX(), region)
        if (position.z > 0) trace(regions, position.minusZ(), region)
        if (position.z < ProtocolDefinition.SECTION_MAX_Z) trace(regions, position.plusZ(), region)
        if (position.y > 0) trace(regions, position.minusY(), region)
        if (position.y < ProtocolDefinition.SECTION_MAX_Y) trace(regions, position.plusY(), region)
    }

    private fun floodFill(array: ShortArray): ShortArray {
        // mark regions and check direct neighbours
        Arrays.fill(array, EMPTY_REGION)

        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            startTrace(array, InSectionPosition(index))
        }

        return array
    }

    private fun calculateOcclusion(regions: ShortArray): BooleanArray {
        val sideRegions: Array<IntOpenHashSet> = Array(Directions.SIZE) { IntOpenHashSet() }

        for (axis in Axes.VALUES) {
            for (a in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (b in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val indexPrefix = when (axis) {
                        Axes.X -> a shl 8 or (b shl 4)
                        Axes.Y -> (a shl 4) or b
                        Axes.Z -> (a shl 8) or b
                    }
                    val nDirection = when (axis) {
                        Axes.X -> Directions.WEST
                        Axes.Y -> Directions.DOWN
                        Axes.Z -> Directions.NORTH
                    }
                    val nRegion = regions[indexPrefix].toInt()
                    if (nRegion > EMPTY_REGION) {
                        sideRegions[nDirection.ordinal].add(nRegion) // primitive
                    }

                    val pDirection = when (axis) {
                        Axes.X -> Directions.EAST
                        Axes.Y -> Directions.UP
                        Axes.Z -> Directions.SOUTH
                    }
                    val index2 = indexPrefix or when (axis) {
                        Axes.X -> ProtocolDefinition.SECTION_MAX_X
                        Axes.Y -> ProtocolDefinition.SECTION_MAX_Y shl 8
                        Axes.Z -> ProtocolDefinition.SECTION_MAX_Z shl 4
                    }
                    val pRegion = regions[index2].toInt()
                    if (pRegion > EMPTY_REGION) {
                        sideRegions[pDirection.ordinal].add(pRegion) // primitive
                    }
                }
            }
        }

        val occlusion = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
            occlusion[index] = sideRegions.canOcclude(pair.`in`, pair.out)
        }
        return occlusion
    }

    private fun update(occlusion: BooleanArray, notify: Boolean) {
        if (this.occlusion.contentEquals(occlusion)) {
            return
        }
        this.occlusion = occlusion
        if (notify) {
            provider.section.chunk.world.occlusion++
        }
    }

    private fun Array<IntOpenHashSet>.canOcclude(`in`: Directions, out: Directions): Boolean {
        val inSides = this[`in`.ordinal]
        val outSides = this[`out`.ordinal]
        if (inSides.isEmpty() || outSides.isEmpty()) {
            return true
        }

        val preferIn = inSides.size < outSides.size
        val first = if (preferIn) inSides else outSides
        val second = if (preferIn) outSides else inSides

        val iterator = first.intIterator()
        while (iterator.hasNext()) {
            val region = iterator.nextInt()
            if (second.contains(region)) {
                return false
            }
        }
        return true
    }

    /**
     * If we can **not** look from `in` to `out`
     */
    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        if (`in` == out) {
            return false
        }
        return isOccluded(CubeDirections.getIndex(`in`, out))
    }

    fun isOccluded(index: Int): Boolean {
        if (!calculate) {
            calculate = true
            recalculate(false)
        }
        return occlusion[index]
    }

    companion object {
        private const val EMPTY_REGION = (-1).toShort()
        private const val INVALID_REGION = (-2).toShort()
        private val EMPTY = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        private val ALLOCATOR = ShortAllocator()

        private const val X = 0x001
        private const val Y = 0x100
        private const val Z = 0x010


        fun BlockState?._isFullyOpaque(): Boolean {
            if (this == null) {
                return false
            }
            if (BlockStateFlags.FULLY_OPAQUE in flags) return true
            val block = this.block
            if (block is FullOpaqueBlock) return true
            if (block !is PotentialFullOpaqueBlock) return false

            return block.isFullOpaque(this)
        }

        fun BlockState?.isFullyOpaque(): Boolean {
            if (this == null) return false
            return BlockStateFlags.FULLY_OPAQUE in flags
        }
    }
}
