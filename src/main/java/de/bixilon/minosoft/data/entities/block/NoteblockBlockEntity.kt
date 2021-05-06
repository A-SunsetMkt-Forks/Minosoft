/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.properties.Instruments
import de.bixilon.minosoft.protocol.network.connection.PlayConnection

class NoteblockBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    var instrument: Instruments? = null
        private set
    var pitch: Int? = null
        private set

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        instrument = when (data1.toInt()) {
            0 -> Instruments.HARP
            1 -> Instruments.BASS
            2 -> Instruments.SNARE
            3 -> Instruments.BANJO // ToDo: Was CLICKS_STICKS before
            4 -> Instruments.BASE_DRUM
            else -> null
        }

        pitch = data2.toInt()
    }

    companion object : BlockEntityFactory<NoteblockBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:noteblock")

        override fun build(connection: PlayConnection): NoteblockBlockEntity {
            return NoteblockBlockEntity(connection)
        }
    }

}
