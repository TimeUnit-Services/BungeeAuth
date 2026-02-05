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

import dev.timeunit.auth.command.*
import dev.timeunit.auth.command.admin.AuthDebugCommand
import dev.timeunit.auth.command.admin.ForceLoginCommand
import dev.timeunit.auth.command.admin.ResetCommand
import dev.timeunit.auth.listener.PlayerLoginListener
import dev.timeunit.auth.listener.PreLoginListener
import dev.timeunit.auth.mongo.MongoManager
import dev.timeunit.auth.redis.RedisBungeePublisher
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class BungeeAuth : Plugin() {
    lateinit var mongoManager: MongoManager
    lateinit var config: Configuration
    lateinit var messages: Configuration

    override fun onEnable() {
        instance = this

        config = loadYml("config.yml")
        messages = loadYml("messages.yml")

        val mongoUri = config.getString("mongo.uri", "mongodb://localhost:27017")
        val dbName = config.getString("mongo.database", "BungeeAuth")
        mongoManager = MongoManager(mongoUri, dbName)

        this.proxy.pluginManager.registerCommand(this, ChangePasswordCommand)
        this.proxy.pluginManager.registerCommand(this, LoginCommand)
        this.proxy.pluginManager.registerCommand(this, LogoutCommand)
        this.proxy.pluginManager.registerCommand(this, PremiumCommand)
        this.proxy.pluginManager.registerCommand(this, RegisterCommand)
        this.proxy.pluginManager.registerCommand(this, ResetCommand)
        this.proxy.pluginManager.registerCommand(this, ForceLoginCommand)
        this.proxy.pluginManager.registerCommand(this, AuthDebugCommand)

        if (config.getBoolean("settings.pin-code-enabled")) {
            this.proxy.pluginManager.registerCommand(this, GeneratePinCommand)
            this.proxy.pluginManager.registerCommand(this, ResetPasswordCommand)
        }

        this.proxy.pluginManager.registerListener(this, PreLoginListener)
        //this.proxy.pluginManager.registerListener(this, LoginListener)
        this.proxy.pluginManager.registerListener(this, PlayerLoginListener)

        /*
        SQLConverter.initialize(mongoUri, dbName)
        SQLConverter.convertUsers()
        */
    }

    override fun onDisable() {
        mongoManager.close()
        RedisBungeePublisher.close()
    }

    private fun loadYml(fileName: String): Configuration {
        if (!dataFolder.exists()) dataFolder.mkdir()

        val file = File(dataFolder, fileName)

        if (!file.exists()) {
            val resource: InputStream? = getResourceAsStream(fileName)

            if (resource != null) {
                Files.copy(resource, file.toPath())
            } else {
                file.createNewFile()
            }
        }

        return ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(file)
    }

    companion object {
        lateinit var instance: BungeeAuth
    }
}