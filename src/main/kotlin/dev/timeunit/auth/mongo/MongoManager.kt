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

package dev.timeunit.auth.mongo

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import dev.timeunit.auth.mongo.user.User
import org.bson.Document
import java.util.*

class MongoManager(connectionString: String, databaseName: String) {
    private val client: MongoClient
    private val database: MongoDatabase
    private val usersCollection: MongoCollection<Document>

    init {
        val uri = MongoClientURI(connectionString)
        client = MongoClient(uri)
        database = client.getDatabase(databaseName)
        usersCollection = database.getCollection("users")
    }

    fun findUser(username: String): User? {
        val query = Document("username", username)
        val document = usersCollection.find(query).first()
        return document?.let { User.fromDocument(it) }
    }

    fun findUser(id: UUID): User? {
        val query = Document("_id", id.toString())
        val document = usersCollection.find(query).first()
        return document?.let { User.fromDocument(it) }
    }

    fun saveUser(user: User) {
        val query = Document("username", user.username)
        val update = Document("\$set", user.toDocument())
        usersCollection.updateOne(query, update, UpdateOptions().upsert(true))
    }

    fun deleteUser(username: String) {
        val query = Document("username", username)
        usersCollection.deleteOne(query)
    }

    fun close() {
        client.close()
    }
}