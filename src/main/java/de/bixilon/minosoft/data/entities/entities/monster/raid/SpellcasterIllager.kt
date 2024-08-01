/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.monster.raid

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class SpellcasterIllager(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractIllager(session, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val spell: Spells
        get() = Spells.VALUES.getOrNull(data.get(SPELL_DATA, Spells.NONE.ordinal)) ?: Spells.NONE

    enum class Spells {
        NONE,
        SUMMON_VEX,
        FANGS,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS,
        ;

        companion object : ValuesEnum<Spells> {
            override val VALUES: Array<Spells> = values()
            override val NAME_MAP: Map<String, Spells> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object {
        private val SPELL_DATA = EntityDataField("SPELLCASTER_ILLAGER_SPELL")
    }
}
