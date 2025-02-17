/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.commands

import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.util.crash.freeze.FreezeDumpUtil

object DumpCommand : Command {
    override var node = LiteralNode("dump").addChild(
        LiteralNode("freeze", executor = { stack ->
            FreezeDumpUtil.catchAsync {
                if (it.path == null) {
                    stack.print.print("§4Failed to create freeze dump!")
                    stack.print.print(it.dump)
                } else {
                    stack.print.print(BaseComponent(TextComponent("Freeze dump created and saved at ").color(ChatColors.DARK_RED), TextComponent(it.path).color(ChatColors.YELLOW).clickEvent(OpenFileClickEvent(it.path))))
                }
            }
        }),
    )
}
