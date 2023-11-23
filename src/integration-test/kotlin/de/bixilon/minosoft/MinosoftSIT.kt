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

package de.bixilon.minosoft

import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.WindowFactory
import de.bixilon.minosoft.gui.rendering.system.window.dummy.DummyWindow
import de.bixilon.minosoft.main.BootTasks
import de.bixilon.minosoft.main.MinosoftBoot
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.minusAssign
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.testng.annotations.BeforeSuite
import java.nio.file.Path


internal object MinosoftSIT {

    private fun setupEnv() {
        Log.ASYNC_LOGGING = false
        RunConfiguration.VERBOSE_LOGGING = true
        RunConfiguration.APPLICATION_NAME = "Minosoft it"

        val isCi = (System.getenv("GITHUB_ACTIONS") ?: System.getenv("TRAVIS") ?: System.getenv("CIRCLECI") ?: System.getenv("GITLAB_CI")) != null // TODO: kutil 1.25 Environment.isInCI()
        if (isCi) {
            RunConfiguration::HOME_DIRECTORY.forceSet(Path.of("./it"))
        }
        RunConfiguration::CONFIG_DIRECTORY.forceSet(Path.of(System.getProperty("java.io.tmpdir"), "minosoft").resolve("conf"))
        RunConfiguration.PROFILES_HOT_RELOADING = false

        WindowFactory.factory = DummyWindow
        RenderSystemFactory.factory = DummyRenderSystem
    }

    @BeforeSuite
    fun setup() {
        setupEnv()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "This is java version ${System.getProperty("java.version")}" }
        KUtil.initBootClasses()
        KUtil.initPlayClasses()
        disableGC()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Setting up integration tests...." }

        IntegratedAssets.DEFAULT.load()

        val worker = TaskWorker()
        MinosoftBoot.register(worker)
        worker.minusAssign(BootTasks.PROFILES)
        worker.minusAssign(BootTasks.LAN_SERVERS)
        worker.minusAssign(BootTasks.MODS)
        worker.minusAssign(BootTasks.CLI)
        worker.work(MinosoftBoot.LATCH)
        MinosoftBoot.LATCH.dec()
        MinosoftBoot.LATCH.await()

        loadPixlyzerData()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Integration tests setup successfully!" }
    }


    @Deprecated("Not sure if that is needed")
    fun disableGC() {
        Thread {
            val references = IT.references
            // basically while (true)
            for (i in 0 until Int.MAX_VALUE) {
                Thread.sleep(100000L)
            }
            references.hashCode() // force keep reference to references
        }.start()
    }

    fun loadPixlyzerData() {
        val (version, registries) = ITUtil.loadPixlyzerData(IT.TEST_VERSION_NAME)
        IT.VERSION = version
        IT.REGISTRIES = registries
        IT.FALLBACK_TAGS = FallbackTags.map(registries)
    }
}
