package net.dylan.magicmod.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class WindStaff extends Item {
    private static final int COOLDOWN_TICKS = 100; // 5 seconds cooldown
    private static final int TORNADO_DURATION_TICKS = 60; // 3 seconds duration
    private static final double INITIAL_RADIUS = 2.0;
    private static final double MAX_RADIUS = 5.0;
    private static final double MAX_HEIGHT = 20.0;
    private static final double LIFT_SPEED = 0.5;
    private static final double ROTATION_SPEED = 0.5;

    // Set to keep track of active tornadoes
    private static final Set<TornadoEffect> activeTornadoes = new HashSet<>();

    public WindStaff(Settings settings) {
        super(settings);
        // Register the tick event listener
        ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            Vec3d pos = user.getPos().add(0, user.getStandingEyeHeight(), 0).add(user.getRotationVec(1.0F).multiply(5));

            // Play activation sound
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.0F);

            // Apply cooldown
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

            // Create and start the tornado effect
            TornadoEffect tornado = new TornadoEffect(serverWorld, pos, INITIAL_RADIUS, MAX_RADIUS, TORNADO_DURATION_TICKS);
            activeTornadoes.add(tornado);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void onWorldTick(ServerWorld world) {
        // Iterate over active tornadoes and update them
        activeTornadoes.removeIf(tornado -> {
            if (tornado.world == world) {
                return tornado.tick();
            }
            return false;
        });
    }

    private static class TornadoEffect {
        private final ServerWorld world;
        private final Vec3d center;
        private final double initialRadius;
        private final double maxRadius;
        private final int durationTicks;
        private int ticksElapsed;

        public TornadoEffect(ServerWorld world, Vec3d center, double initialRadius, double maxRadius, int durationTicks) {
            this.world = world;
            this.center = center;
            this.initialRadius = initialRadius;
            this.maxRadius = maxRadius;
            this.durationTicks = durationTicks;
            this.ticksElapsed = 0;
        }

        public boolean tick() {
            if (ticksElapsed >= durationTicks) {
                return true; // Indicate that the tornado has finished
            }

            double progress = (double) ticksElapsed / durationTicks;
            double currentRadius = initialRadius + (maxRadius - initialRadius) * progress;
            double currentHeight = MAX_HEIGHT * progress;

            // Lift and rotate entities
            Box box = new Box(center.add(-currentRadius, -currentHeight, -currentRadius), center.add(currentRadius, currentHeight, currentRadius));
            for (Entity entity : world.getEntitiesByClass(Entity.class, box, e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
                if (entity.squaredDistanceTo(center) <= currentRadius * currentRadius) {
                    double angle = Math.atan2(entity.getZ() - center.z, entity.getX() - center.x) + ROTATION_SPEED;
                    double distance = Math.sqrt(entity.squaredDistanceTo(center));
                    double newX = center.x + distance * Math.cos(angle);
                    double newZ = center.z + distance * Math.sin(angle);
                    double newY = center.y + currentHeight * progress;

                    Vec3d newPos = new Vec3d(newX, newY, newZ);
                    Vec3d velocity = newPos.subtract(entity.getPos()).multiply(0.2);
                    entity.setVelocity(velocity);
                    entity.velocityModified = true;
                }
            }

            // Spawn particles
            spawnParticles(currentRadius, currentHeight);

            ticksElapsed++;
            return false; // Indicate that the tornado is still active
        }

        private void spawnParticles(double radius, double height) {
            for (int i = 0; i < 20; i++) {
                double angle = world.random.nextDouble() * 2 * Math.PI;
                double distance = world.random.nextDouble() * radius;
                double x = center.x + distance * Math.cos(angle);
                double z = center.z + distance * Math.sin(angle);
                double y = center.y + world.random.nextDouble() * height;

                world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
