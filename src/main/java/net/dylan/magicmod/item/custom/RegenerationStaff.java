package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class RegenerationStaff extends Item {
    private static final double REGEN_RADIUS = 12.0;
    private static final int REGEN_DURATION = 600; // 30 seconds
    
    public RegenerationStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Creates a regeneration field").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("Grants strong regeneration in 12 block radius").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Effect lasts 30 seconds").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Also provides resistance and strength").formatted(Formatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            createRegenerationField(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void createRegenerationField(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        // Play regeneration sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            // Create a large regeneration aura effect
            for (int layer = 0; layer < 5; layer++) {
                double layerRadius = (layer + 1) * 2.5;
                int particleCount = (int)(layerRadius * 8);
                
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i / (double) particleCount) * 2 * Math.PI;
                    double x = playerPos.x + Math.cos(angle) * layerRadius;
                    double z = playerPos.z + Math.sin(angle) * layerRadius;
                    double y = playerPos.y + 0.5 + Math.sin(layer + world.getTime() * 0.1) * 0.3;
                    
                    serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                        x, y, z,
                        1, 0.05, 0.1, 0.05, 0.02);
                }
            }

            // Create upward spiraling effect
            for (int i = 0; i < 30; i++) {
                double angle = i * 0.4;
                double radius = 3.0 + Math.sin(i * 0.2) * 1.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + (i * 0.3);
                
                serverWorld.spawnParticles(ParticleTypes.HEART,
                    x, y, z,
                    1, 0.1, 0.1, 0.1, 0.05);
            }

            // Find and buff all living entities in radius
            Box regenBox = new Box(playerPos.subtract(REGEN_RADIUS, REGEN_RADIUS, REGEN_RADIUS),
                                 playerPos.add(REGEN_RADIUS, REGEN_RADIUS, REGEN_RADIUS));
            List<Entity> entities = world.getOtherEntities(null, regenBox);

            int buffedCount = 0;
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    double distance = entity.getPos().distanceTo(playerPos);
                    if (distance <= REGEN_RADIUS) {
                        // Apply regeneration effects
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, REGEN_DURATION, 2));
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, REGEN_DURATION, 1));
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, REGEN_DURATION, 0));
                        
                        // For players, add additional benefits
                        if (livingEntity instanceof PlayerEntity) {
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 200, 1));
                        }
                        
                        buffedCount++;
                        
                        // Create buff particles around the entity
                        serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                            5, 0.3, 0.5, 0.3, 0.1);
                    }
                }
            }

            // Apply effects to caster
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, REGEN_DURATION, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, REGEN_DURATION, 1));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, REGEN_DURATION, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 200, 1));
            buffedCount++;

            // Play success sound
            if (buffedCount > 0) {
                serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 0.8F);
            }

            // Create a lasting visual effect
            for (int i = 0; i < 20; i++) {
                double angle = world.random.nextDouble() * 2 * Math.PI;
                double radius = world.random.nextDouble() * REGEN_RADIUS;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    x, playerPos.y + 1, z,
                    3, 0.2, 0.5, 0.2, 0.1);
            }
        }
    }
}