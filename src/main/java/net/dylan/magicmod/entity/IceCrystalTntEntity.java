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

public class IceCrystalTntEntity extends TntEntity {
    private static final int ICE_TNT_FUSE = 80;

    public IceCrystalTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
        this.setFuse(ICE_TNT_FUSE);
    }

    public IceCrystalTntEntity(World world, double x, double y, double z, LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(ICE_TNT_FUSE);
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
            // Enhanced ice particles
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        float power = 5.0F; // Moderate explosion power
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), power, World.ExplosionSourceType.TNT);
        
        // Create ice blocks in surrounding area
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos center = this.getBlockPos();
            int radius = 6;
            
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.add(x, y, z);
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        
                        if (distance <= radius && serverWorld.random.nextFloat() < 0.4f) {
                            if (serverWorld.isAir(pos)) {
                                serverWorld.setBlockState(pos, net.minecraft.block.Blocks.ICE.getDefaultState());
                            } else if (serverWorld.getBlockState(pos).isOf(net.minecraft.block.Blocks.WATER)) {
                                serverWorld.setBlockState(pos, net.minecraft.block.Blocks.ICE.getDefaultState());
                            }
                        }
                    }
                }
            }
            
            // Add enhanced explosion particles
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, 
                this.getX(), this.getY(), this.getZ(), 
                2, 1.0, 1.0, 1.0, 0.0);
                
            serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, 
                this.getX(), this.getY(), this.getZ(), 
                40, 4.0, 4.0, 4.0, 0.3);
                
            serverWorld.spawnParticles(ParticleTypes.ITEM_SNOWBALL, 
                this.getX(), this.getY(), this.getZ(), 
                30, 3.0, 3.0, 3.0, 0.5);
                
            // Ice crystal particles for magical effect
            serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                this.getX(), this.getY(), this.getZ(), 
                15, 2.0, 2.0, 2.0, 0.2);
        }
    }
}