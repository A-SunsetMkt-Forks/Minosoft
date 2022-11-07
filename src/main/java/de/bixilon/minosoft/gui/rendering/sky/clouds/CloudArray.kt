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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2i
import java.util.*

class CloudArray(
    val clouds: CloudsRenderer,
    val offset: Vec2i,
) {
    private val mesh: CloudMesh = CloudMesh(clouds.renderWindow)

    init {
        build()
        mesh.load()
    }

    private fun BitSet.getCloud(x: Int, z: Int): Boolean {
        val offset = (x and CloudsRenderer.CLOUD_MATRIX_MASK) + (z and CloudsRenderer.CLOUD_MATRIX_MASK) * CloudsRenderer.CLOUD_MATRIX_SIZE
        return get(offset)
    }

    private fun build() {
        val matrix = clouds.cloudMatrix
        val matrixOffset = (offset * ARRAY_SIZE) and 0xFF

        for (z in 0 until ARRAY_SIZE) {
            for (x in 0 until ARRAY_SIZE) {
                val matrixX = matrixOffset.x + x
                val matrixZ = matrixOffset.y + z

                if (!matrix.getCloud(matrixX, matrixZ)) {
                    continue
                }

                val start = (this@CloudArray.offset * ARRAY_SIZE + Vec2i(x, z)) * CLOUD_SIZE

                val cull = booleanArrayOf(
                    matrix.getCloud(matrixX + 0, matrixZ - 1), // NORTH
                    matrix.getCloud(matrixX + 0, matrixZ + 1), // SOUTH
                    matrix.getCloud(matrixX - 1, matrixZ + 0), // WEST
                    matrix.getCloud(matrixX + 1, matrixZ + 0), // EAST
                )
                mesh.createCloud(start, start + CLOUD_SIZE, 0, 4f, cull)
            }
        }
    }


    fun draw() {
        mesh.draw()
    }

    fun unload() {}

    companion object {
        const val CLOUD_SIZE = 16
        const val ARRAY_SIZE = 16
    }
}
