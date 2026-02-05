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
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

object ResetPasswordCommand : Command("resetpassword", null, "resetpw") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-only"))))
            return
        }

        if (args.size != 2) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.reset-password-usage"))))
            return
        }

        val inputPin = args[0]
        val newPassword = args[1]
        val minLen = BungeeAuth.instance.config.getInt("settings.min-password-length")
        val maxLen = BungeeAuth.instance.config.getInt("settings.max-password-length")

        if (newPassword.length < minLen) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.password-too-short").replace("%length%", minLen.toString()))))
            return
        }

        if (newPassword.length > maxLen) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.password-too-long").replace("%length%", maxLen.toString()))))
            return
        }

        val user = BungeeAuth.instance.mongoManager.findUser(sender.name)

        if (user == null) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-registered"))))
            return
        }

        if (user.resetPin.isEmpty()) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-pin-set"))))
            return
        }

        if (user.isPremium) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.premium-user")))
            return
        }

        if (user.resetPin != inputPin) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.invalid-pin"))))
            return
        }


        user.setPassword(newPassword)
        user.loggedIn = true
        user.resetPin = ""
        BungeeAuth.instance.mongoManager.saveUser(user)

        sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.password-changed"))))
    }
}