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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream

interface AssetsManager {

    /**
     * All namespaces this assets-manager can provide
     */
    val namespaces: Set<String>

    /**
     * The icon of the assets-manager (probably the pack.png)
     */
    val image: ByteArray?
        get() = null

    /**
     * Various properties
     */
    val properties: AssetsManagerProperties?
        get() = null

    val loaded: Boolean

    /**
     * Returns the input stream of an asset or throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    operator fun get(path: ResourceLocation): InputStream

    /**
     * Returns the input stream of an asset or null
     */
    fun nullGet(path: ResourceLocation): InputStream?

    /**
     * Loads all assets
     */
    fun load(latch: CountUpAndDownLatch)

    /**
     * Deletes all assets from memory
     */
    fun unload()
}