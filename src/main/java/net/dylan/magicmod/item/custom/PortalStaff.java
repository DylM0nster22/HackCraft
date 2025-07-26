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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class PortalStaff extends Item {
    public PortalStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Creates a temporary portal gate effect").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("Teleports you 10 blocks forward").formatted(Formatting.BLUE));
            tooltip.add(Text.literal("Creates swirling portal particles").formatted(Formatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            createPortalTeleport(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void createPortalTeleport(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        Vec3d teleportPos = playerPos.add(lookDirection.multiply(10));
        
        // Find safe landing spot
        BlockPos targetPos = BlockPos.ofFloored(teleportPos);
        for (int y = targetPos.getY(); y < world.getTopY(); y++) {
            BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (!world.getBlockState(checkPos).isOpaque() && !world.getBlockState(checkPos.up()).isOpaque()) {
                teleportPos = Vec3d.ofBottomCenter(checkPos);
                break;
            }
        }

        // Play portal sounds
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.2F);

        if (world instanceof ServerWorld serverWorld) {
            // Create portal effect at current location
            createPortalEffect(serverWorld, playerPos);
            
            // Create intermediate portal effects along the path
            Vec3d step = teleportPos.subtract(playerPos).multiply(0.2);
            for (int i = 1; i < 5; i++) {
                Vec3d intermediatePos = playerPos.add(step.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.PORTAL,
                    intermediatePos.x, intermediatePos.y + 1, intermediatePos.z,
                    5, 0.2, 0.2, 0.2, 0.3);
            }
        }

        // Teleport player
        player.teleport(teleportPos.x, teleportPos.y, teleportPos.z);

        // Create portal effect at destination
        world.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            createPortalEffect(serverWorld, teleportPos);
        }
    }

    private void createPortalEffect(ServerWorld world, Vec3d pos) {
        // Create circular portal effect
        for (int i = 0; i < 360; i += 20) {
            double angle = Math.toRadians(i);
            double x = pos.x + Math.cos(angle) * 1.5;
            double z = pos.z + Math.sin(angle) * 1.5;
            
            world.spawnParticles(ParticleTypes.PORTAL,
                x, pos.y + 1, z,
                3, 0.1, 0.3, 0.1, 0.2);
        }
        
        // Create upward spiral effect
        for (int i = 0; i < 20; i++) {
            double angle = i * 0.5;
            double radius = 1.0 - (i * 0.05);
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + (i * 0.2);
            
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                x, y, z,
                1, 0.05, 0.05, 0.05, 0.1);
        }
    }
}