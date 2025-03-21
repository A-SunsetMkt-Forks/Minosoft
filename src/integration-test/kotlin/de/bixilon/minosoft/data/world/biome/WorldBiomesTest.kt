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

package de.bixilon.minosoft.data.world.biome

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.WorldTestUtil.initialize
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["biomes"])
class WorldBiomesTest {
    private val b1 = Biome(minosoft("b1"), 0.0f, 0.0f)
    private val b2 = Biome(minosoft("b2"), 0.0f, 0.0f)
    private val b3 = Biome(minosoft("b3"), 0.0f, 0.0f)

    private fun create(noise: ((PlaySession) -> NoiseBiomeAccessor)?, source: (ChunkPosition) -> BiomeSource): World {
        val session = createSession(0)
        session.world.biomes.noise = noise?.invoke(session)
        session.world.initialize(1, source)

        return session.world
    }

    fun `simple biome getting at origin chunk`() {
        val world = create(null) { if (it.x == 0 && it.z == 0) PositionedSource(InChunkPosition(1, 2, 3), b1, b3) else DummyBiomeSource(b2) }
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(world.biomes[BlockPosition(1, 2, 4)], b3)
        assertEquals(world.biomes[BlockPosition(16, 2, 4)], b2)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(world.biomes[BlockPosition(1, 2, 4)], b3)
        assertEquals(world.biomes[BlockPosition(16, 2, 4)], b2)
        val chunk = world.chunks[0, 0]!!
        assertEquals(world.biomes[InChunkPosition(1, 2, 3), chunk], b1)
        assertEquals(world.biomes[InChunkPosition(1, 2, 4), chunk], b3)
        assertEquals(chunk.getBiome(InChunkPosition(1, 2, 3)), b1)
        assertEquals(chunk.getBiome(InChunkPosition(1, 2, 4)), b3)
    }

    fun `simple biome getting at chunk -1, -1`() {
        val world = create(null) { if (it.x == -1 && it.z == -1) PositionedSource(InChunkPosition(15, 2, 14), b1, b3) else DummyBiomeSource(b2) }
        assertEquals(world.biomes[BlockPosition(-1, 2, -2)], b1)
        assertEquals(world.biomes[BlockPosition(-1, 2, -6)], b3)
        assertEquals(world.biomes[BlockPosition(1, 2, 6)], b2)
        assertEquals(world.biomes[BlockPosition(-1, 2, -2)], b1)
        val chunk = world.chunks[-1, -1]!!
        assertEquals(world.biomes[InChunkPosition(15, 2, 14), chunk], b1)
        assertEquals(world.biomes[InChunkPosition(15, 2, 13), chunk], b3)
        assertEquals(chunk.getBiome(InChunkPosition(15, 2, 14)), b1)
        assertEquals(chunk.getBiome(InChunkPosition(15, 2, 13)), b3)
    }

    fun `ensure no caching is done without noise`() {
        val source = CounterSource(b1)
        val world = create(null) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.getOrPut(0)

        assertEquals(source.counter, 0)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        assertFalse(chunk.cacheBiomes)
        assertEquals(chunk[0]!!.biomes[1, 2, 3], null)
    }

    fun `ensure caching is done with noise`() {
        val source = CounterSource(b1)
        val world = create({ FastNoiseAccessor(it.world) }) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.getOrPut(0)

        assertEquals(source.counter, 0) // biomes ore on demand
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1) // don't query again
        assertTrue(chunk.cacheBiomes)
        assertEquals(chunk[0]!!.biomes[1, 2, 3], b1)
    }

    fun `ensure world position is converted to chunk position`() {
        val source = VerifyPositionSource(b1)
        val world = create(null) { source }

        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(world.biomes[BlockPosition(16, 2, 4)], b1)
        assertEquals(world.biomes[BlockPosition(-4, 2, -4)], b1)

        assertEquals(world.biomes[BlockPosition(-4, -2, -4)], b1)
        assertEquals(world.biomes[BlockPosition(-4, 1024, -4)], b1)
    }


    fun `ensure caching is properly cleared`() {
        val source = CounterSource(b1)
        val world = create({ FastNoiseAccessor(it.world) }) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.getOrPut(0)

        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        source.biome = b2
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1) // cache is still the old one

        world.biomes.resetCache()
        assertEquals(source.counter, 1)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b2)
        assertEquals(source.counter, 2)
    }


    private class PositionedSource(
        val position: InChunkPosition,
        val biome: Biome?,
        val fallback: Biome?,
    ) : BiomeSource {

        override fun get(position: InChunkPosition): Biome? {
            if (this.position != position) return fallback
            return biome
        }
    }

    private class CounterSource(var biome: Biome?) : BiomeSource {
        var counter = 0

        override fun get(position: InChunkPosition): Biome? {
            counter++
            return biome
        }
    }

    private class VerifyPositionSource(val biome: Biome?) : BiomeSource {
        override fun get(position: InChunkPosition): Biome? {
            if (position.x < 0 || position.x > 15) throw IllegalArgumentException("Invalid x: ${position.x}")
            if (position.y < 0 || position.y > 255) throw IllegalArgumentException("Invalid y: ${position.y}")
            if (position.z < 0 || position.z > 15) throw IllegalArgumentException("Invalid z: ${position.z}")

            return biome
        }
    }

    private class FastNoiseAccessor(world: World) : NoiseBiomeAccessor(world, 0L) {

        override fun get(position: InChunkPosition, chunk: Chunk): Biome? {
            return chunk.biomeSource.get(position)
        }
    }
}
