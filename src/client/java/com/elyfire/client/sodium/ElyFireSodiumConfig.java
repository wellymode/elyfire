package com.elyfire.client.sodium;

import com.elyfire.ElyFireConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ElyFireSodiumConfig implements ConfigEntryPoint {

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        ModOptionsBuilder modOptions = builder.registerModOptions("elyfire", "elyfire", "1.0.0");

        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Text.literal("elyfire"));

        OptionGroupBuilder mainGroup = builder.createOptionGroup();
        mainGroup.setName(Text.literal("Настройки огня (Fire Aspect)"));

        // Toggle visual fire
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("elyfire", "enabled"))
            .setName(Text.literal("Визуальный огонь"))
            .setTooltip(Text.literal("Включает или выключает визуальное отображение огня на противнике при ударе оружием с Заговором огня."))
            .setDefaultValue(true)
            .setBinding(val -> ElyFireConfig.getInstance().enabled = val, () -> ElyFireConfig.getInstance().enabled)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // Last hit only toggle (default: false)
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("elyfire", "last_hit_only"))
            .setName(Text.literal("Режим ластхита"))
            .setTooltip(Text.literal("Отображать огонь исключительно на том противнике, которого вы ударили последним.\n§7По умолчанию: Выключено."))
            .setDefaultValue(false)
            .setBinding(val -> ElyFireConfig.getInstance().lastHitOnly = val, () -> ElyFireConfig.getInstance().lastHitOnly)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // Fire Aspect I duration slider: max 4 seconds (vanilla max)
        mainGroup.addOption(builder.createIntegerOption(Identifier.of("elyfire", "fire_aspect_1_duration"))
            .setName(Text.literal("Заговор огня I (секунды)"))
            .setTooltip(Text.literal("Сколько секунд цель будет визуально гореть при ударе с Заговором огня I.\n§7Максимальное и ванильное значение: 4 сек."))
            .setRange(1, ElyFireConfig.VANILLA_FIRE_ASPECT_1_SECONDS, 1)
            .setDefaultValue(ElyFireConfig.VANILLA_FIRE_ASPECT_1_SECONDS)
            .setValueFormatter(val -> Text.literal(val + " сек"))
            .setBinding(val -> ElyFireConfig.getInstance().fireAspect1Seconds = val, () -> ElyFireConfig.getInstance().fireAspect1Seconds)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // Fire Aspect II duration slider: max 8 seconds (vanilla max)
        mainGroup.addOption(builder.createIntegerOption(Identifier.of("elyfire", "fire_aspect_2_duration"))
            .setName(Text.literal("Заговор огня II (секунды)"))
            .setTooltip(Text.literal("Сколько секунд цель будет визуально гореть при ударе с Заговором огня II.\n§7Максимальное и ванильное значение: 8 сек."))
            .setRange(1, ElyFireConfig.VANILLA_FIRE_ASPECT_2_SECONDS, 1)
            .setDefaultValue(ElyFireConfig.VANILLA_FIRE_ASPECT_2_SECONDS)
            .setValueFormatter(val -> Text.literal(val + " сек"))
            .setBinding(val -> ElyFireConfig.getInstance().fireAspect2Seconds = val, () -> ElyFireConfig.getInstance().fireAspect2Seconds)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // All living entities or players only
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("elyfire", "all_living_entities"))
            .setName(Text.literal("Все сущности (мобы)"))
            .setTooltip(Text.literal("Отображать визуальный огонь не только на игроках, но и на других мобах."))
            .setDefaultValue(true)
            .setBinding(val -> ElyFireConfig.getInstance().allLivingEntities = val, () -> ElyFireConfig.getInstance().allLivingEntities)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // Require attack cooldown toggle
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("elyfire", "require_attack_cooldown"))
            .setName(Text.literal("Требовать кулдаун удара"))
            .setTooltip(Text.literal("Требовать полный заряд атаки (> 90%).\nОтключите для серверов со спам-кликом или 1.8 PvP."))
            .setDefaultValue(false)
            .setBinding(val -> ElyFireConfig.getInstance().requireAttackCooldown = val, () -> ElyFireConfig.getInstance().requireAttackCooldown)
            .setStorageHandler(() -> ElyFireConfig.getInstance().save())
        );

        // Reset button
        mainGroup.addOption(builder.createExternalButtonOption(Identifier.of("elyfire", "reset_vanilla"))
            .setName(Text.literal("§eСбросить до ванильных значений"))
            .setTooltip(Text.literal("Сбрасывает настройки длительности огня до значений настоящего ванильного огня (I: 4 сек, II: 8 сек)."))
            .setScreenConsumer(parentScreen -> {
                ElyFireConfig.getInstance().resetToVanilla();
                ElyFireConfig.getInstance().save();
                MinecraftClient.getInstance().setScreen(parentScreen);
            })
        );

        page.addOptionGroup(mainGroup);
        modOptions.addPage(page);
    }
}
