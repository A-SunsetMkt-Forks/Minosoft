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

import de.bixilon.minosoft.data.Tag;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class PacketTags extends ClientboundPacket {
    Tag[] blockTags;
    Tag[] itemTags;
    Tag[] fluidTags;
    Tag[] entityTags;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.blockTags = readTags(buffer);
        this.itemTags = readTags(buffer);
        this.fluidTags = readTags(buffer); // ToDo: when was this added? Was not available in 18w01
        if (buffer.getVersionId() >= 440) {
            this.entityTags = readTags(buffer);
        }
        return true;
    }

    private Tag[] readTags(InByteBuffer buffer) {
        Tag[] ret = new Tag[buffer.readVarInt()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Tag(buffer.readString(), buffer.readVarIntArray());
        }
        return ret;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received tags (blockLength=%d, itemLength=%d, fluidLength=%d, entityLength=%d)", this.blockTags.length, this.itemTags.length, this.fluidTags.length, ((this.entityTags == null) ? 0 : this.entityTags.length)));
    }
}
