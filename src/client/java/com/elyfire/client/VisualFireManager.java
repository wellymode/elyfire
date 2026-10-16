package com.elyfire.client;

import com.elyfire.ElyFireConfig;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class VisualFireManager {
    // Stores entityId -> expiration time in milliseconds (System.currentTimeMillis())
    private static final Int2LongOpenHashMap FIRE_EXPIRY = new Int2LongOpenHashMap();
    private static int lastAttackedEntityId = -1;

    public static void applyVisualFire(Entity entity, float seconds) {
        if (entity == null || !entity.isAlive()) {
            return;
        }

        // Strictly disallow fire in water
        if (entity.isTouchingWater() || entity.isSubmergedInWater()) {
            return;
        }

        lastAttackedEntityId = entity.getId();

        ElyFireConfig config = ElyFireConfig.getInstance();
        synchronized (FIRE_EXPIRY) {
            if (config.lastHitOnly) {
                FIRE_EXPIRY.clear();
            }
            long durationMs = (long) (seconds * 1000.0f);
            long expireAt = System.currentTimeMillis() + durationMs;
            FIRE_EXPIRY.put(entity.getId(), expireAt);
        }
    }

    public static boolean isVisualFireActive(Entity entity) {
        if (entity == null) {
            return false;
        }

        ElyFireConfig config = ElyFireConfig.getInstance();
        if (!config.enabled) {
            return false;
        }

        if (entity.isSpectator()) {
            return false;
        }

        // Fire in water is completely forbidden and extinguished immediately
        if (entity.isTouchingWater() || entity.isSubmergedInWater()) {
            removeVisualFire(entity.getId());
            return false;
        }

        // Last hit only check
        if (config.lastHitOnly && entity.getId() != lastAttackedEntityId) {
            return false;
        }

        if (config.extinguishOnDeath && !entity.isAlive()) {
            removeVisualFire(entity.getId());
            return false;
        }

        long expireAt;
        synchronized (FIRE_EXPIRY) {
            expireAt = FIRE_EXPIRY.get(entity.getId());
        }

        if (expireAt == 0L) {
            return false;
        }

        if (System.currentTimeMillis() >= expireAt) {
            removeVisualFire(entity.getId());
            return false;
        }

        return true;
    }

    public static void removeVisualFire(int entityId) {
        synchronized (FIRE_EXPIRY) {
            FIRE_EXPIRY.remove(entityId);
        }
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        synchronized (FIRE_EXPIRY) {
            if (FIRE_EXPIRY.isEmpty()) {
                return;
            }
            FIRE_EXPIRY.int2LongEntrySet().removeIf(entry -> now >= entry.getLongValue());
        }
    }

    public static void clear() {
        synchronized (FIRE_EXPIRY) {
            FIRE_EXPIRY.clear();
        }
        lastAttackedEntityId = -1;
    }
}
