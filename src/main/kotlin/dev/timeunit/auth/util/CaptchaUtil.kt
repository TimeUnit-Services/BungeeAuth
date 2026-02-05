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

import java.security.SecureRandom

object CaptchaUtil {
    private val RANDOM = SecureRandom()
    private val CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    private val INTS = "0123456789".toCharArray()

    fun generateString(): String {
        return generateStringInternal(10, CHARS.size)
    }

    fun generateCaptcha(length: Int): String {
        return generateNumb(length)
    }

    fun generateHex(length: Int): String {
        return generateStringInternal(length, 16)
    }

    private fun generateNumb(length: Int): String {
        return buildString(length) {
            repeat(length) {
                append(INTS[RANDOM.nextInt(9)])
            }
        }
    }

    private fun generateStringInternal(length: Int, bound: Int): String {
        require(length >= 0) { "Length must be positive but was $length" }
        return buildString(length) {
            repeat(length) {
                append(CHARS[RANDOM.nextInt(bound)])
            }
        }
    }
}