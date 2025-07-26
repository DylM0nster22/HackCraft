package net.dylan.magicmod.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

public class MiningStaff extends Item {
    private static final int MINING_RADIUS = 3;
    
    public MiningStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Instantly mines blocks in a 3x3x3 area").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Drops items directly into your inventory").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Works on most solid blocks").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Creates mining dust particles").formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            performMagicMining(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void performMagicMining(World world, PlayerEntity player) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(50));

        RaycastContext context = new RaycastContext(
            startPos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        
        BlockHitResult hitResult = world.raycast(context);
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos centerPos = hitResult.getBlockPos();
            
            // Play mining sound
            world.playSound(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                    SoundEvents.BLOCK_ANCIENT_DEBRIS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (world instanceof ServerWorld serverWorld) {
                int minedBlocks = 0;
                
                // Mine blocks in a 3x3x3 area
                for (int x = -MINING_RADIUS; x <= MINING_RADIUS; x++) {
                    for (int y = -MINING_RADIUS; y <= MINING_RADIUS; y++) {
                        for (int z = -MINING_RADIUS; z <= MINING_RADIUS; z++) {
                            BlockPos minePos = centerPos.add(x, y, z);
                            BlockState blockState = world.getBlockState(minePos);
                            
                            // Check if the block can be mined
                            if (!blockState.isAir() && 
                                !blockState.isLiquid() && 
                                blockState.getHardness(world, minePos) >= 0 &&
                                blockState.getHardness(world, minePos) < 50) { // Don't mine bedrock-like blocks
                                
                                // Get drops
                                List<ItemStack> drops = Block.getDroppedStacks(blockState, serverWorld, minePos, 
                                    world.getBlockEntity(minePos), player, player.getMainHandStack());
                                
                                // Add drops to player inventory
                                for (ItemStack drop : drops) {
                                    if (!player.getInventory().insertStack(drop)) {
                                        // If inventory is full, drop at player location
                                        Block.dropStack(world, player.getBlockPos(), drop);
                                    }
                                }
                                
                                // Create mining particles
                                serverWorld.spawnParticles(ParticleTypes.BLOCK.of(blockState),
                                    minePos.getX() + 0.5, minePos.getY() + 0.5, minePos.getZ() + 0.5,
                                    8, 0.3, 0.3, 0.3, 0.1);
                                
                                // Break the block
                                world.removeBlock(minePos, false);
                                minedBlocks++;
                            }
                        }
                    }
                }

                // Create magical mining effects
                Vec3d centerVec = Vec3d.ofCenter(centerPos);
                
                // Create expanding sphere of particles
                for (int i = 0; i < 50; i++) {
                    double phi = Math.random() * 2 * Math.PI;
                    double costheta = Math.random() * 2 - 1;
                    double theta = Math.acos(costheta);
                    double radius = MINING_RADIUS * Math.cbrt(Math.random());
                    
                    double x = centerVec.x + radius * Math.sin(theta) * Math.cos(phi);
                    double y = centerVec.y + radius * Math.sin(theta) * Math.sin(phi);
                    double z = centerVec.z + radius * Math.cos(theta);
                    
                    serverWorld.spawnParticles(ParticleTypes.CRIT,
                        x, y, z,
                        1, 0.1, 0.1, 0.1, 0.2);
                }

                // Create dust cloud effect
                serverWorld.spawnParticles(ParticleTypes.POOF,
                    centerVec.x, centerVec.y, centerVec.z,
                    20, MINING_RADIUS * 0.5, MINING_RADIUS * 0.5, MINING_RADIUS * 0.5, 0.1);

                // Play completion sound if blocks were mined
                if (minedBlocks > 0) {
                    serverWorld.playSound(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.2F);
                            
                    // Create success particles
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        centerVec.x, centerVec.y + 1, centerVec.z,
                        minedBlocks / 2, 1.0, 1.0, 1.0, 0.1);
                }
            }
        }
    }
}