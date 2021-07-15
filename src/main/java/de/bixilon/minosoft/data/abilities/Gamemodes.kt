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
package de.bixilon.minosoft.data.abilities

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class Gamemodes(
    val canBuild: Boolean,
    val canBreak: Boolean,
    val useTools: Boolean,
    val canInteract: InteractionAbilities,
) {
    SURVIVAL(
        canBuild = true,
        canBreak = true,
        useTools = true,
        canInteract = InteractionAbilities.EVERYTHING,
    ),
    CREATIVE(
        canBuild = true,
        canBreak = true,
        useTools = true,
        canInteract = InteractionAbilities.EVERYTHING,
    ),
    ADVENTURE(
        canBuild = false,
        canBreak = false,
        useTools = false,
        canInteract = InteractionAbilities.ONLY_ENTITIES,
    ),
    SPECTATOR(
        canBuild = false,
        canBreak = false,
        useTools = false,
        canInteract = InteractionAbilities.ONLY_ENTITIES,
    ),
    ;

    companion object : ValuesEnum<Gamemodes> {
        override val VALUES: Array<Gamemodes> = values()
        override val NAME_MAP: Map<String, Gamemodes> = KUtil.getEnumValues(VALUES)
    }
}
