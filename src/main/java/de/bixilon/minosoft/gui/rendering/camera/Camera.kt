/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.TargetHandler
import de.bixilon.minosoft.gui.rendering.camera.view.ViewManager
import de.bixilon.minosoft.gui.rendering.world.view.WorldVisibilityGraph

class Camera(
    val renderWindow: RenderWindow,
) {
    val fogManager = FogManager(renderWindow)
    val matrixHandler = MatrixHandler(renderWindow, fogManager, this)
    val targetHandler = TargetHandler(renderWindow, this)
    val visibilityGraph = WorldVisibilityGraph(renderWindow, this)

    val view = ViewManager(this)


    fun init() {
        matrixHandler.init()
        view.init()
    }

    fun draw() {
        val entity = matrixHandler.entity
        entity.tryTick()
        if (entity is LocalPlayerEntity) {
            entity._draw(millis())
        }
        view.draw()
        matrixHandler.draw()
        val latch = CountUpAndDownLatch(2)
        renderWindow.runAsync { visibilityGraph.draw();latch.dec() }
        renderWindow.runAsync { targetHandler.raycast();latch.dec() }
        fogManager.draw()
        latch.await()
    }
}
