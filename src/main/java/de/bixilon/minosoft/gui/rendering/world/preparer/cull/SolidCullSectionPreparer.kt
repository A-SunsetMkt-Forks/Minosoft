/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.preparer.cull

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.SingleBlockRenderable
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.MeshedBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.OnlyMeshedBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

class SolidCullSectionPreparer(
    val renderWindow: RenderWindow,
) : SolidSectionPreparer {
    private val profile = renderWindow.connection.profiles.block.rendering
    private val bedrock = renderWindow.connection.registries.blockRegistry[MinecraftBlocks.BEDROCK]?.defaultState
    private val someFullBlock = renderWindow.connection.registries.blockRegistry[MinecraftBlocks.COMMAND_BLOCK]?.defaultState
    private val tintColorCalculator = renderWindow.tintManager
    private var fastBedrock = false

    init {
        val profile = renderWindow.connection.profiles.rendering
        profile.performance::fastBedrock.profileWatch(this, true, profile) { this.fastBedrock = it }
    }

    override fun prepareSolid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>, mesh: WorldMesh) {
        val random = Random(0L)

        val randomBlockModels = profile.antiMoirePattern
        val isLowestSection = sectionHeight == chunk.lowestSection
        val isHighestSection = sectionHeight == chunk.highestSection
        val blocks = section.blocks
        val sectionLight = section.light
        val blockEntities: MutableSet<BlockEntityRenderer<*>> = mutableSetOf()
        var blockEntity: BlockEntity?
        var model: SingleBlockRenderable
        var blockState: BlockState
        var position: Vec3i
        var rendered: Boolean
        var tints: IntArray?
        val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.SIZE)
        val light = ByteArray(Directions.SIZE + 1) // last index (6) for the current block

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            val fastBedrock = y == 0 && isLowestSection && fastBedrock
            for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    val baseIndex = (z shl 4) or x
                    val index = (y shl 8) or baseIndex
                    blockState = blocks.unsafeGet(index) ?: continue
                    if (blockState.block is FluidBlock) {
                        continue
                    }
                    light[SELF_LIGHT_INDEX] = sectionLight[index]
                    position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)

                    val maxHeight = chunk.heightmap[baseIndex]
                    if (position.y >= maxHeight) {
                        light[SELF_LIGHT_INDEX] = (light[SELF_LIGHT_INDEX].toInt() or 0xF0).toByte()
                    }

                    blockEntity = section.blockEntities.unsafeGet(index)
                    val blockEntityModel = blockEntity?.getRenderer(renderWindow, blockState, position, light[SELF_LIGHT_INDEX].toInt())
                    if (blockEntityModel != null && (blockEntityModel !is OnlyMeshedBlockEntityRenderer)) {
                        blockEntities += blockEntityModel
                        mesh.addBlock(x, y, z)
                    }
                    model = blockState.blockModel ?: if (blockEntityModel is MeshedBlockEntityRenderer) {
                        blockEntityModel
                    } else {
                        continue
                    }


                    if (y == 0) {
                        if (fastBedrock && blockState === bedrock) {
                            neighbourBlocks[O_DOWN] = someFullBlock
                        } else {
                            neighbourBlocks[O_DOWN] = neighbours[O_DOWN]?.blocks?.unsafeGet(x, ProtocolDefinition.SECTION_MAX_Y, z)
                            light[O_DOWN] = if (isLowestSection) {
                                chunk.bottomLight
                            } else {
                                neighbours[O_DOWN]?.light
                            }?.get(ProtocolDefinition.SECTION_MAX_Y shl 8 or baseIndex) ?: 0x00
                        }
                    } else {
                        neighbourBlocks[O_DOWN] = blocks.unsafeGet((y - 1) shl 8 or baseIndex)
                        light[O_DOWN] = sectionLight[(y - 1) shl 8 or baseIndex]
                    }
                    if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbourBlocks[O_UP] = neighbours[O_UP]?.blocks?.unsafeGet(x, 0, z)
                        light[O_UP] = if (isHighestSection) {
                            chunk.topLight
                        } else {
                            neighbours[O_UP]?.light
                        }?.get((z shl 4) or x) ?: 0x00
                    } else {
                        neighbourBlocks[O_UP] = blocks.unsafeGet((y + 1) shl 8 or baseIndex)
                        light[O_UP] = sectionLight[(y + 1) shl 8 or baseIndex]
                    }

                    checkNorth(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, blocks, sectionLight, chunk)
                    checkSouth(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, blocks, sectionLight, chunk)
                    checkWest(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, blocks, sectionLight, chunk)
                    checkEast(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, blocks, sectionLight, chunk)

                    if (position.y > maxHeight) {
                        light[O_DOWN] = (light[O_DOWN].toInt() or 0xF0).toByte()
                    } else if (position.y - 1 > maxHeight) {
                        light[O_DOWN] = (light[O_DOWN].toInt() or 0xF0).toByte()
                    }

                    if (position.y + 1 > maxHeight) {
                        light[O_UP] = (light[O_UP].toInt() or 0xF0).toByte()
                    }

                    if (randomBlockModels) {
                        random.setSeed(VecUtil.generatePositionHash(position.x, position.y, position.z))
                    } else {
                        random.setSeed(0L)
                    }
                    tints = tintColorCalculator.getAverageTint(chunk, neighbourChunks, blockState, x, y, z)
                    rendered = model.singleRender(position, mesh, random, blockState, neighbourBlocks, light, tints)

                    if (blockEntityModel is MeshedBlockEntityRenderer<*>) {
                        rendered = blockEntityModel.singleRender(position, mesh, random, blockState, neighbourBlocks, light, tints) || rendered
                    }

                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                }
            }
        }
        mesh.blockEntities = blockEntities
    }

    private inline fun checkNorth(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, blocks: BlockSectionDataProvider, sectionLight: SectionLight, chunk: Chunk) {
        if (z == 0) {
            setNeighbour(neighbourBlocks, x, y, ProtocolDefinition.SECTION_MAX_Z, light, position, neighbours[O_NORTH]?.blocks, sectionLight, neighbourChunks[3], O_NORTH)
        } else {
            setNeighbour(neighbourBlocks, x, y, z - 1, light, position, blocks, sectionLight, chunk, O_NORTH)
        }
    }

    private inline fun checkSouth(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, blocks: BlockSectionDataProvider, sectionLight: SectionLight, chunk: Chunk) {
        if (z == ProtocolDefinition.SECTION_MAX_Z) {
            setNeighbour(neighbourBlocks, x, y, 0, light, position, neighbours[O_SOUTH]?.blocks, sectionLight, neighbourChunks[4], O_SOUTH)
        } else {
            setNeighbour(neighbourBlocks, x, y, z + 1, light, position, blocks, sectionLight, chunk, O_SOUTH)
        }
    }

    private inline fun checkWest(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, blocks: BlockSectionDataProvider, sectionLight: SectionLight, chunk: Chunk) {
        if (x == 0) {
            setNeighbour(neighbourBlocks, ProtocolDefinition.SECTION_MAX_X, y, z, light, position, neighbours[O_WEST]?.blocks, sectionLight, neighbourChunks[1], O_WEST)
        } else {
            setNeighbour(neighbourBlocks, x - 1, y, z, light, position, blocks, sectionLight, chunk, O_WEST)
        }
    }

    private inline fun checkEast(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, blocks: BlockSectionDataProvider, sectionLight: SectionLight, chunk: Chunk) {
        if (x == ProtocolDefinition.SECTION_MAX_X) {
            setNeighbour(neighbourBlocks, 0, y, z, light, position, neighbours[O_EAST]?.blocks, sectionLight, neighbourChunks[6], O_EAST)
        } else {
            setNeighbour(neighbourBlocks, x + 1, y, z, light, position, blocks, sectionLight, chunk, O_EAST)
        }
    }

    private inline fun setNeighbour(neighbourBlocks: Array<BlockState?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, blocks: BlockSectionDataProvider?, sectionLight: SectionLight, chunk: Chunk, ordinal: Int) {
        val nextBaseIndex = (z shl 4) or x
        val neighbourIndex = y shl 8 or nextBaseIndex
        neighbourBlocks[ordinal] = blocks?.unsafeGet(neighbourIndex)
        light[ordinal] = sectionLight[neighbourIndex]
        if (position.y > chunk.heightmap[nextBaseIndex]) {
            light[ordinal] = (light[ordinal].toInt() or 0xF0).toByte()
        }
    }

    companion object {
        const val SELF_LIGHT_INDEX = 6 // after all directions
    }
}
