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

package de.bixilon.minosoft.data.commands.parser.properties;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;

public class RangeParserProperties implements ParserProperties {
    private final boolean allowDecimals;

    public RangeParserProperties(InByteBuffer buffer) {
        this.allowDecimals = BitByte.isBitMask(buffer.readByte(), 0x01);
    }

    public RangeParserProperties(boolean allowDecimals) {
        this.allowDecimals = allowDecimals;
    }

    public boolean isAllowDecimals() {
        return this.allowDecimals;
    }
}
