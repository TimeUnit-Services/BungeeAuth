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
import dev.timeunit.auth.cache.LoginCache
import dev.timeunit.auth.mongo.user.User
import dev.timeunit.auth.util.ColorUtil
import dev.timeunit.auth.util.Cooldown
import dev.timeunit.auth.util.TitleUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

object RegisterCommand : Command("register") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-only")))
            return
        }

        if (!Cooldown.isAuthenticating(sender.uniqueId)) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-register-needed")))
            return
        }

        val captchaEnabled = BungeeAuth.instance.config.getBoolean("settings.captcha-enabled")

        if (captchaEnabled) {
            val storedCaptcha = LoginCache.getCaptcha(sender.name)

            if (args.size == 2) {
                val msg = BungeeAuth.instance.messages.getString("messages.register-captcha-needed")
                sender.sendMessage(ColorUtil.translate(msg.replace("%captcha%", storedCaptcha ?: "Error")))
                return
            } else if (args.size == 3) {
                if (storedCaptcha == null || args[2] != storedCaptcha) {
                    val msg = BungeeAuth.instance.messages.getString("messages.captcha-wrong")
                    sender.sendMessage(ColorUtil.translate(msg.replace("%captcha%", storedCaptcha ?: "Error")))
                    return
                }
            } else if (args.size != 3) {
                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.register-usage")))
                return
            }
        } else {
            if (args.size != 2) {
                sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.register-usage")))
                return
            }
        }

        val password = args[0]
        val confirmPassword = args[1]

        if (password != confirmPassword) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.passwords-not-match")))
            return
        }

        if (password.length < BungeeAuth.instance.config.getInt("settings.min-password-length")) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.password-too-short").replace("%length%", BungeeAuth.instance.config.getInt("settings.min-password-length").toString())))
            return
        }

        if (password.length > BungeeAuth.instance.config.getInt("settings.max-password-length")) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.password-too-long").replace("%length%", BungeeAuth.instance.config.getInt("settings.max-password-length").toString())))
            return
        }

        var user = BungeeAuth.instance.mongoManager.findUser(sender.name)

        if (user != null && user.password.isNotEmpty()) {
            sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.already-registered")))
            return
        }

        if (user == null) {
            user = User(id = sender.uniqueId.toString(), username = sender.name, password = "", resetPin = "")
        }

        user.setPassword(password)
        user.loggedIn = true

        BungeeAuth.instance.mongoManager.saveUser(user)
        Cooldown.cancelAuthProcess(sender.uniqueId)

        if (captchaEnabled) {
            LoginCache.removeCaptcha(sender.name)
        }

        sender.sendMessage(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.registration-success")))

        if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
            val top = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.success.top"))
            val bottom = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.success.bottom"))

            TitleUtil.sendTitle(sender, top, bottom)
        }
    }
}