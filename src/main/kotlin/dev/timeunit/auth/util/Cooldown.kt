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

package dev.timeunit.auth.util

import dev.timeunit.auth.BungeeAuth
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object Cooldown {
    private val authTasks = ConcurrentHashMap<UUID, ScheduledTask>()
    private const val LOGIN_TIMEOUT = 30L
    private const val REGISTER_TIMEOUT = 60L

    fun startAuthProcess(player: ProxiedPlayer) {
        val uuid = player.uniqueId
        val user = BungeeAuth.instance.mongoManager.findUser(uuid)

        if (user == null) {
            scheduleKick(player, REGISTER_TIMEOUT, "register")
        } else if (!user.loggedIn) {
            scheduleKick(player, LOGIN_TIMEOUT, "login")
        }
    }

    private fun scheduleKick(player: ProxiedPlayer, delay: Long, action: String) {
        val task = BungeeAuth.instance.proxy.scheduler.schedule(BungeeAuth.instance, {
            if (player.isConnected) {
                val message = if (action == "register") {
                    BungeeAuth.instance.messages.getString("messages.register-timeout")
                } else {
                    BungeeAuth.instance.messages.getString("messages.login-timeout")
                }
                player.disconnect(ColorUtil.translate(message))
            }
            authTasks.remove(player.uniqueId)
        }, delay, TimeUnit.SECONDS)

        authTasks[player.uniqueId] = task
    }

    fun cancelAuthProcess(uuid: UUID) {
        authTasks.remove(uuid)?.cancel()
    }

    fun isAuthenticating(uuid: UUID): Boolean {
        return authTasks.containsKey(uuid)
    }
}