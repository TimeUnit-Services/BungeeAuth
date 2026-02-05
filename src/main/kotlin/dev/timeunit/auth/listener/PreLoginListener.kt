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
import dev.timeunit.auth.util.ColorUtil
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.HttpURLConnection
import java.net.URL

object PreLoginListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPreLogin(event: PreLoginEvent) {
        if (event.isCancelled) return
        event.registerIntent(BungeeAuth.instance)
        val connection = event.connection
        val username = connection.name
        val userIp = connection.address.address.hostAddress

        if (!isValidName(username)) {
            event.isCancelled = true
            event.cancelReason = ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.invalid-username"))
            return
        }

        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            try {
                val user = BungeeAuth.instance.mongoManager.findUser(username)

                if (user != null && user.isPremium) {
                    var updated = false

                    if (user.lastIP != userIp) {
                        user.lastIP = userIp
                        updated = true
                    }

                    if (!user.ips.contains(userIp)) {
                        user.ips.add(userIp)
                        updated = true
                    }

                    if (updated) {
                        BungeeAuth.instance.mongoManager.saveUser(user)
                    }

                    activatePremiumMode(connection, username)
                } else if (user == null) {
                    if (checkMojangPremium(username)) {
                        activatePremiumMode(connection, username)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                event.completeIntent(BungeeAuth.instance)
            }
        }
    }

    private fun isValidName(username: String): Boolean {
        val regexPattern = BungeeAuth.instance.config.getString("settings.name-regex") ?: "^[a-zA-Z0-9_]*"
        return username.length in 3..16
                && username.matches(Regex(regexPattern))
                && !username.contains("$")
                && !username.contains(" ")
                && !username.contains("-")
    }

    private fun activatePremiumMode(connection: PendingConnection, username: String) {
        connection.isOnlineMode = true
        LoginCache.addPremiumUser(username)
    }

    private fun checkMojangPremium(username: String): Boolean {
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }
}