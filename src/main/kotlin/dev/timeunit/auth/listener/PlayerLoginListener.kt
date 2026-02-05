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

package dev.timeunit.auth.listener

import dev.timeunit.auth.BungeeAuth
import dev.timeunit.auth.cache.LoginCache
import dev.timeunit.auth.mongo.user.User
import dev.timeunit.auth.util.CaptchaUtil
import dev.timeunit.auth.util.ColorUtil
import dev.timeunit.auth.util.Cooldown
import dev.timeunit.auth.util.TitleUtil
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

object PlayerLoginListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPostLogin(event: PostLoginEvent) {
        val player = event.player
        val username = player.name
        val id = player.uniqueId

        var user = BungeeAuth.instance.mongoManager.findUser(username)

        if (user == null) {
            user = createNewUser(id.toString(), username, player.address.address.hostAddress)
        }

        if (LoginCache.isPremiumUser(username)) {
            handlePremiumUser(player, user)
            LoginCache.removeUser(username)
        } else {
            handleNonPremiumUser(player, user)
            Cooldown.startAuthProcess(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        Cooldown.cancelAuthProcess(player.uniqueId)
        val user = BungeeAuth.instance.mongoManager.findUser(player.name)

        if (user != null && user.loggedIn) {
            user.loggedIn = false
            BungeeAuth.instance.mongoManager.saveUser(user)
        }

        LoginCache.removeUser(player.name)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSwitchServer(event: ServerConnectEvent) {
        if (event.player.server == null) {
            return
        }

        val user = BungeeAuth.instance.mongoManager.findUser(event.player.name)

        if (user == null || (!user.loggedIn && !user.isPremium)) {
            event.isCancelled = true
            event.player.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.login"))))

            if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
                val top = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.login.top"))
                val bottom = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.login.bottom"))

                TitleUtil.sendTitle(event.player, top, bottom)
            }
        }
    }

    private fun createNewUser(id: String, username: String, lastIP: String): User {
        val newUser = User(id = id, username = username, password = "", isPremium = false, resetPin = "", lastIP = lastIP, ips = mutableListOf(lastIP))
        BungeeAuth.instance.mongoManager.saveUser(newUser)
        return newUser
    }

    private fun handlePremiumUser(player: ProxiedPlayer, user: User) {
        user.loggedIn = true
        user.isPremium = true
        BungeeAuth.instance.mongoManager.saveUser(user)
        player.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.login-success-premium"))))

        if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
            val top = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.auto.top"))
            val bottom = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.auto.bottom"))

            TitleUtil.sendTitle(player, top, bottom)
        }
    }

    private fun handleNonPremiumUser(player: ProxiedPlayer, user: User) {
        user.loggedIn = false

        val captchaEnabled = BungeeAuth.instance.config.getBoolean("settings.captcha-enabled")
        if (captchaEnabled && user.password.isEmpty()) {
            val length = BungeeAuth.instance.config.getInt("settings.captcha-length")
            val code = CaptchaUtil.generateCaptcha(length)
            LoginCache.setCaptcha(player.name, code)
        }

        if (user.password.isEmpty()) {
            player.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.register").replace("%captcha%", LoginCache.getCaptcha(player.name) ?: "Error"))))

            if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
                val top = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.register.top"))
                val bottom = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.register.bottom").replace("%captcha%", LoginCache.getCaptcha(player.name) ?: "Error"))

                TitleUtil.sendTitle(player, top, bottom)
            }
        } else {
            player.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.login"))))

            if (BungeeAuth.instance.messages.getBoolean("titles.enabled")) {
                val top = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.login.top"))
                val bottom = ColorUtil.translate(BungeeAuth.instance.messages.getString("titles.login.bottom"))

                TitleUtil.sendTitle(player, top, bottom)
            }
        }
    }
}