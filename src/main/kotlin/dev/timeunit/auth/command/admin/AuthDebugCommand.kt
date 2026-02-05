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
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import java.text.SimpleDateFormat
import java.util.*

object AuthDebugCommand : Command("authdebug") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("bungeeauth.admin")) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-permission"))))
            return
        }

        if (args.size != 1) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.debug-usage"))))
            return
        }

        val targetPlayer = args[0]

        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            val user = BungeeAuth.instance.mongoManager.findUser(targetPlayer)

            if (user == null) {
                sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-not-found"))))
                return@runAsync
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val dateString = if (user.lastPasswordChange > 0) {
                dateFormat.format(Date(user.lastPasswordChange))
            } else {
                "Date not available"
            }

            val debugMessages = BungeeAuth.instance.messages.getStringList("messages.debug")

            for (line in debugMessages) {
                val formattedLine = line
                    .replace("%player%", user.username)
                    .replace("%uuid%", user.id)
                    .replace("%password%", user.password)
                    .replace("%logged_in%", if (user.loggedIn) "Yes" else "No")
                    .replace("%premium%", if (user.isPremium) "Yes" else "No")
                    .replace("%reset_pin%", user.resetPin.ifEmpty { "None" })
                    .replace("%last_changed%", dateString)
                    .replace("%last_ip%", user.lastIP.ifEmpty { "None" })
                    .replace("%ips%", if (user.ips.isNotEmpty()) user.ips.joinToString(", ") else "None")

                sender.sendMessage(TextComponent(ColorUtil.translate(formattedLine)))
            }
        }
    }
}