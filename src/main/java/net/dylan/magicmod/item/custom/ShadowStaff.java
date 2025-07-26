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

public class ShadowStaff extends Item {
    public ShadowStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Cloaks you in shadows and darkness").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Grants invisibility and night vision").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Creates a shadow aura around you").formatted(Formatting.BLACK));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            castShadowCloak(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void castShadowCloak(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.5F, 1.5F);

        if (world instanceof ServerWorld serverWorld) {
            // Create shadow particles
            for (int i = 0; i < 40; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * 3;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + Math.random() * 3;
                
                serverWorld.spawnParticles(ParticleTypes.SMOKE,
                    x, y, z, 2, 0.2, 0.2, 0.2, 0.1);
            }

            // Apply invisibility and night vision
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.INVISIBILITY, 600, 0));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, 600, 0));
        }
    }
}