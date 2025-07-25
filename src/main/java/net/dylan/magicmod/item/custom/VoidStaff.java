package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoidStaff extends Item {
    private static final int COOLDOWN_TICKS = 800; // 40 seconds cooldown
    private static final int VOID_DURATION = 100; // 5 seconds void effect
    private static final double VOID_RADIUS = 8.0; // 8 block radius
    private final Map<UUID, ServerBossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public VoidStaff(Settings settings) {
        super(settings);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Creates a void rift that damages and disorients enemies").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("Deals 4 hearts of void damage in 8 block radius").formatted(Formatting.RED));
            tooltip.add(Text.literal("Effect lasts 5 seconds").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("40 second cooldown").formatted(Formatting.RED));
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
            createVoidRift(world, user);
            
            // Set cooldown and create boss bar
            playerCooldowns.put(user.getUuid(), COOLDOWN_TICKS);
            if (user instanceof ServerPlayerEntity serverPlayer) {
                createCooldownBossBar(serverPlayer);
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void createVoidRift(World world, PlayerEntity player) {
        // Find the target location
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(20)); // 20 block range

        RaycastContext context = new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        );
        BlockHitResult hitResult = world.raycast(context);

        Vec3d riftPos;
        if (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            riftPos = hitResult.getPos();
        } else {
            riftPos = endPos;
        }

        // Play void sound
        world.playSound(null, riftPos.x, riftPos.y, riftPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0F, 0.5F);

        if (world instanceof ServerWorld serverWorld) {
            // Create the void rift effect over time
            createVoidRiftEffect(serverWorld, riftPos);
        }
    }

    private void createVoidRiftEffect(ServerWorld world, Vec3d centerPos) {
        // Schedule void effect to happen over multiple ticks
        for (int tick = 0; tick < VOID_DURATION; tick += 5) {
            world.getServer().execute(() -> {
                // Create void particles in a sphere
                for (int i = 0; i < 30; i++) {
                    double phi = Math.random() * 2 * Math.PI;
                    double costheta = Math.random() * 2 - 1;
                    double theta = Math.acos(costheta);
                    double radius = VOID_RADIUS * Math.cbrt(Math.random());
                    
                    double x = centerPos.x + radius * Math.sin(theta) * Math.cos(phi);
                    double y = centerPos.y + radius * Math.sin(theta) * Math.sin(phi);
                    double z = centerPos.z + radius * Math.cos(theta);
                    
                    // Dark particles for void effect
                    world.spawnParticles(ParticleTypes.PORTAL, 
                        x, y, z, 
                        1, 0.0, 0.0, 0.0, 0.5);
                        
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE, 
                        x, y, z, 
                        1, 0.1, 0.1, 0.1, 0.0);
                }

                // Create central void vortex
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL, 
                    centerPos.x, centerPos.y, centerPos.z, 
                    10, 0.5, 0.5, 0.5, 1.0);

                // Damage entities in range
                Box damageBox = new Box(centerPos.subtract(VOID_RADIUS, VOID_RADIUS, VOID_RADIUS),
                                      centerPos.add(VOID_RADIUS, VOID_RADIUS, VOID_RADIUS));
                List<Entity> entities = world.getOtherEntities(null, damageBox);

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !(entity instanceof PlayerEntity)) {
                        double distance = entity.getPos().distanceTo(centerPos);
                        if (distance <= VOID_RADIUS) {
                            // Damage decreases with distance
                            float damage = (float) (8.0 * (1.0 - distance / VOID_RADIUS)); // Up to 4 hearts
                            
                            DamageSource voidDamage = world.getDamageSources().magic();
                            livingEntity.damage(voidDamage, damage);
                            
                            // Pull entities toward the center
                            Vec3d pullDirection = centerPos.subtract(entity.getPos()).normalize().multiply(0.3);
                            entity.setVelocity(entity.getVelocity().add(pullDirection));
                            
                            // Add void particles around damaged entities
                            world.spawnParticles(ParticleTypes.SOUL, 
                                entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), 
                                5, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }

                // Play ambient void sounds periodically
                if (world.random.nextInt(10) == 0) {
                    world.playSound(null, centerPos.x, centerPos.y, centerPos.z,
                            SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 0.8F, 0.3F);
                }
            });
        }
    }

    private void createCooldownBossBar(ServerPlayerEntity player) {
        ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Void Staff Cooldown"), 
            BossBar.Color.PURPLE, 
            BossBar.Style.NOTCHED_20
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