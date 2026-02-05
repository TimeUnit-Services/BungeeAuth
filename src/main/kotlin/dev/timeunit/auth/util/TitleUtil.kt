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
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

object TitleUtil {

    fun sendTitle(player: ProxiedPlayer, title: String, subTitle: String) {
        val titleObj = ProxyServer.getInstance().createTitle()

        if (title.isNotEmpty()) {
            titleObj.title(TextComponent(ColorUtil.translate(title)))
        }

        if (subTitle.isNotEmpty()) {
            titleObj.subTitle(TextComponent(ColorUtil.translate(subTitle)))
        }


        titleObj.fadeIn(BungeeAuth.instance.messages.getInt("titles.fade-in"))
        titleObj.fadeOut(BungeeAuth.instance.messages.getInt("titles.fade-out"))
        titleObj.stay(BungeeAuth.instance.messages.getInt("titles.stay"))

        titleObj.send(player)
    }
}