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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class EnderStaff extends Item {
    private static final double MAX_TELEPORT_DISTANCE = 50.0;

    public EnderStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Teleports you to where you're looking").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("Maximum range: 50 blocks").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Creates Enderman particles and sound effects").formatted(Formatting.DARK_GREEN));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            performEnderTeleport(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void performEnderTeleport(World world, PlayerEntity player) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(MAX_TELEPORT_DISTANCE));

        RaycastContext context = new RaycastContext(
            startPos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        
        BlockHitResult hitResult = world.raycast(context);
        Vec3d teleportPos;
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            teleportPos = Vec3d.ofBottomCenter(hitPos.up());
        } else {
            teleportPos = endPos;
        }

        // Check if teleport location is safe
        BlockPos targetPos = BlockPos.ofFloored(teleportPos);
        if (world.getBlockState(targetPos).isOpaque() || world.getBlockState(targetPos.up()).isOpaque()) {
            // Find the highest non-solid block
            for (int y = targetPos.getY(); y < world.getTopY(); y++) {
                BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
                if (!world.getBlockState(checkPos).isOpaque() && !world.getBlockState(checkPos.up()).isOpaque()) {
                    teleportPos = Vec3d.ofBottomCenter(checkPos);
                    break;
                }
            }
        }

        // Play teleport sound at current location
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Create particles at current location
        if (world instanceof ServerWorld serverWorld) {
            Vec3d currentPos = player.getPos();
            serverWorld.spawnParticles(ParticleTypes.PORTAL,
                currentPos.x, currentPos.y + 1, currentPos.z,
                20, 0.5, 1.0, 0.5, 0.1);
        }

        // Teleport the player
        player.teleport(teleportPos.x, teleportPos.y, teleportPos.z);

        // Play sound and particles at destination
        world.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL,
                teleportPos.x, teleportPos.y + 1, teleportPos.z,
                20, 0.5, 1.0, 0.5, 0.1);
            
            // Add reverse particles for effect
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                teleportPos.x, teleportPos.y + 1, teleportPos.z,
                15, 0.3, 0.8, 0.3, 0.05);
        }
    }
}