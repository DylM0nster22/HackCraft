package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
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

public class MagicWand extends Item {
    public MagicWand(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("A versatile magical focus").formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.literal("Right-click for random magical effects").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Each use creates different magic").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            castRandomMagic(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void castRandomMagic(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        int effect = world.random.nextInt(5);
        
        switch (effect) {
            case 0: // Healing sparkles
                world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.5F);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART,
                        playerPos.x, playerPos.y + 1.5, playerPos.z,
                        10, 0.5, 0.5, 0.5, 0.1);
                }
                player.heal(2.0F);
                break;
                
            case 1: // Speed boost
                world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5F, 2.0F);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                        playerPos.x, playerPos.y + 1, playerPos.z,
                        15, 0.5, 1.0, 0.5, 0.1);
                }
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SPEED, 200, 1));
                break;
                
            case 2: // Protective sparkles
                world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 1.2F);
                if (world instanceof ServerWorld serverWorld) {
                    for (int i = 0; i < 20; i++) {
                        double angle = (i / 20.0) * 2 * Math.PI;
                        double x = playerPos.x + Math.cos(angle) * 2;
                        double z = playerPos.z + Math.sin(angle) * 2;
                        serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                            x, playerPos.y + 1, z,
                            1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.RESISTANCE, 300, 0));
                break;
                
            case 3: // Light burst
                world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.5F, 1.5F);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.FLASH,
                        playerPos.x, playerPos.y + 1.5, playerPos.z,
                        1, 0, 0, 0, 0);
                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        playerPos.x, playerPos.y + 1.5, playerPos.z,
                        20, 1.0, 1.0, 1.0, 0.3);
                }
                break;
                
            case 4: // Magical mist
                world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                        SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (world instanceof ServerWorld serverWorld) {
                    for (int i = 0; i < 30; i++) {
                        double x = playerPos.x + (world.random.nextDouble() - 0.5) * 4;
                        double y = playerPos.y + world.random.nextDouble() * 3;
                        double z = playerPos.z + (world.random.nextDouble() - 0.5) * 4;
                        
                        serverWorld.spawnParticles(ParticleTypes.WITCH,
                            x, y, z, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                break;
        }
    }
}