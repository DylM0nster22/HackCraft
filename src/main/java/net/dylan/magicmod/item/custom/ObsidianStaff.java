package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ObsidianStaff extends Item {
    private static final int COOLDOWN_TICKS = 240; // 12 seconds * 20 ticks per second
    private static final int BARRIER_RADIUS = 3;
    private static final int BARRIER_HEIGHT = 4; // Total height of the barrier
    private static final int HOLLOW_START_Y = 1; // Start of the hollow section (relative to player's position)
    private static final int HOLLOW_HEIGHT = 2; // Height of the hollow section
    private static final int BARRIER_DURATION_TICKS = 200; // 10 seconds * 20 ticks per second

    private final Map<BlockPos, Integer> barrierPositions = new HashMap<>();

    public ObsidianStaff(Settings settings) {
        super(settings);
        // Register the server tick event listener
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && !user.getItemCooldownManager().isCoolingDown(this)) {
            BlockPos userPos = user.getBlockPos();
            createObsidianBarrier(world, userPos);
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void createObsidianBarrier(World world, BlockPos center) {
        for (int dx = -BARRIER_RADIUS; dx <= BARRIER_RADIUS; dx++) {
            for (int dz = -BARRIER_RADIUS; dz <= BARRIER_RADIUS; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) <= BARRIER_RADIUS) {
                    for (int dy = 0; dy < BARRIER_HEIGHT; dy++) {
                        BlockPos pos = center.add(dx, dy, dz);

                        // Define the hollow region
                        boolean isHollowY = dy >= HOLLOW_START_Y && dy < (HOLLOW_START_Y + HOLLOW_HEIGHT);
                        boolean isCenterX = dx == 0;
                        boolean isCenterZ = dz == 0;

                        // Skip the hollow center
                        if (isHollowY && isCenterX && isCenterZ) {
                            continue;
                        }

                        if (world.isAir(pos)) {
                            world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                            if (world instanceof ServerWorld) {
                                ((ServerWorld) world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
                            }
                        }
                    }
                }
            }
        }

        // Schedule removal after 10 seconds (200 ticks)
        barrierPositions.put(center, BARRIER_DURATION_TICKS);
    }

    private void removeObsidianBarrier(World world, BlockPos center) {
        for (int dx = -BARRIER_RADIUS; dx <= BARRIER_RADIUS; dx++) {
            for (int dz = -BARRIER_RADIUS; dz <= BARRIER_RADIUS; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) <= BARRIER_RADIUS) {
                    for (int dy = 0; dy < BARRIER_HEIGHT; dy++) {
                        BlockPos pos = center.add(dx, dy, dz);
                        if (world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) {
                            world.breakBlock(pos, false);
                            if (world instanceof ServerWorld) {
                                ((ServerWorld) world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<BlockPos, Integer>> iterator = barrierPositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                ServerWorld world = server.getWorld(World.OVERWORLD);
                if (world != null) {
                    removeObsidianBarrier(world, entry.getKey());
                }
                iterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
    }
}
