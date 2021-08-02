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

package de.bixilon.minosoft.modding.event.master

import de.bixilon.minosoft.modding.event.EventInstantFire
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.invoker.EventInstantFireable
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.modding.event.invoker.OneShotInvoker
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import kotlin.reflect.full.companionObjectInstance

open class EventMaster(vararg parents: AbstractEventMaster) : AbstractEventMaster {
    val parents: MutableSet<AbstractEventMaster> = synchronizedSetOf(*parents)
    private val eventInvokers: MutableList<EventInvoker> = mutableListOf<EventInvoker>().sortedWith { a: EventInvoker, b: EventInvoker ->
        -(b.priority.ordinal - a.priority.ordinal)
    }.toSynchronizedList()

    override val size: Int
        get() {
            var size = eventInvokers.size
            for (parent in parents) {
                size += parent.size
            }
            return size
        }


    override fun fireEvent(event: Event): Boolean {
        for (parent in parents.toSynchronizedSet()) {
            parent.fireEvent(event)
        }

        for (invoker in eventInvokers.toSynchronizedList()) {
            if (!invoker.eventType.isAssignableFrom(event::class.java)) {
                continue
            }
            invoker(event)

            if (invoker is OneShotInvoker && invoker.oneShot) {
                eventInvokers -= invoker
            }
        }


        if (event is CancelableEvent) {
            val cancelled = event.cancelled
            event.cancelled = false // Cleanup memory
            return cancelled
        }
        return false
    }

    override fun unregisterEvent(invoker: EventInvoker?) {
        eventInvokers -= invoker ?: return
    }

    override fun <T : EventInvoker> registerEvent(invoker: T): T {
        eventInvokers += invoker

        if (invoker is EventInstantFireable && invoker.instantFire) {
            val companion = invoker.kEventType?.companionObjectInstance ?: return invoker

            if (companion is EventInstantFire<*>) {
                invoker.invoke(companion.fire())
            }
        }
        return invoker
    }


    override fun iterator(): Iterator<EventInvoker> {
        return eventInvokers.toSynchronizedList().iterator()
    }
}