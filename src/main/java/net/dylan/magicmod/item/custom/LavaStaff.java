package net.dylan.magicmod.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class LavaStaff extends Item {

    private static final int RANGE = 50;
    private static final int LAVA_DURATION_TICKS = 100; // 5 seconds * 20 ticks per second
    private final Map<BlockPos, LavaBlockInfo> lavaPositions = new HashMap<>();
    
    private static class LavaBlockInfo {
        int ticksLeft;
        BlockState originalState;
        World world;
        
        LavaBlockInfo(int ticksLeft, BlockState originalState, World world) {
            this.ticksLeft = ticksLeft;
            this.originalState = originalState;
            this.world = world;
        }
    }

    public LavaStaff(Settings settings) {
        super(settings);
        // Register the server tick event listener
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        Hand hand = context.getHand();

        if (player == null || world.isClient()) {
            return ActionResult.PASS;
        }

        BlockHitResult hitResult = rayTrace(world, player, RANGE);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos targetPos = hitResult.getBlockPos();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = findTopSolidBlock(world, targetPos.add(dx, 0, dz));
                if (pos != null) {
                    BlockState currentState = world.getBlockState(pos);
                    if (isReplaceable(currentState)) {
                        world.setBlockState(pos, Blocks.LAVA.getDefaultState());
                        // Schedule this lava block for removal
                        lavaPositions.put(pos, new LavaBlockInfo(LAVA_DURATION_TICKS, currentState, world));
                    }
                }
            }
        }

        player.swingHand(hand);
        return ActionResult.SUCCESS;
    }

    private BlockHitResult rayTrace(World world, PlayerEntity player, int range) {
        Vec3d start = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(range));
        return world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
    }

    private BlockPos findTopSolidBlock(World world, BlockPos pos) {
        for (int y = world.getHeight() - 1; y >= world.getBottomY(); y--) {
            BlockPos currentPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = world.getBlockState(currentPos);
            if (state.isSolidBlock(world, currentPos) && isReplaceable(state)) {
                return currentPos;
            }
        }
        return null;
    }

    private boolean isReplaceable(BlockState state) {
        Block block = state.getBlock();
        return block != Blocks.SHORT_GRASS && block != Blocks.TALL_GRASS && block != Blocks.TALL_SEAGRASS;
    }

    private void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<BlockPos, LavaBlockInfo>> iterator = lavaPositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, LavaBlockInfo> entry = iterator.next();
            LavaBlockInfo lavaInfo = entry.getValue();
            int ticksLeft = lavaInfo.ticksLeft - 1;
            if (ticksLeft <= 0) {
                BlockPos pos = entry.getKey();
                if (lavaInfo.world.getBlockState(pos).getBlock() == Blocks.LAVA) {
                    lavaInfo.world.setBlockState(pos, lavaInfo.originalState);
                }
                iterator.remove();
            } else {
                lavaInfo.ticksLeft = ticksLeft;
            }
        }
    }
}