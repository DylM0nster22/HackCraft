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

public class CrystalShard extends Item {
    public CrystalShard(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("A pure magical crystal fragment").formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.literal("Right-click to release stored magic energy").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Creates a dazzling light display").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Can be used as crafting material").formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            releaseMagicEnergy(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void releaseMagicEnergy(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0F, 1.5F);

        if (world instanceof ServerWorld serverWorld) {
            // Create crystal energy burst
            for (int i = 0; i < 25; i++) {
                double angle = (i / 25.0) * 2 * Math.PI;
                double radius = 1.5 + Math.sin(i * 0.5) * 0.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 1 + Math.sin(angle * 2) * 0.5;
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    x, y, z, 1, 0.05, 0.05, 0.05, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
            }
            
            // Central flash
            serverWorld.spawnParticles(ParticleTypes.FLASH,
                playerPos.x, playerPos.y + 1.5, playerPos.z,
                1, 0, 0, 0, 0);
        }
    }
}