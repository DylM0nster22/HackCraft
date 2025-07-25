package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ObsidianStaff extends Item {
    private static final int COOLDOWN_TICKS = 240; // 12 seconds * 20 ticks per second
    private static final int BARRIER_RADIUS = 3;
    private static final int BARRIER_HEIGHT = 4; // Total height of the barrier
    private static final int HOLLOW_START_Y = 1; // Start of the hollow section (relative to player's position)
    private static final int HOLLOW_HEIGHT = 2; // Height of the hollow section
    private static final int BARRIER_DURATION_TICKS = 200; // 10 seconds * 20 ticks per second

    // Map from barrier center position to remaining ticks and the world where it was created
    private final Map<BlockPos, BarrierInfo> barrierPositions = new HashMap<>();
    private final Map<UUID, ServerBossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();
    
    private static class BarrierInfo {
        int ticksLeft;
        World world;
        
        BarrierInfo(int ticksLeft, World world) {
            this.ticksLeft = ticksLeft;
            this.world = world;
        }
    }

    public ObsidianStaff(Settings settings) {
        super(settings);
        // Register the server tick event listener
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            // Check custom cooldown instead of item cooldown manager for boss bar support
            if (playerCooldowns.containsKey(user.getUuid()) && playerCooldowns.get(user.getUuid()) > 0) {
                return TypedActionResult.pass(stack); // Still on cooldown
            }
            
            BlockPos userPos = user.getBlockPos();
            createObsidianBarrier(world, userPos);
            
            // Set cooldown and create boss bar
            playerCooldowns.put(user.getUuid(), COOLDOWN_TICKS);
            if (user instanceof ServerPlayerEntity serverPlayer) {
                createCooldownBossBar(serverPlayer);
            }
            
            // Play sound
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_STONE_PLACE, SoundCategory.PLAYERS, 1.0F, 0.8F);
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
                                // Add portal particles for more dramatic effect
                                ((ServerWorld) world).spawnParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.1);
                            }
                        }
                    }
                }
            }
        }

        // Schedule removal after 10 seconds (200 ticks)
        barrierPositions.put(center, new BarrierInfo(BARRIER_DURATION_TICKS, world));
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

    private void createCooldownBossBar(ServerPlayerEntity player) {
        ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Obsidian Staff Cooldown"), 
            BossBar.Color.PURPLE, 
            BossBar.Style.PROGRESS
        );
        bossBar.addPlayer(player);
        bossBar.setPercent(1.0f);
        playerBossBars.put(player.getUuid(), bossBar);
    }

    private void updateCooldownBossBar(ServerPlayerEntity player, int ticksLeft) {
        ServerBossBar bossBar = playerBossBars.get(player.getUuid());
        if (bossBar != null) {
            float progress = (float) ticksLeft / COOLDOWN_TICKS;
            bossBar.setPercent(progress);
            if (ticksLeft <= 0) {
                bossBar.removePlayer(player);
                playerBossBars.remove(player.getUuid());
            }
        }
    }

    private void onServerTick(MinecraftServer server) {
    private void onServerTick(MinecraftServer server) {
        // Handle barrier removal
        Iterator<Map.Entry<BlockPos, BarrierInfo>> iterator = barrierPositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, BarrierInfo> entry = iterator.next();
            BarrierInfo barrierInfo = entry.getValue();
            int ticksLeft = barrierInfo.ticksLeft - 1;
            if (ticksLeft <= 0) {
                removeObsidianBarrier(barrierInfo.world, entry.getKey());
                iterator.remove();
            } else {
                barrierInfo.ticksLeft = ticksLeft;
            }
        }

        // Handle player cooldowns and boss bars
        Iterator<Map.Entry<UUID, Integer>> cooldownIterator = playerCooldowns.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = cooldownIterator.next();
            UUID playerId = entry.getKey();
            int ticksLeft = entry.getValue() - 1;
            
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                updateCooldownBossBar(player, ticksLeft);
            }
            
            if (ticksLeft <= 0) {
                cooldownIterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
    }
}
