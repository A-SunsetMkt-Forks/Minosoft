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

import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.abilities.Gamemodes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.RespawnS2CP;

public class RespawnEvent extends PlayConnectionEvent {
    private final Gamemodes gamemode;
    private final Dimension dimension;
    private final Difficulties difficulty;

    public RespawnEvent(PlayConnection connection, Gamemodes gamemode, Dimension dimension, Difficulties difficulty) {
        super(connection);
        this.gamemode = gamemode;
        this.dimension = dimension;
        this.difficulty = difficulty;
    }

    public RespawnEvent(PlayConnection connection, RespawnS2CP pkg) {
        super(connection);
        this.gamemode = pkg.getGamemode();
        this.dimension = pkg.getDimension();
        this.difficulty = pkg.getDifficulty();
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
}
