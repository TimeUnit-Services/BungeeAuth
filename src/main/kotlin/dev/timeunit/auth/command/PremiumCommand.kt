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

import com.google.gson.JsonParser
import dev.timeunit.auth.BungeeAuth
import dev.timeunit.auth.redis.RedisBungeePublisher
import dev.timeunit.auth.util.ColorUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object PremiumCommand : Command("premium") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.player-only"))))
            return
        }

        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            val user = BungeeAuth.instance.mongoManager.findUser(sender.name)

            if (user == null) {
                sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-registered"))))
                return@runAsync
            }

            if (!user.loggedIn) {
                sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.not-logged-in"))))
                return@runAsync
            }

            val onlineUUID = getMojangUUID(sender.name)

            if (onlineUUID == null) {
                sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.no-premium-user"))))
                return@runAsync
            }

            val offlineUUID = sender.uniqueId
            user.isPremium = true
            BungeeAuth.instance.mongoManager.saveUser(user)
            RedisBungeePublisher.sendUUIDUpdate(sender.name, offlineUUID, onlineUUID)

            sender.sendMessage(TextComponent(ColorUtil.translate(BungeeAuth.instance.messages.getString("messages.premium-success"))))
        }
    }

    private fun getMojangUUID(username: String): UUID? {
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = InputStreamReader(connection.inputStream)
                val jsonElement = JsonParser.parseReader(reader)
                val jsonObject = jsonElement.asJsonObject

                val idStr = jsonObject.get("id").asString

                val formattedId = StringBuilder(idStr)
                    .insert(8, "-")
                    .insert(13, "-")
                    .insert(18, "-")
                    .insert(23, "-")
                    .toString()

                UUID.fromString(formattedId)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}