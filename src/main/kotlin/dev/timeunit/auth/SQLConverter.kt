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

package dev.timeunit.auth
/*
import dev.timeunit.auth.mongo.MongoManager
import dev.timeunit.auth.mongo.user.User
import kotlinx.coroutines.*
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

object SQLConverter {
    private lateinit var mongoManager: MongoManager
    private const val BATCH_SIZE = 1000
    private const val NUM_THREADS = 4

    fun initialize(mongoConnectionString: String, mongoDatabaseName: String) {
        mongoManager = MongoManager(mongoConnectionString, mongoDatabaseName)
    }

    fun convertUsers() = runBlocking {
        val totalUsers = getTotalUserCount()
        val processedUsers = AtomicInteger(0)
        val failedUsers = AtomicInteger(0)

        val jobs = List(NUM_THREADS) { threadId ->
            launch(Dispatchers.Default) {
                convertUsersBatch(threadId, totalUsers, processedUsers, failedUsers)
            }
        }

        val timeTaken = measureTimeMillis {
            jobs.joinAll()
        }

        println("Conversion completed in $timeTaken ms")
        println("Processed ${processedUsers.get()} users")
        println("Failed to process ${failedUsers.get()} users")
    }

    private fun getTotalUserCount(): Int {
        return PoolManager.execute { connection ->
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM playerdata")
            resultSet.next()
            val count = resultSet.getInt("count")
            resultSet.close()
            statement.close()
            count
        } ?: 0
    }

    private suspend fun convertUsersBatch(threadId: Int, totalUsers: Int, processedUsers: AtomicInteger, failedUsers: AtomicInteger) {
        PoolManager.execute { connection ->
            val statement = connection.prepareStatement(
                "SELECT * FROM playerdata LIMIT ? OFFSET ?"
            )

            var offset = threadId * BATCH_SIZE
            while (offset < totalUsers) {
                statement.setInt(1, BATCH_SIZE)
                statement.setInt(2, offset)
                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    try {
                        val user = createUserFromResultSet(resultSet)
                        mongoManager.saveUser(user)
                        processedUsers.incrementAndGet()
                    } catch (e: Exception) {
                        println("Failed to process user at offset $offset: ${e.message}")
                        failedUsers.incrementAndGet()
                    }
                }

                resultSet.close()
                offset += NUM_THREADS * BATCH_SIZE

                val progress = processedUsers.get() * 100 / totalUsers
                println("Progress: $progress% (${processedUsers.get()}/$totalUsers)")
            }

            statement.close()
        }
    }

    private fun createUserFromResultSet(resultSet: ResultSet): User {
        val uuid = resultSet.getString("uuid")
        val username = resultSet.getString("name")
        val password = resultSet.getString("password") ?: ""
        val isPremium = resultSet.getBoolean("premium")

        return User(
            id = uuid,
            username = username,
            password = password,
            loggedIn = false,
            isPremium = isPremium
        )
    }
}*/