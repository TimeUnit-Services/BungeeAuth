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

package dev.timeunit.auth.command

import dev.timeunit.auth.BungeeAuth
import dev.timeunit.auth.util.ColorUtil
import dev.timeunit.auth.util.Cooldown
import dev.timeunit.auth.util.TitleUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import java.text.SimpleDateFormat
import java.util.*

object LoginCommand : Command("login") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-only")))
            return
        }

        if (!Cooldown.isAuthenticating(sender.uniqueId)) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-login-needed")))
            return
        }

        if (args.size != 1) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.login-usage")))
            return
        }

        val password = args[0]
        val username = sender.name

        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            val user = BungeeAuth.instance.mongoManager.findUser(username)

            if (user == null) {
                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-registered")))
                return@runAsync
            }

            if (user.loggedIn) {
                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.already-logged-in")))
                return@runAsync
            }

            if (user.verifyPassword(password)) {
                user.loggedIn = true
                BungeeAuth.instance.mongoManager.saveUser(user)
                Cooldown.cancelAuthProcess(sender.uniqueId)
                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.login-success")))

                if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
                    val top = BungeeAuth.instance.messages.getString("titles.success.top")
                    val bottom = BungeeAuth.instance.messages.getString("titles.success.bottom")
                    TitleUtil.sendTitle(sender, top, bottom)
                }
            } else {
                var dateString = "Date not available"

                if (user.lastPasswordChange != 0L) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                    dateString = sdf.format(Date(user.lastPasswordChange))
                }

                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.incorrect-password").replace("%last_changed%", dateString)))
            }
        }
    }
}