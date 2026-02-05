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
import dev.timeunit.auth.util.CaptchaUtil
import dev.timeunit.auth.util.ColorUtil
import dev.timeunit.auth.util.WebHookUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

object GeneratePinCommand : Command("generatepin") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-only"))))
            return
        }

        val user = BungeeAuth.instance.mongoManager.findUser(sender.name)

        if (user == null) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-registered"))))
            return
        }

        if (!user.loggedIn) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-logged-in"))))
            return
        }

        if (user.isPremium) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.premium-user")))
            return
        }

        if (user.resetPin.isNotEmpty()) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.pin-already-generated"))))
            return
        }

        val pin = CaptchaUtil.generateCaptcha(BungeeAuth.instance.config.getInt("settings.pin-code-length"))

        user.resetPin = pin
        BungeeAuth.instance.mongoManager.saveUser(user)
        WebHookUtil.sendPinLog(sender, pin)

        val message = BungeeAuth.instance.messages.getString("messages.pin-generated")
        sender.sendMessage(TextComponent(ColorUtil.translate(message.replace("%pin%", pin))))
    }
}