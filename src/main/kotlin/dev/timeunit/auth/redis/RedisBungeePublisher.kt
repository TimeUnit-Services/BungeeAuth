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

package dev.timeunit.auth.redis

import com.google.gson.Gson
import dev.timeunit.auth.BungeeAuth
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.UUID

object RedisBungeePublisher {
    private val gson = Gson()
    private var pool: JedisPool? = null
    private const val PACKET_CLASS = "PACKET_CLASS:UUID_UPDATE"
    private const val CHANNEL = "RedisPacket:ALL"

    data class UUIDUpdatePayload(
        val playerName: String,
        val oldUuid: UUID,
        val newUuid: UUID
    )

    private fun getPool(): JedisPool {
        if (pool == null) {
            val config = BungeeAuth.instance.config
            val host = config.getString("redis.host")
            val port = config.getInt("redis.port")
            val password = config.getString("redis.password")

            val poolConfig = JedisPoolConfig()
            poolConfig.maxTotal = 8

            pool = if (password.isEmpty()) {
                JedisPool(poolConfig, host, port)
            } else {
                JedisPool(poolConfig, host, port, 2000, password)
            }
        }
        return pool!!
    }

    fun sendUUIDUpdate(playerName: String, oldUuid: UUID, newUuid: UUID) {
        BungeeAuth.instance.proxy.scheduler.runAsync(BungeeAuth.instance) {
            try {
                getPool().resource.use { jedis ->
                    val payload = UUIDUpdatePayload(playerName, oldUuid, newUuid)
                    val json = gson.toJson(payload)

                    val message = "$PACKET_CLASS||$json"

                    jedis.publish(CHANNEL, message)
                    BungeeAuth.instance.logger.info("Published UUID update for player $playerName to Redis.")
                }
            } catch (e: Exception) {
                BungeeAuth.instance.logger.warning("Failed to publish UUID update for player $playerName to Redis.")
                e.printStackTrace()
            }
        }
    }

    fun close() {
        pool?.close()
    }
}