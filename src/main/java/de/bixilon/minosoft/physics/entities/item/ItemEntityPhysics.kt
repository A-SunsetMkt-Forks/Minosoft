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

package de.bixilon.minosoft.physics.entities.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.FrictionBlock
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.EntityPhysics

class ItemEntityPhysics(entity: ItemEntity) : EntityPhysics<ItemEntity>(entity) {


    private fun updateFluidVelocity(friction: Float) {
        val velocity = this.velocity
        this.velocity = Vec3d(
            velocity.x * friction,
            velocity.y + if (this.velocity.y < 0.06f) 5.0E-4f else 0.0f,
            velocity.z * friction,
        )
    }

    private fun updateVelocity() {
        val minLevel = entity.eyeHeight - Fluid.MIN_LEVEL

        for ((fluid, height) in submersion.heights) {
            if (height <= minLevel) continue
            updateFluidVelocity(fluid.friction)
            return
        }

        if (!entity.hasGravity) return
        if (onGround && this.velocity.y == GRAVITY) return

        this.velocity = velocity + GRAVITY_MOVEMENT
    }

    private fun updateFriction() {
        var friction = PhysicsConstants.AIR_RESISTANCEf
        if (onGround) {
            val frictionPosition = (position + FRICTION_OFFSET).blockPosition.inChunkPosition
            friction *= positionInfo.chunk?.get(frictionPosition)?.block?.nullCast<FrictionBlock>()?.friction ?: FrictionBlock.DEFAULT_FRICTION
        }
        this.velocity = this.velocity * Vec3d(friction, PhysicsConstants.AIR_RESISTANCE, friction)
    }

    private fun boost() {
        if (!onGround) return

        val velocity = this.velocity
        if (velocity.y < 0.0) {
            this.velocity = Vec3d(velocity.x, velocity.y * -0.5, velocity.z)
        }
    }

    override fun tick() {
        // This is not 100% vanilla, but performance optimized
        if (position.y < entity.session.world.dimension.minY - 20.0) return // ignore out of world entities

        updateVelocity()
        if (onGround && this.velocity.xz.length2() <= 9.999999747378752E-6 && entity.age % 4 != 0) return

        move(this.velocity)

        updateFriction()
        boost()
    }

    companion object {
        const val GRAVITY = -0.04
        val GRAVITY_MOVEMENT = Vec3d(0.0, GRAVITY, 0.0)
        val FRICTION_OFFSET = Vec3d(0, -1, 0)

        val Fluid.friction: Float
            get() = when (this) {
                is WaterFluid -> 0.99f
                is LavaFluid -> 0.95f
                else -> 1.0f
            }
    }
}
