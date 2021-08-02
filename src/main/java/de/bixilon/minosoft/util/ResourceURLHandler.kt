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

package de.bixilon.minosoft.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory


object ResourceURLHandler {

    init {
        URL.setURLStreamHandlerFactory(ResourceURLStreamHandlerFactory)
    }


    private object ResourceStreamHandler : URLStreamHandler() {
        override fun openConnection(url: URL?): URLConnection {
            return ResourceURLConnection(url)
        }
    }


    private object ResourceURLStreamHandlerFactory : URLStreamHandlerFactory {

        override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
            if (protocol == "resource") {
                return ResourceStreamHandler
            }
            return null
        }
    }

    private class ResourceURLConnection(url: URL?) : URLConnection(url) {
        override fun connect() {
        }

        override fun getInputStream(): InputStream {
            return Minosoft.MINOSOFT_ASSETS_MANAGER.readAssetAsStream(ResourceLocation(url.path))
        }
    }
}