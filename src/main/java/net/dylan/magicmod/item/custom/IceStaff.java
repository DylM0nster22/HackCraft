package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IceStaff extends Item {
    private static final int ICE_DURATION_TICKS = 100; // 5 seconds * 20 ticks per second
    private static final int COOLDOWN_TICKS = 60; // 3 seconds cooldown
    private final Map<BlockPos, Integer> icePositions = new HashMap<>();
    private final Map<UUID, ServerBossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public IceStaff(Settings settings) {
        super(settings);
        // Register the server tick event listener
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private enum Element {
        ICE;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("When you use this item within 8 block of a entity it will trap the entity in a cube of ice").formatted(Formatting.BLUE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    private void useIceElement(World world, PlayerEntity player) {
        // Check cooldown
        if (playerCooldowns.containsKey(player.getUuid()) && playerCooldowns.get(player.getUuid()) > 0) {
            return; // Still on cooldown
        }

        // Play ice sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            // Set cooldown and create boss bar
            playerCooldowns.put(player.getUuid(), COOLDOWN_TICKS);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                createCooldownBossBar(serverPlayer);
            }

            // Perform a ray trace to find the targeted entity with extended range (50 blocks)
            Vec3d startPos = player.getCameraPosVec(1.0F);
            Vec3d direction = player.getRotationVec(1.0F);
            Vec3d endPos = startPos.add(direction.multiply(50)); // Extended range to 50 blocks

            EntityHitResult entityHitResult = ProjectileUtil.raycast(player, startPos, endPos,
                    player.getBoundingBox().stretch(direction.multiply(50)).expand(1.0D),
                    entity -> !entity.isSpectator() && entity.isAlive(),
                    50);

            if (entityHitResult != null) {
                Entity target = entityHitResult.getEntity();
                BlockPos targetPos = target.getBlockPos();

                // Determine the size of the encasement based on the entity's bounding box
                Box boundingBox = target.getBoundingBox();
                double targetWidth = boundingBox.maxX - boundingBox.minX;
                double targetHeight = boundingBox.maxY - boundingBox.minY;
                int encasementRadius = (int) Math.ceil(targetWidth / 2);
                int encasementHeight = (int) Math.ceil(targetHeight);

                // Define the area around the entity to encase in ice, scaling with the entity's size
                BlockPos.Mutable mutablePos = new BlockPos.Mutable();
                for (int x = -encasementRadius; x <= encasementRadius; x++) {
                    for (int y = 0; y <= encasementHeight; y++) {
                        for (int z = -encasementRadius; z <= encasementRadius; z++) {
                            mutablePos.set(targetPos.getX() + x, targetPos.getY() + y, targetPos.getZ() + z);
                            if (world.getBlockState(mutablePos).isAir()) {
                                world.setBlockState(mutablePos, Blocks.ICE.getDefaultState());
                                // Schedule this ice block for removal
                                icePositions.put(mutablePos.toImmutable(), ICE_DURATION_TICKS);
                                
                                // Add ice particles
                                if (world instanceof ServerWorld serverWorld) {
                                    serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, 
                                        mutablePos.getX() + 0.5, mutablePos.getY() + 0.5, mutablePos.getZ() + 0.5, 
                                        3, 0.3, 0.3, 0.3, 0.1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createCooldownBossBar(ServerPlayerEntity player) {
        ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Ice Staff Cooldown"), 
            BossBar.Color.BLUE, 
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
        // Handle ice block removal
        Iterator<Map.Entry<BlockPos, Integer>> iterator = icePositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                // Remove ice blocks from all worlds
                for (World world : server.getWorlds()) {
                    BlockPos pos = entry.getKey();
                    if (world.getBlockState(pos).isOf(Blocks.ICE)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        // Add break particles
                        if (world instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.ITEM_SNOWBALL, 
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                                8, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
                iterator.remove();
            } else {
                entry.setValue(ticksLeft);
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        useIceElement(world, user);
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}



