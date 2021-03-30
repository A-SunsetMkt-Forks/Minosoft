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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketOpenBook extends ClientboundPacket {
    private final Hands hand;

    public PacketOpenBook(InByteBuffer buffer) {
        if (buffer.readVarInt() == 0) {
            this.hand = Hands.MAIN_HAND;
            return;
        }
        this.hand = Hands.OFF_HAND;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received open book packet (hand=%s)", this.hand));
    }

    public Hands getHand() {
        return this.hand;
    }
}
