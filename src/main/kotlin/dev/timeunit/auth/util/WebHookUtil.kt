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
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object WebHookUtil {

    fun sendPinLog(player: ProxiedPlayer, pin: String) {
        val webhookUrl = BungeeAuth.instance.config.getString("settings.discord-webhook-url")
        if (webhookUrl.isEmpty() || webhookUrl == "https://discord.com/api/webhooks/XXXXXXXX/XXXXXXXXXXXX") return

        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            try {
                val uuid = player.uniqueId.toString()
                val name = player.name
                val ip = player.pendingConnection.address.address.hostAddress
                val jsonPayload = """
                {
                  "username": "Auth Security",
                  "avatar_url": "https://minotar.net/helm/$name/100.png",
                  "embeds": [
                    {
                      "title": "🔐 PIN Generated",
                      "color": 16763904,
                      "fields": [
                        {
                          "name": "Player",
                          "value": "$name",
                          "inline": true
                        },
                        {
                          "name": "UUID",
                          "value": "$uuid",
                          "inline": true
                        },
                        {
                          "name": "IP",
                          "value": "$ip",
                          "inline": true
                        },
                        {
                          "name": "PIN",
                          "value": "||$pin||", 
                          "inline": false
                        }
                      ],
                      "footer": {
                        "text": "Auth Security System • ${java.time.Instant.now()}"
                      }
                    }
                  ]
                }
                """.trimIndent()

                val url = URL(webhookUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("User-Agent", "BungeeAuth-Plugin")

                val out = connection.outputStream
                out.write(jsonPayload.toByteArray(StandardCharsets.UTF_8))
                out.close()
                connection.inputStream.close()
                connection.disconnect()
            } catch (e: Exception) {
                BungeeAuth.instance.logger.warning("Error sending discord webhook: ${e.message}")
            }
        }
    }
}