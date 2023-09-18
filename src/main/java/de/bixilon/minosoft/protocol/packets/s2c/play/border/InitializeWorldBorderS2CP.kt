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

package de.bixilon.minosoft.protocol.packets.s2c.play.border

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class InitializeWorldBorderS2CP(buffer: PlayInByteBuffer) : WorldBorderS2CP {
    val center = buffer.readVec2d()
    val oldRadius = buffer.readDouble() / 2.0
    val newRadius = buffer.readDouble() / 2.0
    val millis = buffer.readVarLong()
    val portalBound = buffer.readVarInt()
    val warningTime = buffer.readVarInt()
    val warningBlocks = buffer.readVarInt()

    override fun handle(connection: PlayConnection) {
        connection.world.border.center = center
        connection.world.border.interpolate(oldRadius, newRadius, millis)
        connection.world.border.portalBound = portalBound
        connection.world.border.warningTime = warningTime
        connection.world.border.warningBlocks = warningBlocks
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Initialize world border (center=$center, oldRadius=$oldRadius, newRadius=$newRadius, speed=$millis, portalBound=$portalBound, warningTime=$warningTime, warningBlocks=$warningBlocks)" }
    }
}
