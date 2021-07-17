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
package de.bixilon.minosoft.data.entities.entities.boss.wither

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.monster.Monster
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

class WitherBoss(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Monster(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Center head target entity id")
    val centerHeadTargetEntityId: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.WITHER_BOSS_CENTER_HEAD_TARGET_ENTITY_ID)

    @get:EntityMetaDataFunction(name = "Left head target entity id")
    val leftHeadTargetEntityId: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.WITHER_BOSS_LEFT_HEAD_TARGET_ENTITY_ID)

    @get:EntityMetaDataFunction(name = "Right head target entity id")
    val rightHeadTargetEntityId: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.WITHER_BOSS_RIGHT_HEAD_TARGET_ENTITY_ID)

    @get:EntityMetaDataFunction(name = "Invulnerable time")
    val invulnerableTime: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.WITHER_BOSS_INVULNERABLE_TIME)


    companion object : EntityFactory<WitherBoss> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("wither")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): WitherBoss {
            return WitherBoss(connection, entityType, position, rotation)
        }
    }
}