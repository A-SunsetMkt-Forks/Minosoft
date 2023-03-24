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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions

object BakingUtil {

    fun positions(direction: Directions, from: Vec3, to: Vec3): FloatArray {
        return when (direction) {
            // @formatter:off
            Directions.DOWN ->  floatArrayOf(from.x, from.y, from.z, /**/  to.x,   from.y, from.z,  /**/  to.x,   from.y, to.z,   /**/  from.x, from.y, to.z   )
            Directions.UP ->    floatArrayOf(from.x, to.y,   from.z, /**/  from.x, to.y,   to.z,    /**/  to.x,   to.y,   to.z,   /**/  to.x,   to.y,   from.z )
            Directions.NORTH -> floatArrayOf(from.x, from.y, from.z, /**/  to.x,   from.y, from.z,  /**/  to.x,   to.y,   from.z, /**/  from.x, to.y,   from.z )
            Directions.SOUTH -> floatArrayOf(from.x, from.y, to.z,   /**/  from.x, to.y,   to.z,    /**/  to.x,   to.y,   to.z,   /**/  to.x,   from.y, to.z   )
            Directions.WEST ->  floatArrayOf(from.x, from.y, from.z, /**/  from.x, to.y,   from.z,  /**/  from.x, to.y,   to.z,   /**/  from.x, from.y, to.z   )
            Directions.EAST ->  floatArrayOf(to.x,   from.y, from.z, /**/  to.x,   from.y, to.z,    /**/  to.x,   to.y,   to.z,   /**/  to.x,   to.y,   from.z )
            // @formatter:on
        }
    }

}
