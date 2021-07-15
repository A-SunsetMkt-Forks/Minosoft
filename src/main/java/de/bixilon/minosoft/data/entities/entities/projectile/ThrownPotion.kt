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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import glm_.vec3.Vec3d

class ThrownPotion(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : ThrowableItemProjectile(connection, entityType, position, rotation) {
    override val gravity: Float = 0.05f

    @EntityMetaDataFunction(name = "Item")
    override val item: ItemStack?
        get() = if (versionId > ProtocolVersions.V_20W09A) {
            super.item
        } else {
            entityMetaData.sets.getItemStack(EntityMetaDataFields.THROWN_POTION_ITEM) ?: defaultItem
        }

    override val defaultItem: ItemStack? = DEFAULT_ITEM

    companion object : EntityFactory<ThrownPotion> {
        private val DEFAULT_ITEM: ItemStack? = null
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("potion")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): ThrownPotion {
            return ThrownPotion(connection, entityType, position, rotation)
        }
    }
}
