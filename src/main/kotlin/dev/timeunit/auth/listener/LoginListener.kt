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

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.nio.charset.StandardCharsets
import java.util.*

object LoginListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onLogin(event: LoginEvent) {
        if (event.isCancelled) return
        val connection = event.connection

        if (!connection.isOnlineMode) {
            val offlineUUID = generateOfflineUUID(connection.name)

            try {
                val uniqueIdField = InitialHandler::class.java.getDeclaredField("uniqueId")
                uniqueIdField.isAccessible = true
                uniqueIdField.set(connection, offlineUUID)
                println("[BungeeAuth] UUID Offline ${connection.name}: $offlineUUID")
            } catch (e: Exception) {
                e.printStackTrace()
                event.setCancelReason("${ChatColor.RED}Internal error generating UUID.")
                event.isCancelled = true
            }
        }
    }

    private fun generateOfflineUUID(username: String): UUID {
        return UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))
    }
}