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

package dev.timeunit.auth.cache

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object LoginCache {
    private val premiumUsers = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val captchaCache = ConcurrentHashMap<String, String>()

    fun addPremiumUser(username: String) {
        premiumUsers.add(username.lowercase())
    }

    fun isPremiumUser(username: String): Boolean {
        return premiumUsers.contains(username.lowercase())
    }

    fun removeUser(username: String) {
        premiumUsers.remove(username.lowercase())
    }

    fun setCaptcha(username: String, code: String) {
        captchaCache[username.lowercase()] = code
    }

    fun getCaptcha(username: String): String? {
        return captchaCache[username.lowercase()]
    }

    fun removeCaptcha(username: String) {
        captchaCache.remove(username.lowercase())
    }
}