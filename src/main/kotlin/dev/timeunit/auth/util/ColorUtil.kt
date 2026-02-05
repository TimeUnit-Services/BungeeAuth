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

import net.md_5.bungee.api.ChatColor


object ColorUtil {

    fun translate(str: String): String {
        return ChatColor.translateAlternateColorCodes('&', str)
    }

    fun translate(lines: List<String>): List<String> {
        val toReturn = ArrayList<String>()

        for (line in lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line))
        }

        return toReturn
    }

    fun translate(lines: Array<String>): List<String> {
        val toReturn = ArrayList<String>()

        for (line in lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line))
        }

        return toReturn
    }
}