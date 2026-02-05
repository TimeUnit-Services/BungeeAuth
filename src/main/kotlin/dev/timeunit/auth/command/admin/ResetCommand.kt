/*
 * Copyright (C) 2026 Jose Gambarte
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.timeunit.auth.command.admin

import dev.timeunit.auth.BungeeAuth
import dev.timeunit.auth.util.ColorUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command

object ResetCommand : Command("reset") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("bungeeauth.admin")) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-permission")))
            return
        }

        if (args.size != 1) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.reset-usage")))
            return
        }

        val targetPlayer = args[0]
        val user = BungeeAuth.instance.mongoManager.findUser(targetPlayer)

        if (user == null) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-not-found")))
            return
        }

        BungeeAuth.instance.mongoManager.deleteUser(targetPlayer)
        sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.reset-success").replace("%player%", targetPlayer)))

        if (ProxyServer.getInstance().getPlayer(targetPlayer) != null && ProxyServer.getInstance().getPlayer(targetPlayer).isConnected) {
            ProxyServer.getInstance().getPlayer(targetPlayer).disconnect(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.reset-disconnect")))
        }
    }
}