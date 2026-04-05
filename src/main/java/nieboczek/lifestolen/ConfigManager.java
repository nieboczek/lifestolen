package nieboczek.lifestolen;

import net.minecraft.client.Minecraft;
import nieboczek.lifestolen.module.Module;
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

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

    public static void saveConfigs(ArrayList<Module<?>> modules) {
        File config = getConfigFile();

        AllModulesSerializer serializer = new AllModulesSerializer(modules);
        SerializedStringBuilder builder = new SerializedStringBuilder();
        serializer.serialize(modules, builder);

        try (FileWriter writer = new FileWriter(config)) {
            writer.write(builder.getString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Lifestolen.LOG.info("[ConfigManager::saveConfigs] All configs saved");
    }

    public static void loadConfigs(ArrayList<Module<?>> modules) {
        File config = getConfigFile();

        String source;
        try {
            source = Files.readString(config.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AllModulesSerializer serializer = new AllModulesSerializer(modules);
        serializer.deserialize(source, modules);

        Lifestolen.LOG.info("[ConfigManager::loadConfigs] All configs loaded");
    }
}
