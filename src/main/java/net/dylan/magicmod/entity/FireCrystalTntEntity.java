package net.dylan.magicmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class FireCrystalTntEntity extends TntEntity {
    private static final int FIRE_TNT_FUSE = 80;

    public FireCrystalTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
        this.setFuse(FIRE_TNT_FUSE);
    }

    public FireCrystalTntEntity(World world, double x, double y, double z, LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(FIRE_TNT_FUSE);
    }

    @Override
    public void tick() {
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }

        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
        }

        int fuse = this.getFuse() - 1;
        this.setFuse(fuse);
        if (fuse <= 0) {
            this.discard();
            if (!this.getWorld().isClient) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            // Enhanced fire particles
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        float power = 6.0F; // Stronger than regular TNT
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), power, World.ExplosionSourceType.TNT);
        
        // Create fire in surrounding area
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos center = this.getBlockPos();
            int radius = 5;
            
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.add(x, y, z);
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        
                        if (distance <= radius && serverWorld.random.nextFloat() < 0.3f) {
                            if (serverWorld.isAir(pos) && serverWorld.getBlockState(pos.down()).isSolidBlock(serverWorld, pos.down())) {
                                serverWorld.setBlockState(pos, net.minecraft.block.Blocks.FIRE.getDefaultState());
                            }
                        }
                    }
                }
            }
            
            // Add enhanced explosion particles
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, 
                this.getX(), this.getY(), this.getZ(), 
                3, 1.0, 1.0, 1.0, 0.0);
                
            serverWorld.spawnParticles(ParticleTypes.LAVA, 
                this.getX(), this.getY(), this.getZ(), 
                20, 3.0, 3.0, 3.0, 0.5);
                
            serverWorld.spawnParticles(ParticleTypes.FLAME, 
                this.getX(), this.getY(), this.getZ(), 
                30, 4.0, 4.0, 4.0, 0.3);
        }
    }
}