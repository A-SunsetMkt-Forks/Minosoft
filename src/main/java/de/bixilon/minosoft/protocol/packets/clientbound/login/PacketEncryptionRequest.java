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

package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class PacketEncryptionRequest extends ClientboundPacket {

    String serverId; // normally empty
    byte[] publicKey;
    byte[] verifyToken;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.serverId = buffer.readString();
        this.publicKey = buffer.readByteArray();
        this.verifyToken = buffer.readByteArray();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(getServerId(), publicKey, secretKey)).toString(16);
        connection.getPlayer().getAccount().join(serverHash);
        connection.sendPacket(new PacketEncryptionResponse(secretKey, getVerifyToken(), publicKey));
    }

    @Override
    public void log() {
        Log.protocol("[IN] Receiving encryption request packet");
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public byte[] getVerifyToken() {
        return this.verifyToken;
    }

    public String getServerId() {
        return this.serverId;
    }
}
