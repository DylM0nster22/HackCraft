package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimeStaff extends Item {
    private static final int COOLDOWN_TICKS = 400; // 20 seconds cooldown
    private static final int TIME_EFFECT_DURATION = 200; // 10 seconds effect
    private static final double EFFECT_RADIUS = 10.0; // 10 block radius
    private final Map<UUID, ServerBossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public TimeStaff(Settings settings) {
        super(settings);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Manipulates time in a 10 block radius").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Slows enemies and speeds up allies").formatted(Formatting.BLUE));
            tooltip.add(Text.literal("Effect lasts 10 seconds").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("20 second cooldown").formatted(Formatting.RED));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Check cooldown
        if (playerCooldowns.containsKey(user.getUuid()) && playerCooldowns.get(user.getUuid()) > 0) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        if (!world.isClient) {
            manipulateTime(world, user);
            
            // Set cooldown and create boss bar
            playerCooldowns.put(user.getUuid(), COOLDOWN_TICKS);
            if (user instanceof ServerPlayerEntity serverPlayer) {
                createCooldownBossBar(serverPlayer);
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void manipulateTime(World world, PlayerEntity player) {
        // Play time manipulation sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 2.0F);

        if (world instanceof ServerWorld serverWorld) {
            Vec3d playerPos = player.getPos();
            
            // Create time distortion particles in a sphere
            for (int i = 0; i < 50; i++) {
                double phi = Math.random() * 2 * Math.PI;
                double costheta = Math.random() * 2 - 1;
                double theta = Math.acos(costheta);
                double radius = EFFECT_RADIUS * Math.cbrt(Math.random()); // Cube root for uniform distribution in sphere
                
                double x = playerPos.x + radius * Math.sin(theta) * Math.cos(phi);
                double y = playerPos.y + radius * Math.sin(theta) * Math.sin(phi);
                double z = playerPos.z + radius * Math.cos(theta);
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    x, y, z, 
                    1, 0.1, 0.1, 0.1, 0.05);
                    
                // Add portal particles for time distortion effect
                serverWorld.spawnParticles(ParticleTypes.PORTAL, 
                    x, y, z, 
                    2, 0.1, 0.1, 0.1, 0.1);
            }

            // Find all entities in radius
            Box effectBox = new Box(playerPos.subtract(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS),
                                  playerPos.add(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS));
            List<Entity> entities = world.getOtherEntities(player, effectBox);

            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    double distance = entity.getPos().distanceTo(playerPos);
                    if (distance <= EFFECT_RADIUS) {
                        if (entity instanceof PlayerEntity) {
                            // Speed up other players (allies)
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, TIME_EFFECT_DURATION, 2));
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, TIME_EFFECT_DURATION, 1));
                        } else {
                            // Slow down hostile mobs
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, TIME_EFFECT_DURATION, 3));
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, TIME_EFFECT_DURATION, 2));
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, TIME_EFFECT_DURATION, 1));
                        }
                        
                        // Add visual effect to affected entities
                        serverWorld.spawnParticles(ParticleTypes.ENCHANT, 
                            entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), 
                            5, 0.3, 0.3, 0.3, 0.1);
                    }
                }
            }

            // Give the caster some time benefits
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, TIME_EFFECT_DURATION, 1));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, TIME_EFFECT_DURATION, 1));
        }
    }

    private void createCooldownBossBar(ServerPlayerEntity player) {
        ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Time Staff Cooldown"), 
            BossBar.Color.BLUE, 
            BossBar.Style.NOTCHED_20
        );
        bossBar.addPlayer(player);
        bossBar.setPercent(1.0f);
        playerBossBars.put(player.getUuid(), bossBar);
    }

    private void updateCooldownBossBar(ServerPlayerEntity player, int ticksLeft) {
        ServerBossBar bossBar = playerBossBars.get(player.getUuid());
        if (bossBar != null) {
            float progress = (float) ticksLeft / COOLDOWN_TICKS;
            bossBar.setPercent(progress);
            if (ticksLeft <= 0) {
                bossBar.removePlayer(player);
                playerBossBars.remove(player.getUuid());
            }
        }
    }

    private void onServerTick(MinecraftServer server) {
        // Handle player cooldowns and boss bars
        Iterator<Map.Entry<UUID, Integer>> cooldownIterator = playerCooldowns.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = cooldownIterator.next();
            UUID playerId = entry.getKey();
            int ticksLeft = entry.getValue() - 1;
            
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                updateCooldownBossBar(player, ticksLeft);
            }
            
            if (ticksLeft <= 0) {
                cooldownIterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
    }
}