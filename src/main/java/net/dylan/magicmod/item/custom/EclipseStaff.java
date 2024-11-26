package net.dylan.magicmod.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;


public class EclipseStaff extends Item {

    private static final int COOLDOWN_TICKS = 200; // 10 seconds
    private static final int ORB_RADIUS = 10;
    private static final int ORB_DURATION_TICKS = 3 * 20; // 3 seconds in ticks
    private static final float DAMAGE_AMOUNT = 10.0f; // 5 hearts

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public EclipseStaff(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos targetPos = user.getBlockPos().offset(user.getHorizontalFacing(), 5);

            // Apply blindness effect to all players in range except the user
            applyBlindnessEffect(serverWorld, targetPos, user);

            // Create swirling black orb effect
            createSwirlingOrb(serverWorld, targetPos);

            // Apply attraction effect
            applyConstantAttractionEffect(serverWorld, targetPos, user); // Pass the user as the third argument

            // Schedule task to apply implosion effect after ORB_DURATION_TICKS
            scheduler.schedule(() -> {
                applyImplosionEffect(serverWorld, targetPos, user);
            }, ORB_DURATION_TICKS / 20, TimeUnit.SECONDS);
            // Apply cooldown

            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

            // Play activation sound
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void applyBlindnessEffect(ServerWorld world, BlockPos pos, PlayerEntity user) {
        Box effectBox = new Box(pos).expand(ORB_RADIUS);
        for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, effectBox, e -> !e.equals(user))) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0)); // 5 seconds
        }
    }

    private void createSwirlingOrb(ServerWorld world, BlockPos pos) {
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        for (int tick = 0; tick < ORB_DURATION_TICKS / 20; tick++) {
            scheduler.schedule(() -> {
                // Generate particles in a spherical pattern
                for (int i = 0; i < 100; i++) { // Number of particles to spawn
                    double theta = Math.random() * 2 * Math.PI; // Random angle around the Y-axis
                    double phi = Math.acos(2 * Math.random() - 1); // Random angle from the Y-axis
                    double radius = 1.5; // Radius of the sphere

                    double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
                    double y = center.y + radius * Math.sin(phi) * Math.sin(theta);
                    double z = center.z + radius * Math.cos(phi);

                    world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, 0);
                }
            }, tick, TimeUnit.SECONDS);
        }
    }

    private Future<?> attractionTask; // Track the task for cancellation

    private void applyConstantAttractionEffect(ServerWorld world, BlockPos pos, PlayerEntity user) {
        attractionTask = scheduler.scheduleAtFixedRate(() -> {
            Box attractionBox = new Box(pos).expand(ORB_RADIUS);
            Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            for (Entity entity : world.getEntitiesByClass(Entity.class, attractionBox, e -> (e instanceof LivingEntity || e instanceof ItemEntity) && !e.equals(user))) {
                Vec3d direction = center.subtract(entity.getPos()).normalize();
                double distance = center.distanceTo(entity.getPos());
                double pullStrength = 0.1 + (ORB_RADIUS - distance) * 0.05; // Adjust pull strength based on distance
                entity.addVelocity(direction.x * pullStrength, direction.y * pullStrength, direction.z * pullStrength);
                entity.velocityModified = true; // Mark velocity as modified
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // Schedule task to cancel the attraction after the orb duration
        scheduler.schedule(() -> {
            if (attractionTask != null) {
                attractionTask.cancel(false);
            }
        }, ORB_DURATION_TICKS / 20, TimeUnit.SECONDS);
    }

    private void applyImplosionEffect(ServerWorld world, BlockPos pos, PlayerEntity user) {
        Box implosionBox = new Box(pos).expand(ORB_RADIUS);
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        for (Entity entity : world.getEntitiesByClass(Entity.class, implosionBox, e -> (e instanceof LivingEntity || e instanceof ItemEntity) && !e.equals(user))) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(world.getDamageSources().magic(), DAMAGE_AMOUNT);
            }
            Vec3d direction = entity.getPos().subtract(center).normalize();
            entity.addVelocity(direction.x * 0.5, direction.y * 0.5, direction.z * 0.5);
            entity.velocityModified = true; // Mark velocity as modified
        }
    }
}
