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

package dev.timeunit.auth.mongo.user

import org.bson.Document
import org.mindrot.jbcrypt.BCrypt

data class User(
    val id: String,
    val username: String,
    var password: String,
    var loggedIn: Boolean = false,
    var isPremium: Boolean = false,
    var resetPin: String,
    var lastPasswordChange: Long = 0,
    var lastIP: String = "",
    var ips: MutableList<String> = mutableListOf()
) {
    fun toDocument(): Document {
        return Document()
            .append("_id", id)
            .append("username", username)
            .append("password", password)
            .append("loggedIn", loggedIn)
            .append("isPremium", isPremium)
            .append("resetPin", resetPin)
            .append("lastPasswordChange", lastPasswordChange)
            .append("lastIP", lastIP)
            .append("ips", ips)
    }

    fun verifyPassword(inputPassword: String): Boolean {
        if (password.isEmpty()) return false
        return try {
            BCrypt.checkpw(inputPassword, password)
        } catch (e: Exception) {
            false
        }
    }

    @JvmName("changePassword")
    fun setPassword(newPassword: String) {
        password = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        lastPasswordChange = System.currentTimeMillis()
    }

    companion object {
        fun fromDocument(doc: Document): User {
            return User(
                id = doc.getString("_id") ?: "",
                username = doc.getString("username") ?: "",
                password = doc.getString("password") ?: "",
                loggedIn = doc.getBoolean("loggedIn", false),
                isPremium = doc.getBoolean("isPremium", false),
                resetPin = doc.getString("resetPin") ?: "",
                lastPasswordChange = doc.getLong("lastPasswordChange") ?: 0L,
                lastIP = doc.getString("lastIP") ?: "",
                ips = doc.getList("ips", String::class.java)?.toMutableList() ?: mutableListOf()
            )
        }
    }
}