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

public class LifeStaff extends Item {
    private static final double HEALING_RADIUS = 8.0;
    private static final int HEALING_AMOUNT = 6; // 3 hearts
    
    public LifeStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Channels life energy to heal allies").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("Heals all living entities in 8 block radius").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Restores 3 hearts instantly").formatted(Formatting.RED));
            tooltip.add(Text.literal("Grants regeneration effect").formatted(Formatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            channelLifeEnergy(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void channelLifeEnergy(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        // Play healing sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.5F);

        if (world instanceof ServerWorld serverWorld) {
            // Create life energy particles in a sphere
            for (int i = 0; i < 50; i++) {
                double phi = Math.random() * 2 * Math.PI;
                double costheta = Math.random() * 2 - 1;
                double theta = Math.acos(costheta);
                double radius = HEALING_RADIUS * Math.cbrt(Math.random());
                
                double x = playerPos.x + radius * Math.sin(theta) * Math.cos(phi);
                double y = playerPos.y + radius * Math.sin(theta) * Math.sin(phi);
                double z = playerPos.z + radius * Math.cos(theta);
                
                serverWorld.spawnParticles(ParticleTypes.HEART,
                    x, y + 1, z,
                    1, 0.1, 0.1, 0.1, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    x, y + 1, z,
                    1, 0.05, 0.05, 0.05, 0.05);
            }

            // Create pulsing effect around caster
            for (int ring = 1; ring <= 3; ring++) {
                double ringRadius = ring * 2.0;
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * 2 * Math.PI;
                    double x = playerPos.x + Math.cos(angle) * ringRadius;
                    double z = playerPos.z + Math.sin(angle) * ringRadius;
                    
                    serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                        x, playerPos.y + 0.5, z,
                        2, 0.1, 0.2, 0.1, 0.05);
                }
            }

            // Find and heal all living entities in radius
            Box healingBox = new Box(playerPos.subtract(HEALING_RADIUS, HEALING_RADIUS, HEALING_RADIUS),
                                   playerPos.add(HEALING_RADIUS, HEALING_RADIUS, HEALING_RADIUS));
            List<Entity> entities = world.getOtherEntities(null, healingBox);

            int healedCount = 0;
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    double distance = entity.getPos().distanceTo(playerPos);
                    if (distance <= HEALING_RADIUS) {
                        // Heal the entity
                        if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                            livingEntity.heal(HEALING_AMOUNT);
                            healedCount++;
                            
                            // Add regeneration effect
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                            
                            // Add absorption for extra protection
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 0));
                            
                            // Create healing particles around the healed entity
                            serverWorld.spawnParticles(ParticleTypes.HEART,
                                entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                                8, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
            }

            // Also heal the caster
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(HEALING_AMOUNT);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 0));
                healedCount++;
            }

            // Play additional sound if entities were healed
            if (healedCount > 0) {
                serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5F, 2.0F);
            }
        }
    }
}