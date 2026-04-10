package nieboczek.lifestolen.config;

import net.minecraft.client.Minecraft;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public final class ConfigManager {
    private ConfigManager() {
    }

    private static File getConfigFile() {
        File dir = Minecraft.getInstance().gameDirectory;
        File config = dir.toPath().resolve("lifestolen.txt").toFile();

        try {
            config.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return config;
    }

    public static void saveConfig() {
        File config = getConfigFile();

        MainConfigSerializer serializer = new MainConfigSerializer(Lifestolen.modules);
        SerializedStringBuilder builder = new SerializedStringBuilder();
        serializer.serialize(Lifestolen.modules, builder);

        try (FileWriter writer = new FileWriter(config)) {
            writer.write(builder.getString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Lifestolen.log.info("[ConfigManager::saveConfigs] All configs saved");
    }

    public static void loadConfig() {
        File config = getConfigFile();

        String source;
        try {
            source = Files.readString(config.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MainConfigSerializer serializer = new MainConfigSerializer(Lifestolen.modules);
        serializer.deserialize(source, Lifestolen.modules);

        Lifestolen.log.info("[ConfigManager::loadConfigs] All configs loaded");
    }
}
