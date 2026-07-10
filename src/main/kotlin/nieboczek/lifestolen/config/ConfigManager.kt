package nieboczek.lifestolen.config

import net.minecraft.client.Minecraft
import nieboczek.lifestolen.BuildInfo
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.Setting
import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

object ConfigManager {
    private val configFile: File
    private val serializers = HashMap<String, ModuleSerializer>()

    init {
        @Suppress("KotlinConstantConditions")
        val config = if (!BuildInfo.CONFIG_GLOBAL) {
            val dir = Minecraft.getInstance().gameDirectory
            dir.toPath().resolve(BuildInfo.CONFIG_LOCATION).toFile()
        } else {
            File(BuildInfo.CONFIG_LOCATION)
        }

        try {
            config.createNewFile()
            configFile = config
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        @Suppress("unchecked_cast")
        Lifestolen.modules.forEach { serializers[it.id] = ModuleSerializer(it.settings as List<Setting<Any>>) }
        Lifestolen.log.info("Loaded {} serializers", serializers.size)
        Lifestolen.log.debug("Serializers: {}", serializers.keys)
    }

    fun loadConfig(): ClientConfig {
        val source: String
        try {
            source = Files.readString(configFile.toPath())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        if (source.isBlank()) {
            val cfg = ClientConfig()
            saveConfig(cfg)
            Lifestolen.log.info("[ConfigManager::loadConfig] Saved default config")
            return cfg
        } else {
            val cfg = deserializeConfig(TokenStream(source))
            Lifestolen.log.info("[ConfigManager::loadConfig] Config loaded")
            return cfg
        }
    }

    fun saveConfig() {
        saveConfig(Lifestolen.cfg)
    }

    private fun saveConfig(cfg: ClientConfig) {
        val builder = SerializedStringBuilder()
        serializeConfig(cfg, builder)

        try {
            FileWriter(configFile).use { writer ->
                writer.write(builder.string)
            }
            Lifestolen.log.info("[ConfigManager::saveConfig] Config saved")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun serializeConfig(cfg: ClientConfig, builder: SerializedStringBuilder) {
        builder.text('{').newLine()
        builder.indent()

        // Client config
        builder.indented().text(ClientConfig.ID).text(" = ")
        ClientConfig.serializer.serialize(cfg, builder)
        builder.text(';').newLine()

        // Modules
        for ((id, serializer) in serializers) {
            builder.indented().text(id).text(" = ")
            serializer.serialize(builder)
            builder.text(';').newLine()
        }

        builder.unindent()
        builder.text("};").newLine()
    }

    private fun deserializeConfig(stream: TokenStream): ClientConfig {
        var clientConfig: ClientConfig? = null
        stream.expect(TokenType.L_BRACE)

        while (stream.continueIfNot(TokenType.R_BRACE)) {
            val id = stream.nextTokenText(TokenType.IDENTIFIER)
            stream.expect(TokenType.EQUAL)
            when (id) {
                ClientConfig.ID -> {
                    clientConfig = ClientConfig.serializer.deserialize(stream)
                }

                else -> {
                    val serializer = serializers[id] ?: throw SerializerError("Serializer for $id not found")
                    serializer.deserialize(stream)
                }
            }
            stream.expect(TokenType.SEMICOLON)
        }

        stream.expect(TokenType.SEMICOLON)
        return clientConfig ?: ClientConfig()
    }

    private class ModuleSerializer(val settings: List<Setting<Any>>) {
        fun serialize(builder: SerializedStringBuilder) {
            builder.text('{').newLine()
            builder.indent()

            for (setting in settings) {
                builder.indented().text(setting.id).text(" = ")
                setting.serializer.serialize(setting.value, builder)
                builder.text(';').newLine()
            }

            builder.unindent()
            builder.indented().text('}')
        }

        fun deserialize(stream: TokenStream) {
            stream.expect(TokenType.L_BRACE)

            while (stream.continueIfNot(TokenType.R_BRACE)) {
                val id = stream.nextTokenText(TokenType.IDENTIFIER)
                val setting = settings.find { it.id == id } ?: throw SerializerError("Setting \"$id\" not found")
                stream.expect(TokenType.EQUAL)
                setting.value = setting.serializer.deserialize(stream)
                stream.expect(TokenType.SEMICOLON)
            }
        }
    }
}
