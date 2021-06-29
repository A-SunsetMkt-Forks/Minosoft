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

package de.bixilon.minosoft.data.registries.fluid.lava

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava.LavaParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.chance
import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

class LavaFluid(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : FlowableFluid(resourceLocation, registries, data) {
    private val lavaParticleType: ParticleType? = null
    override val stillTexture: ResourceLocation = "minecraft:block/lava_still".asResourceLocation()
    override val flowingTexture: ResourceLocation = "minecraft:block/lava_flow".asResourceLocation()

    init {
        this::lavaParticleType.inject(LavaParticle)
    }

    override fun getVelocityMultiplier(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Double {
        return (connection.world.dimension?.ultraWarm == true).decide(0.007, 0.0023333333333333335)
    }

    override fun matches(other: Fluid): Boolean {
        return other is LavaFluid
    }

    override fun travel(entity: LocalPlayerEntity, sidewaysSpeed: Float, forwardSpeed: Float, gravity: Double, falling: Boolean) {
        entity.accelerate(sidewaysSpeed, forwardSpeed, 0.02)

        val fluidHeight = entity.fluidHeights[DefaultFluids.LAVA] ?: 0.0f

        if (fluidHeight <= entity.swimHeight) {
            entity.velocity = entity.velocity * Vec3d(0.5, 0.800000011920929, 0.5)
            entity.velocity = updateMovement(entity, gravity, falling, entity.velocity)
        } else {
            entity.velocity = entity.velocity * 0.5
        }

        if (entity.hasGravity) {
            entity.velocity.y += -gravity / 4.0
        }

        // ToDo: Same as for water
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)
        val above = connection.world[blockPosition + Directions.UP]

        if (above != null) { // ToDo: Or is not a full block
            return
        }
        if (lavaParticleType != null && random.chance(1)) {
            val position = blockPosition.toVec3d + Vec3d.horizontal(
                { random.nextDouble() },
                1.0
            )

            connection.world += LavaParticle(connection, position, lavaParticleType.default())
        }
    }

}
