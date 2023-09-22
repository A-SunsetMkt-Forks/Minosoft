/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.modding.event.events

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.spawn.*

@Deprecated("Observables")
class EntitySpawnEvent(
    connection: PlayConnection,
    val entity: Entity,
) : PlayConnectionEvent(connection) {

    constructor(connection: PlayConnection, packet: EntityMobSpawnS2CP) : this(connection, packet.entity)

    constructor(connection: PlayConnection, packet: GlobalEntitySpawnS2CP) : this(connection, packet.entity)

    constructor(connection: PlayConnection, packet: EntityPlayerS2CP) : this(connection, packet.entity)

    constructor(connection: PlayConnection, packet: EntityExperienceOrbS2CP) : this(connection, packet.entity)

    constructor(connection: PlayConnection, packet: EntityObjectSpawnS2CP) : this(connection, packet.entity)

    constructor(connection: PlayConnection, packet: EntityPaintingS2CP) : this(connection, packet.entity)
}
