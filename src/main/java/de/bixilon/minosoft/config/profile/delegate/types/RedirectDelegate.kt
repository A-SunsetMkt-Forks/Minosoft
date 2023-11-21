/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.delegate.types

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.minosoft.config.profile.delegate.AbstractProfileDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import kotlin.reflect.KProperty

@JsonDeserialize(using = RedirectDelegate.RedirectDeserializer::class)
class RedirectDelegate<V, S>(
    override val profile: Profile,
    val serializer: (V?) -> S?,
    val lookup: (S?) -> V?,
) : DataObserver<V?>(null), AbstractProfileDelegate<V?> {


    override fun setValue(thisRef: Any, property: KProperty<*>, value: V?) {
        super.setValue(thisRef, property, value)
        invalidate()
    }

    //   fun setValue(thisRef: Any, property: KProperty<*>, value: S?) {
//       setValue(thisRef, property, lookup(value))
//   }

    object RedirectDeserializer : StdDeserializer<Any>(Any::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Any {
            return "test"
        }
    }

}
