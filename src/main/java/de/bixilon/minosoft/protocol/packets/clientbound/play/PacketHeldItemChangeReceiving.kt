/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.modding.event.events.HeldItemChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.logging.Log

class PacketHeldItemChangeReceiving() : ClientboundPacket() {
    var slot = 0
        private set

    constructor(buffer: InByteBuffer) : this() {
        slot = buffer.readByte().toInt()
    }

    override fun handle(connection: Connection) {
        connection.fireEvent(HeldItemChangeEvent(connection, slot))

        connection.player.selectedSlot = slot
    }

    override fun log() {
        Log.protocol("[IN] Slot change received. Now on slot $slot")
    }
}
