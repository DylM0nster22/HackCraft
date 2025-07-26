package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
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

public class IceOrb extends Item {
    public IceOrb(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("A frozen sphere of ice magic").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Right-click to create an ice patch").formatted(Formatting.BLUE));
            tooltip.add(Text.literal("Freezes water and creates slippery ice").formatted(Formatting.CYAN));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            createIcePatch(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void createIcePatch(World world, PlayerEntity player) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(20));

        RaycastContext context = new RaycastContext(
            startPos, endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        
        BlockHitResult hitResult = world.raycast(context);
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos centerPos = hitResult.getBlockPos().up();
            
            world.playSound(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                    SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.2F);

            if (world instanceof ServerWorld serverWorld) {
                // Create 5x5 ice patch
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos icePos = centerPos.add(x, 0, z);
                        if (world.getBlockState(icePos).isAir()) {
                            world.setBlockState(icePos, Blocks.ICE.getDefaultState());
                        }
                        
                        // Create particles
                        serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
                            icePos.getX() + 0.5, icePos.getY() + 1, icePos.getZ() + 0.5,
                            3, 0.3, 0.3, 0.3, 0.1);
                    }
                }
            }
        }
    }
}