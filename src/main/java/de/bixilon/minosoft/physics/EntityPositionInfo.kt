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

package de.bixilon.minosoft.physics

import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.physics.entities.EntityPhysics

class EntityPositionInfo(
    val revision: Int,
    val chunkPosition: ChunkPosition,
    val sectionHeight: Int,
    val blockPosition: BlockPosition,
    val eyePosition: BlockPosition,
    val inChunkPosition: InChunkPosition,
    val inSectionPosition: InSectionPosition,

    val chunk: Chunk?,
    val block: BlockState?,
    val velocityBlock: BlockState?,
) {
    val biome: Biome? get() = chunk?.getBiome(inChunkPosition)

    companion object {
        val EMPTY = EntityPositionInfo(0, ChunkPosition.EMPTY, 0, BlockPosition.EMPTY, BlockPosition.EMPTY, InChunkPosition.EMPTY, InSectionPosition.EMPTY, null, null, null)


        fun of(physics: EntityPhysics<*>, previous: EntityPositionInfo = EMPTY): EntityPositionInfo {
            val position = physics.position
            val blockPosition = position.blockPosition
            val chunkPosition = blockPosition.chunkPosition
            val sectionHeight = blockPosition.sectionHeight
            val eyePosition = BlockPosition(position.x.floor, (position.y + physics.entity.eyeHeight).floor, position.z.floor)
            val inChunkPosition = blockPosition.inChunkPosition
            val inSectionPosition = blockPosition.inSectionPosition

            val velocityPosition = BlockPosition(blockPosition.x, (position.y - 0.5000001).toInt(), blockPosition.z)

            val world = physics.entity.session.world
            world.lock.acquire()
            val revision = world.chunks.revision

            var chunk = if (previous.revision == revision) previous.chunk?.neighbours?.trace(chunkPosition - previous.chunkPosition) else null

            if (chunk == null) {
                chunk = world.chunks.chunks.unsafe[chunkPosition]
            }
            world.lock.release()

            val block = chunk?.get(inChunkPosition)
            val velocityBlock = chunk?.get(velocityPosition.inChunkPosition)

            return EntityPositionInfo(revision, chunkPosition, sectionHeight, blockPosition, eyePosition, inChunkPosition, inSectionPosition, chunk, block, velocityBlock)
        }
    }
}
