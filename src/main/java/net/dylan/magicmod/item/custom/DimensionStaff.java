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
import java.util.Random;

public class DimensionStaff extends Item {
    public DimensionStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Tears a rift in reality for dimensional travel").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("Randomly teleports you within 100 blocks").formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.literal("Creates dramatic dimensional effects").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            performDimensionalRift(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void performDimensionalRift(World world, PlayerEntity player) {
        Vec3d currentPos = player.getPos();
        Random random = world.random;
        
        // Generate random teleport location within 100 blocks
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 20 + random.nextDouble() * 80; // 20-100 block range
        
        double x = currentPos.x + Math.cos(angle) * distance;
        double z = currentPos.z + Math.sin(angle) * distance;
        double y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
        
        Vec3d teleportPos = new Vec3d(x, y, z);

        // Play dimensional sound
        world.playSound(null, currentPos.x, currentPos.y, currentPos.z,
                SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.PLAYERS, 1.0F, 0.5F);

        if (world instanceof ServerWorld serverWorld) {
            createDimensionalRift(serverWorld, currentPos);
            
            // Create a brief delay effect with particles
            for (int i = 0; i < 60; i++) {
                double phi = random.nextDouble() * 2 * Math.PI;
                double costheta = random.nextDouble() * 2 - 1;
                double theta = Math.acos(costheta);
                double radius = 5 * Math.cbrt(random.nextDouble());
                
                double px = currentPos.x + radius * Math.sin(theta) * Math.cos(phi);
                double py = currentPos.y + radius * Math.sin(theta) * Math.sin(phi);
                double pz = currentPos.z + radius * Math.cos(theta);
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    px, py + 1, pz,
                    1, 0, 0, 0, 0);
                    
                serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH,
                    px, py + 1, pz,
                    1, 0.1, 0.1, 0.1, 0.05);
            }
        }

        // Teleport player
        player.teleport(teleportPos.x, teleportPos.y + 1, teleportPos.z);

        // Effects at destination
        world.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5F, 2.0F);

        if (world instanceof ServerWorld serverWorld) {
            createDimensionalRift(serverWorld, teleportPos);
        }
    }

    private void createDimensionalRift(ServerWorld world, Vec3d pos) {
        Random random = world.random;
        
        // Create swirling void effect
        for (int i = 0; i < 40; i++) {
            double angle1 = i * 0.3;
            double angle2 = i * 0.15;
            double radius = 2.0 + Math.sin(angle1) * 0.5;
            
            double x = pos.x + Math.cos(angle2) * radius;
            double z = pos.z + Math.sin(angle2) * radius;
            double y = pos.y + 1 + Math.sin(angle1) * 1.5;
            
            world.spawnParticles(ParticleTypes.PORTAL,
                x, y, z,
                2, 0.1, 0.1, 0.1, 0.5);
        }
        
        // Create chaotic particle bursts
        for (int i = 0; i < 25; i++) {
            double x = pos.x + (random.nextDouble() - 0.5) * 4;
            double y = pos.y + random.nextDouble() * 3;
            double z = pos.z + (random.nextDouble() - 0.5) * 4;
            
            world.spawnParticles(ParticleTypes.WITCH,
                x, y, z,
                3, 0.3, 0.3, 0.3, 0.2);
                
            world.spawnParticles(ParticleTypes.SMOKE,
                x, y, z,
                2, 0.2, 0.2, 0.2, 0.1);
        }
    }
}