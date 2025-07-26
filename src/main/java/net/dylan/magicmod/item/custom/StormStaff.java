package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class StormStaff extends Item {
    private static final int LIGHTNING_COUNT = 5;
    private static final double STORM_RADIUS = 20.0;
    
    public StormStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Calls down a lightning storm").formatted(Formatting.DARK_BLUE));
            tooltip.add(Text.literal("Creates 5 lightning strikes in 20 block radius").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Changes weather to thunder and rain").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Creates dramatic storm effects").formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            unleashStorm(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void unleashStorm(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        // Start thunder and rain
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setWeather(0, 6000, true, true); // Clear time, rain time, raining, thundering
        }
        
        // Play storm initiation sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 2.0F, 0.8F);

        if (world instanceof ServerWorld serverWorld) {
            // Create dark storm clouds effect
            for (int i = 0; i < 100; i++) {
                double x = playerPos.x + (world.random.nextDouble() - 0.5) * STORM_RADIUS * 2;
                double y = playerPos.y + 10 + world.random.nextDouble() * 10;
                double z = playerPos.z + (world.random.nextDouble() - 0.5) * STORM_RADIUS * 2;
                
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    3, 1.0, 0.5, 1.0, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.CLOUD,
                    x, y, z,
                    2, 0.8, 0.3, 0.8, 0.05);
            }

            // Create initial electrical charge in the air
            for (int i = 0; i < 50; i++) {
                double angle = world.random.nextDouble() * 2 * Math.PI;
                double radius = world.random.nextDouble() * STORM_RADIUS;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 5 + world.random.nextDouble() * 15;
                
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    1, 0.3, 0.3, 0.3, 0.2);
            }

            // Strike multiple lightning bolts with delays
            for (int i = 0; i < LIGHTNING_COUNT; i++) {
                final int delay = i * 20; // 1 second delay between strikes
                
                // Schedule lightning strike
                serverWorld.getServer().execute(() -> {
                    try {
                        Thread.sleep(delay * 50L); // Convert ticks to milliseconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Random position within storm radius
                    double angle = world.random.nextDouble() * 2 * Math.PI;
                    double distance = world.random.nextDouble() * STORM_RADIUS;
                    double x = playerPos.x + Math.cos(angle) * distance;
                    double z = playerPos.z + Math.sin(angle) * distance;
                    double y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
                    
                    // Create lightning
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                    if (lightning != null) {
                        lightning.refreshPositionAfterTeleport(x, y, z);
                        serverWorld.spawnEntity(lightning);
                        
                        // Add extra effects around lightning
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            x, y + 5, z,
                            20, 2.0, 3.0, 2.0, 0.5);
                            
                        serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                            x, y, z,
                            3, 1.0, 0.5, 1.0, 0.2);
                    }
                });
            }

            // Create swirling wind effect
            for (int i = 0; i < 30; i++) {
                double angle = i * 0.4;
                double radius = 5.0 + Math.sin(i * 0.3) * 2.0;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 2 + i * 0.3;
                
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    x, y, z,
                    1, 0.1, 0.1, 0.1, 0.1);
            }

            // Create rain particles
            for (int i = 0; i < 40; i++) {
                double x = playerPos.x + (world.random.nextDouble() - 0.5) * STORM_RADIUS;
                double z = playerPos.z + (world.random.nextDouble() - 0.5) * STORM_RADIUS;
                double y = playerPos.y + 8 + world.random.nextDouble() * 5;
                
                serverWorld.spawnParticles(ParticleTypes.FALLING_WATER,
                    x, y, z,
                    1, 0, -0.5, 0, 0.3);
            }

            // Play ongoing thunder sounds
            for (int i = 1; i <= 3; i++) {
                final int soundDelay = i * 40; // 2 second delays
                serverWorld.getServer().execute(() -> {
                    try {
                        Thread.sleep(soundDelay * 50L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 
                            1.0F, 0.8F + world.random.nextFloat() * 0.4F);
                });
            }
        }
    }
}