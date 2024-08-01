/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.animal.water

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class PufferFish(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractFish(session, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val puffState: Int
        get() = data.get(PUFF_STATE_DATA, 0)


    companion object : EntityFactory<PufferFish> {
        override val identifier: ResourceLocation = minecraft("pufferfish")
        private val PUFF_STATE_DATA = EntityDataField("PUFFERFISH_PUFF_STATE")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): PufferFish {
            return PufferFish(session, entityType, data, position, rotation)
        }
    }
}
