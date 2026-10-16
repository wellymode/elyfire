package com.elyfire.client;

import com.elyfire.ElyFireConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
public class ElyFireClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ElyFireConfig.getInstance();

        // Register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("elyfire")
                .executes(context -> {
                    sendInfo(context.getSource());
                    return 1;
                })
                .then(ClientCommandManager.literal("reset").executes(context -> {
                    ElyFireConfig cfg = ElyFireConfig.getInstance();
                    cfg.resetToVanilla();
                    cfg.save();
                    context.getSource().sendFeedback(Text.literal("§6[elyfire] §fНастройки сброшены до §eванильных значений настоящего огня §7(I: 4 сек, II: 8 сек)"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("toggle").executes(context -> {
                    ElyFireConfig cfg = ElyFireConfig.getInstance();
                    cfg.enabled = !cfg.enabled;
                    cfg.save();
                    String state = cfg.enabled ? "§aВключен" : "§cВыключен";
                    context.getSource().sendFeedback(Text.literal("§6[elyfire] §fВизуальный огонь: " + state));
                    return 1;
                }))
                .then(ClientCommandManager.literal("lasthit").executes(context -> {
                    ElyFireConfig cfg = ElyFireConfig.getInstance();
                    cfg.lastHitOnly = !cfg.lastHitOnly;
                    cfg.save();
                    String state = cfg.lastHitOnly ? "§aВключен" : "§cВыключен";
                    context.getSource().sendFeedback(Text.literal("§6[elyfire] §fРежим ластхита (огонь только на последнем): " + state));
                    return 1;
                }))
                .then(ClientCommandManager.literal("fire1")
                    .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer(1, ElyFireConfig.VANILLA_FIRE_ASPECT_1_SECONDS))
                        .executes(context -> {
                            int sec = IntegerArgumentType.getInteger(context, "seconds");
                            ElyFireConfig cfg = ElyFireConfig.getInstance();
                            cfg.fireAspect1Seconds = sec;
                            cfg.save();
                            context.getSource().sendFeedback(Text.literal("§6[elyfire] §fЗаговор огня I установлен на: §e" + sec + " сек §7(макс. ванильное: 4)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("fire2")
                    .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer(1, ElyFireConfig.VANILLA_FIRE_ASPECT_2_SECONDS))
                        .executes(context -> {
                            int sec = IntegerArgumentType.getInteger(context, "seconds");
                            ElyFireConfig cfg = ElyFireConfig.getInstance();
                            cfg.fireAspect2Seconds = sec;
                            cfg.save();
                            context.getSource().sendFeedback(Text.literal("§6[elyfire] §fЗаговор огня II установлен на: §e" + sec + " сек §7(макс. ванильное: 8)"));
                            return 1;
                        })
                    )
                )
            );
        });

        // Client tick event for cleaning up expired fire
        ClientTickEvents.END_CLIENT_TICK.register(client -> VisualFireManager.tick());

        // Clear tracking on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> VisualFireManager.clear());

        // Attack entity callback
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                handleAttack(player, entity);
            }
            return ActionResult.PASS;
        });
    }

    public static void handleAttack(PlayerEntity attacker, Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }

        // Fire in water is strictly disallowed
        if (living.isTouchingWater() || living.isSubmergedInWater()) {
            return;
        }

        ElyFireConfig config = ElyFireConfig.getInstance();
        if (!config.enabled) {
            return;
        }

        if (!config.allLivingEntities && !(living instanceof PlayerEntity)) {
            return;
        }

        // Check weapon in attacker's main hand
        ItemStack weapon = attacker.getMainHandStack();
        int fireAspectLevel = getFireAspectLevel(weapon);
        if (fireAspectLevel <= 0) {
            return;
        }

        // Shield blocks fire aspect in vanilla
        if (living.isBlocking()) {
            return;
        }

        // Check attack cooldown if enabled
        if (config.requireAttackCooldown && attacker.getAttackCooldownProgress(0.5f) < 0.9f) {
            return;
        }

        int durationSeconds = config.getDuration(fireAspectLevel);
        VisualFireManager.applyVisualFire(living, durationSeconds);
    }

    public static int getFireAspectLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        ItemEnchantmentsComponent enchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (var entry : enchants.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> registryEntry = entry.getKey();
            if (registryEntry.matchesKey(Enchantments.FIRE_ASPECT) || "minecraft:fire_aspect".equals(registryEntry.getIdAsString())) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    private static void sendInfo(FabricClientCommandSource source) {
        ElyFireConfig cfg = ElyFireConfig.getInstance();
        source.sendFeedback(Text.literal("§6=== §eelyfire §6==="));
        source.sendFeedback(Text.literal("§7Статус: " + (cfg.enabled ? "§aВключен" : "§cВыключен")));
        source.sendFeedback(Text.literal("§7Ластхит (только последний): " + (cfg.lastHitOnly ? "§aВключен" : "§cВыключен")));
        source.sendFeedback(Text.literal("§7Заговор огня I: §e" + cfg.fireAspect1Seconds + " сек §7(Ванилла: 4)"));
        source.sendFeedback(Text.literal("§7Заговор огня II: §e" + cfg.fireAspect2Seconds + " сек §7(Ванилла: 8)"));
        source.sendFeedback(Text.literal("§7Команды: §e/elyfire reset§7, §e/elyfire lasthit§7, §e/elyfire fire1 <1-4>§7, §e/elyfire fire2 <1-8>§7, §e/elyfire toggle"));
    }
}
