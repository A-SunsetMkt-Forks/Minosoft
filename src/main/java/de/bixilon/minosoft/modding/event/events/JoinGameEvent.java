/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event.events;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.abilities.Gamemodes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.JoinGameS2CP;

public class JoinGameEvent extends CancelableEvent {
    private final int entityId;
    private final boolean hardcore;
    private final Gamemodes gamemode;
    private final Dimension dimension;
    private final Difficulties difficulty;
    private final int viewDistance;
    private final int maxPlayers;
    private final boolean reducedDebugScreen;
    private final boolean enableRespawnScreen;
    private final long hashedSeed;
    private final HashBiMap<ResourceLocation, Dimension> dimensions;

    public JoinGameEvent(PlayConnection connection, int entityId, boolean hardcore, Gamemodes gamemode, Dimension dimension, Difficulties difficulty, int viewDistance, int maxPlayers, boolean reducedDebugScreen, boolean enableRespawnScreen, long hashedSeed, HashBiMap<ResourceLocation, Dimension> dimensions) {
        super(connection);
        this.entityId = entityId;
        this.hardcore = hardcore;
        this.gamemode = gamemode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.viewDistance = viewDistance;
        this.maxPlayers = maxPlayers;
        this.reducedDebugScreen = reducedDebugScreen;
        this.enableRespawnScreen = enableRespawnScreen;
        this.hashedSeed = hashedSeed;
        this.dimensions = dimensions;
    }

    public JoinGameEvent(PlayConnection connection, JoinGameS2CP pkg) {
        super(connection);
        this.entityId = pkg.getEntityId();
        this.hardcore = pkg.isHardcore();
        this.gamemode = pkg.getGamemode();
        this.dimension = pkg.getDimension();
        this.difficulty = pkg.getDifficulty();
        this.viewDistance = pkg.getViewDistance();
        this.maxPlayers = pkg.getMaxPlayers();
        this.reducedDebugScreen = pkg.isReducedDebugScreen();
        this.enableRespawnScreen = pkg.isEnableRespawnScreen();
        this.hashedSeed = pkg.getHashedSeed();
        this.dimensions = pkg.getDimensions();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public Gamemodes getGamemode() {
        return this.gamemode;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public Difficulties getDifficulty() {
        return this.difficulty;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isReducedDebugScreen() {
        return this.reducedDebugScreen;
    }

    public boolean isEnableRespawnScreen() {
        return this.enableRespawnScreen;
    }

    public long getHashedSeed() {
        return this.hashedSeed;
    }

    public HashBiMap<ResourceLocation, Dimension> getDimensions() {
        return this.dimensions;
    }
}
