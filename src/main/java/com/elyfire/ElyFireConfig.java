package com.elyfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ElyFireConfig {
    public static final int VANILLA_FIRE_ASPECT_1_SECONDS = 4;
    public static final int VANILLA_FIRE_ASPECT_2_SECONDS = 8;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ElyFireConfig INSTANCE;

    public boolean enabled = true;
    public int fireAspect1Seconds = VANILLA_FIRE_ASPECT_1_SECONDS;
    public int fireAspect2Seconds = VANILLA_FIRE_ASPECT_2_SECONDS;
    public boolean lastHitOnly = false;
    public boolean extinguishOnDeath = true;
    public boolean requireAttackCooldown = false;
    public boolean allLivingEntities = true;

    public static ElyFireConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public int getDuration(int level) {
        if (level <= 1) {
            return Math.clamp(fireAspect1Seconds, 1, VANILLA_FIRE_ASPECT_1_SECONDS);
        } else {
            return Math.clamp(fireAspect2Seconds, 1, VANILLA_FIRE_ASPECT_2_SECONDS);
        }
    }

    public void resetToVanilla() {
        this.enabled = true;
        this.fireAspect1Seconds = VANILLA_FIRE_ASPECT_1_SECONDS;
        this.fireAspect2Seconds = VANILLA_FIRE_ASPECT_2_SECONDS;
        this.lastHitOnly = false;
        this.extinguishOnDeath = true;
        this.requireAttackCooldown = false;
        this.allLivingEntities = true;
    }

    public static ElyFireConfig load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ElyFireConfig config = GSON.fromJson(reader, ElyFireConfig.class);
                if (config != null) {
                    config.clampValues();
                    INSTANCE = config;
                    return config;
                }
            } catch (Exception e) {
                ElyFireMod.LOGGER.error("Failed to load elyfire config, creating default", e);
            }
        }
        ElyFireConfig config = new ElyFireConfig();
        config.save();
        INSTANCE = config;
        return config;
    }

    public void clampValues() {
        this.fireAspect1Seconds = Math.clamp(this.fireAspect1Seconds, 1, VANILLA_FIRE_ASPECT_1_SECONDS);
        this.fireAspect2Seconds = Math.clamp(this.fireAspect2Seconds, 1, VANILLA_FIRE_ASPECT_2_SECONDS);
    }

    public void save() {
        clampValues();
        File configFile = getConfigFile();
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ElyFireMod.LOGGER.error("Failed to save elyfire config", e);
        }
    }

    private static File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("elyfire.json").toFile();
    }
}
