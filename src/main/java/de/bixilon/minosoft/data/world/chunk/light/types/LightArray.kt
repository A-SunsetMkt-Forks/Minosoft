/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light.types

import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

@JvmInline
value class LightArray(inline val array: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION)) {

    inline operator fun get(position: InSectionPosition) = LightLevel(array[position.index])
    inline operator fun set(position: InSectionPosition, value: LightLevel) {
        array[position.index] = value.raw
    }

    inline fun clear() = Arrays.fill(array, 0.toByte())
}
