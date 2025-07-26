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

public class RestorationStaff extends Item {
    public RestorationStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Restores durability to all items").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("Repairs equipment in your inventory").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Creates restoration magic effects").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            performRestoration(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void performRestoration(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.5F);

        if (world instanceof ServerWorld serverWorld) {
            // Repair all damaged items in inventory
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.isDamaged()) {
                    stack.setDamage(0);
                }
            }

            // Create restoration particles
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * 2;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 1 + Math.random() * 2;
                
                serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, y, z, 2, 0.1, 0.1, 0.1, 0.1);
                    
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    x, y, z, 1, 0.05, 0.05, 0.05, 0.05);
            }
        }
    }
}