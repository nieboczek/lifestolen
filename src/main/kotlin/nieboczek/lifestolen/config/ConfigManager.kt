package nieboczek.lifestolen.config

import net.minecraft.client.Minecraft
import nieboczek.lifestolen.Lifestolen
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

object ConfigManager {
    private val configFile: File

    init {
        val dir = Minecraft.getInstance().gameDirectory
        val config = dir.toPath().resolve("lifestolen.txt").toFile()

        try {
            config.createNewFile()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        configFile = config
    }

    fun saveConfig() {
        val configStr = "" // save logic here

        try {
            FileWriter(configFile).use { writer ->
                writer.write(configStr)
            }
            Lifestolen.log.info("[ConfigManager::saveConfig] All configs saved")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun loadConfig() {
        val source: String
        try {
            source = Files.readString(configFile.toPath())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        // load logic here

        Lifestolen.log.info("[ConfigManager::loadConfig] All configs loaded")
    }
}
