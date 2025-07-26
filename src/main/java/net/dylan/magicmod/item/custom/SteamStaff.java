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

public class SteamStaff extends Item {
    public SteamStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Creates a scalding steam cloud").formatted(Formatting.WHITE));
            tooltip.add(Text.literal("Damages and blinds enemies in area").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Combines fire and water magic").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            createSteamCloud(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void createSteamCloud(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        Vec3d steamCenter = playerPos.add(lookDirection.multiply(5));
        
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            // Create large steam cloud
            for (int i = 0; i < 60; i++) {
                double x = steamCenter.x + (Math.random() - 0.5) * 8;
                double y = steamCenter.y + Math.random() * 4;
                double z = steamCenter.z + (Math.random() - 0.5) * 8;
                
                serverWorld.spawnParticles(ParticleTypes.CLOUD,
                    x, y, z, 3, 0.3, 0.3, 0.3, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.WHITE_ASH,
                    x, y, z, 2, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }
}