package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NecromancyStaff extends Item {
    private static final int COOLDOWN_TICKS = 600; // 30 seconds cooldown
    private static final int SUMMON_COUNT = 3; // Number of undead to summon
    private final Map<UUID, ServerBossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public NecromancyStaff(Settings settings) {
        super(settings);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Summons undead minions to fight alongside you").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("Creates 3 undead allies for 60 seconds").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("30 second cooldown").formatted(Formatting.RED));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Check cooldown
        if (playerCooldowns.containsKey(user.getUuid()) && playerCooldowns.get(user.getUuid()) > 0) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        if (!world.isClient) {
            summonUndead(world, user);
            
            // Set cooldown and create boss bar
            playerCooldowns.put(user.getUuid(), COOLDOWN_TICKS);
            if (user instanceof ServerPlayerEntity serverPlayer) {
                createCooldownBossBar(serverPlayer);
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void summonUndead(World world, PlayerEntity player) {
        // Play necromancy sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5F, 0.8F);

        if (world instanceof ServerWorld serverWorld) {
            Vec3d playerPos = player.getPos();
            
            // Spawn dark particles in a circle
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double x = playerPos.x + Math.cos(angle) * 3;
                double z = playerPos.z + Math.sin(angle) * 3;
                serverWorld.spawnParticles(ParticleTypes.SOUL, 
                    x, playerPos.y + 0.5, z, 
                    3, 0.2, 0.5, 0.2, 0.05);
            }

            // Summon undead minions
            for (int i = 0; i < SUMMON_COUNT; i++) {
                double angle = (i / (double) SUMMON_COUNT) * 2 * Math.PI;
                double x = playerPos.x + Math.cos(angle) * 2;
                double z = playerPos.z + Math.sin(angle) * 2;
                BlockPos spawnPos = new BlockPos((int) x, (int) playerPos.y, (int) z);
                
                // Spawn randomly between skeleton and zombie
                if (world.random.nextBoolean()) {
                    SkeletonEntity skeleton = EntityType.SKELETON.create(serverWorld);
                    if (skeleton != null) {
                        skeleton.refreshPositionAndAngles(x, playerPos.y, z, 0, 0);
                        skeleton.initialize(serverWorld, serverWorld.getLocalDifficulty(spawnPos), SpawnReason.MOB_SUMMONED, null);
                        skeleton.setPersistent();
                        serverWorld.spawnEntity(skeleton);
                        
                        // Spawn summon particles
                        serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, 
                            x, playerPos.y + 1, z, 
                            10, 0.3, 0.5, 0.3, 0.1);
                    }
                } else {
                    ZombieEntity zombie = EntityType.ZOMBIE.create(serverWorld);
                    if (zombie != null) {
                        zombie.refreshPositionAndAngles(x, playerPos.y, z, 0, 0);
                        zombie.initialize(serverWorld, serverWorld.getLocalDifficulty(spawnPos), SpawnReason.MOB_SUMMONED, null);
                        zombie.setPersistent();
                        serverWorld.spawnEntity(zombie);
                        
                        // Spawn summon particles
                        serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, 
                            x, playerPos.y + 1, z, 
                            10, 0.3, 0.5, 0.3, 0.1);
                    }
                }
            }
        }
    }

    private void createCooldownBossBar(ServerPlayerEntity player) {
        ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Necromancy Staff Cooldown"), 
            BossBar.Color.PURPLE, 
            BossBar.Style.NOTCHED_10
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