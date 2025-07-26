package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.WolfEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class BeastStaff extends Item {
    private static final int SUMMON_COUNT = 4;
    
    public BeastStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Summons loyal wolf companions").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Creates 4 tamed wolves to fight alongside you").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Wolves have enhanced health and strength").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("Creates magical beast summoning effects").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            summonBeastCompanions(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void summonBeastCompanions(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        // Play summoning sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            // Create summoning circle effect
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double radius = 3.0;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                
                serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, playerPos.y + 0.1, z,
                    2, 0.1, 0.1, 0.1, 0.05);
                    
                serverWorld.spawnParticles(ParticleTypes.CRIT,
                    x, playerPos.y + 0.5, z,
                    1, 0.05, 0.05, 0.05, 0.02);
            }

            // Summon wolves around the player
            for (int i = 0; i < SUMMON_COUNT; i++) {
                double angle = (i / (double) SUMMON_COUNT) * 2 * Math.PI;
                double distance = 2.5 + world.random.nextDouble() * 1.5;
                double x = playerPos.x + Math.cos(angle) * distance;
                double z = playerPos.z + Math.sin(angle) * distance;
                
                BlockPos spawnPos = new BlockPos((int) x, (int) playerPos.y, (int) z);
                
                WolfEntity wolf = EntityType.WOLF.create(serverWorld);
                if (wolf != null) {
                    wolf.refreshPositionAndAngles(x, playerPos.y, z, 0, 0);
                    wolf.initialize(serverWorld, serverWorld.getLocalDifficulty(spawnPos), SpawnReason.MOB_SUMMONED, null);
                    
                    // Make the wolf tamed and owned by the player
                    wolf.setTamed(true);
                    wolf.setOwner(player);
                    wolf.setSitting(false);
                    
                    // Enhance the wolf's attributes
                    wolf.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(30.0); // Normal: 20
                    wolf.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(6.0); // Normal: 4
                    wolf.heal(wolf.getMaxHealth()); // Heal to full
                    
                    serverWorld.spawnEntity(wolf);
                    
                    // Create summoning particles around each wolf
                    serverWorld.spawnParticles(ParticleTypes.POOF,
                        x, playerPos.y + 1, z,
                        10, 0.5, 0.5, 0.5, 0.1);
                        
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        x, playerPos.y + 1, z,
                        5, 0.3, 0.3, 0.3, 0.05);
                        
                    // Play individual summon sound
                    serverWorld.playSound(null, x, playerPos.y, z,
                            SoundEvents.ENTITY_WOLF_AMBIENT, SoundCategory.NEUTRAL, 0.8F, 1.2F);
                }
            }

            // Create final magical burst effect
            serverWorld.spawnParticles(ParticleTypes.FLASH,
                playerPos.x, playerPos.y + 1, playerPos.z,
                1, 0, 0, 0, 0);
                
            // Create expanding ring effect
            for (int ring = 1; ring <= 4; ring++) {
                double ringRadius = ring * 1.0;
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * 2 * Math.PI;
                    double x = playerPos.x + Math.cos(angle) * ringRadius;
                    double z = playerPos.z + Math.sin(angle) * ringRadius;
                    
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                        x, playerPos.y + 0.2, z,
                        1, 0.05, 0.05, 0.05, 0.02);
                }
            }

            // Play completion sound
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8F, 1.0F);
        }
    }
}