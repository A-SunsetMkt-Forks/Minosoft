/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.sounds.SoundEvent
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.WorldBiomeAccessor
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.sound.AudioPlayer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.minus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.ChunkUnloadEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.MMath
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.chunk.ChunkUtil.fullyLoaded
import de.bixilon.minosoft.util.collections.SynchronizedMap
import glm_.func.common.clamp
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.random.Random

/**
 * Collection of chunks and more
 */
class World(
    val connection: PlayConnection,
) : BiomeAccessor {
    var cacheBiomeAccessor: BiomeAccessor? = null
    val chunks: SynchronizedMap<Vec2i, Chunk> = synchronizedMapOf()
    val entities = WorldEntities()
    var hardcore = false
    var dimension: DimensionProperties? = null
    var difficulty: Difficulties? = null
    var difficultyLocked = false
    var hashedSeed = 0L
    val biomeAccessor: BiomeAccessor = WorldBiomeAccessor(this)
    var time = 0L
    var age = 0L
    var raining = false
    var rainGradient = 0.0f
    var thunderGradient = 0.0f
    private val random = Random

    var audioPlayer: AudioPlayer? = null
    var particleRenderer: ParticleRenderer? = null

    operator fun get(blockPosition: Vec3i): BlockState? {
        return chunks[blockPosition.chunkPosition]?.get(blockPosition.inChunkPosition)
    }

    operator fun get(chunkPosition: Vec2i): Chunk? {
        return chunks[chunkPosition]
    }

    fun getOrCreateChunk(chunkPosition: Vec2i): Chunk {
        return chunks.getOrPut(chunkPosition) { Chunk(connection, chunkPosition) }
    }

    fun setBlockState(blockPosition: Vec3i, blockState: BlockState?) {
        this[blockPosition] = blockState
    }

    operator fun set(blockPosition: Vec3i, blockState: BlockState?) {
        val chunk = chunks[blockPosition.chunkPosition] ?: return
        val inChunkPosition = blockPosition.inChunkPosition
        val previousBlock = chunk[inChunkPosition]
        if (previousBlock == blockState) {
            return
        }
        previousBlock?.block?.onBreak(connection, blockPosition, previousBlock, chunk.getBlockEntity(inChunkPosition))
        blockState?.block?.onPlace(connection, blockPosition, blockState)
        chunk[inChunkPosition] = blockState
        connection.fireEvent(BlockSetEvent(
            connection = connection,
            blockPosition = blockPosition,
            blockState = blockState,
        ))
    }

    fun isPositionChangeable(blockPosition: Vec3i): Boolean {
        // ToDo: World border
        val dimension = connection.world.dimension!!
        return (blockPosition.y >= dimension.minY || blockPosition.y < dimension.height)
    }

    fun forceSetBlockState(blockPosition: Vec3i, blockState: BlockState?, check: Boolean = false) {
        if (check && !isPositionChangeable(blockPosition)) {
            return
        }
        chunks[blockPosition.chunkPosition]?.set(blockPosition.inChunkPosition, blockState)
    }

    fun unloadChunk(chunkPosition: Vec2i) {
        chunks.remove(chunkPosition) ?: return
        for (neighbour in getChunkNeighbours(chunkPosition)) {
            neighbour?.neighboursLoaded = false
        }
        connection.fireEvent(ChunkUnloadEvent(connection, EventInitiators.UNKNOWN, chunkPosition))
    }

    fun getBlockEntity(blockPosition: Vec3i): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getBlockEntity(blockPosition.inChunkPosition)
    }

    fun setBlockEntity(blockPosition: Vec3i, blockEntity: BlockEntity?) {
        get(blockPosition.chunkPosition)?.setBlockEntity(blockPosition.inChunkPosition, blockEntity)
    }


    fun setBlockEntities(blockEntities: Map<Vec3i, BlockEntity>) {
        for ((blockPosition, blockEntity) in blockEntities) {
            setBlockEntity(blockPosition, blockEntity)
        }
    }

    override fun getBiome(blockPosition: Vec3i): Biome? {
        return biomeAccessor.getBiome(blockPosition)
    }

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        return biomeAccessor.getBiome(x, y, z)
    }

    fun tick() {
        for ((chunkPosition, chunk) in chunks.toSynchronizedMap()) {
            chunk.tick(connection, chunkPosition)
        }
    }

    fun randomTick() {
        for (i in 0 until 667) {
            randomTick(16)
            randomTick(32)
        }
    }

    private fun randomTick(radius: Int) {
        val blockPosition = connection.player.position.blockPosition + { random.nextInt(radius) } - { random.nextInt(radius) }

        val blockState = this[blockPosition] ?: return

        blockState.block.randomTick(connection, blockState, blockPosition, random)
    }

    operator fun get(aabb: AABB): Map<Vec3i, BlockState> {
        val ret: MutableMap<Vec3i, BlockState> = mutableMapOf()
        for (position in aabb.blockPositions) {
            this[position]?.let { ret[position] = it }
        }
        return ret.toMap()
    }

    fun playSoundEvent(resourceLocation: ResourceLocation, position: Vec3i? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        audioPlayer?.playSoundEvent(resourceLocation, position, volume, pitch)
    }

    fun playSoundEvent(resourceLocation: ResourceLocation, position: Vec3? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        audioPlayer?.playSoundEvent(resourceLocation, position, volume, pitch)
    }

    fun playSoundEvent(soundEvent: SoundEvent, position: Vec3i? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        audioPlayer?.playSoundEvent(soundEvent, position, volume, pitch)
    }

    fun playSoundEvent(soundEvent: SoundEvent, position: Vec3? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        audioPlayer?.playSoundEvent(soundEvent, position, volume, pitch)
    }

    fun addParticle(particle: Particle) {
        particleRenderer?.add(particle)
    }

    operator fun plusAssign(particle: Particle?) {
        addParticle(particle ?: return)
    }

    fun isSpaceEmpty(aabb: AABB, checkFluids: Boolean = false): Boolean {
        for (position in aabb.blockPositions) {
            val blockState = this[position] ?: continue
            if ((blockState.collisionShape + position).intersect(aabb)) {
                return false
            }
            if (!checkFluids || blockState.block !is FluidBlock) {
                continue
            }
            val height = blockState.block.fluid.getHeight(blockState)
            if (position.y + height > aabb.min.y) {
                return false
            }
        }
        return true
    }

    fun getLight(blockPosition: Vec3i): Int {
        return get(blockPosition.chunkPosition)?.getLight(blockPosition.inChunkPosition) ?: 0xFF
    }

    val skyAngle: Double
        get() {
            val fractionalPath = MMath.fractionalPart(abs(time) / ProtocolDefinition.TICKS_PER_DAYf - 0.25)
            val angle = 0.5 - cos(fractionalPath * Math.PI) / 2.0
            return (fractionalPath * 2.0 + angle) / 3.0
        }

    val lightBase: Double
        get() {
            var base = 1.0f - (cos(skyAngle * 2.0 * PI) * 2.0 + 0.2)
            base = base.clamp(0.0, 1.0)
            base = 1.0 - base

            base *= 1.0 - ((rainGradient * 5.0) / 16.0)
            base *= 1.0 - (((thunderGradient * rainGradient) * 5.0) / 16.0)
            return base * 0.8 + 0.2
        }


    /**
     * @return All 8  neighbour chunks
     */
    fun getChunkNeighbours(neighbourPositions: Array<Vec2i>): Array<Chunk?> {
        val chunks: Array<Chunk?> = arrayOfNulls(neighbourPositions.size)
        for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
            chunks[index] = this[neighbourPosition] ?: continue
        }
        return chunks
    }

    fun getChunkNeighbours(chunkPosition: Vec2i): Array<Chunk?> {
        return getChunkNeighbours(ChunkUtil.getChunkNeighbourPositions(chunkPosition))
    }

    fun onChunkUpdate(chunkPosition: Vec2i, chunk: Chunk = get(chunkPosition)!!) {
        val neighbourPositions = ChunkUtil.getChunkNeighbourPositions(chunkPosition)
        val neighbours = getChunkNeighbours(neighbourPositions)
        if (chunk.neighboursLoaded) {
            return
        }
        if (neighbours.fullyLoaded) {
            chunk.neighboursLoaded = true
            chunk.buildBiomeCache()
        }
        for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
            if (neighbourPosition == chunkPosition) {
                continue
            }
            val neighbourChunk = neighbours[index] ?: continue

            if (neighbourChunk.neighboursLoaded) {
                continue
            }
            var biomeSourceLoaded = true
            for (neighbourNeighbourChunk in getChunkNeighbours(neighbourPosition)) {
                if (neighbourNeighbourChunk?.biomeSource == null) {
                    biomeSourceLoaded = false
                    break
                }
            }
            if (biomeSourceLoaded) {
                neighbourChunk.neighboursLoaded = true
                neighbourChunk.buildBiomeCache()
            }
        }
    }


    companion object {
        const val MAX_SIZE = 29999999
        const val MAX_SIZEf = MAX_SIZE.toFloat()
        const val MAX_SIZEd = MAX_SIZE.toDouble()
    }
}
