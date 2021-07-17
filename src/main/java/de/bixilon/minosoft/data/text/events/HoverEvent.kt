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
package de.bixilon.minosoft.data.text.events

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.events.data.EntityHoverData
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import java.util.*

class HoverEvent {
    val action: HoverEventActions
    val value: Any

    constructor(json: JsonObject) {
        action = HoverEventActions.valueOf(json["action"].asString.uppercase(Locale.getDefault()))
        var data: JsonElement = json
        json["value"]?.let {
            data = it
        }
        json["contents"]?.let {
            data = it
        }
        this.value = when (action) {
            HoverEventActions.SHOW_TEXT -> ChatComponent.of(data)
            HoverEventActions.SHOW_ENTITY -> EntityHoverData.deserialize(data)
            else -> TODO("Don't know what todo with $action: $data")
        }
    }

    constructor(action: HoverEventActions, value: Any) {
        this.action = action
        this.value = value
    }

    enum class HoverEventActions {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY,
        SHOW_ACHIEVEMENT,
        ;

        companion object : ValuesEnum<HoverEventActions> {
            override val VALUES: Array<HoverEventActions> = values()
            override val NAME_MAP: Map<String, HoverEventActions> = KUtil.getEnumValues(VALUES)
        }
    }
}