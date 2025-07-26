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

public class EnchantedScroll extends Item {
    public EnchantedScroll(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("An ancient scroll with mysterious runes").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Right-click to cast random enchantments").formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.literal("May grant temporary magical abilities").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Single-use consumable item").formatted(Formatting.RED));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            castScrollMagic(world, player);
            // Consume the scroll
            ItemStack stack = player.getStackInHand(hand);
            stack.decrement(1);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void castScrollMagic(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 0.8F);

        if (world instanceof ServerWorld serverWorld) {
            // Create magical rune effects
            for (int i = 0; i < 40; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * 3;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 0.5 + Math.random() * 2.5;
                
                serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, y, z, 1, 0.05, 0.05, 0.05, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.PORTAL,
                    x, y, z, 1, 0.1, 0.1, 0.1, 0.05);
            }
            
            // Grant multiple beneficial effects
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.SPEED, 600, 1));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.STRENGTH, 600, 1));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.RESISTANCE, 600, 0));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.REGENERATION, 200, 2));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.LUCK, 1200, 0));
                
            // Create dramatic effect
            serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                playerPos.x, playerPos.y + 1.5, playerPos.z,
                20, 0.5, 1.0, 0.5, 0.2);
        }
    }
}