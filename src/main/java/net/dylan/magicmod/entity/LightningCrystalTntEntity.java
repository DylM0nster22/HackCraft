package net.dylan.magicmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class LightningCrystalTntEntity extends TntEntity {
    private static final int LIGHTNING_TNT_FUSE = 60;

    public LightningCrystalTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
        this.setFuse(LIGHTNING_TNT_FUSE);
    }

    public LightningCrystalTntEntity(World world, double x, double y, double z, LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(LIGHTNING_TNT_FUSE);
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
            // Enhanced lightning particles
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.1, 0.0);
            }
        }
    }

    private void explode() {
        float power = 4.0F; // Moderate explosion power
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), power, World.ExplosionSourceType.TNT);
        
        // Create multiple lightning strikes around the explosion
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos center = this.getBlockPos();
            
            // Strike lightning at the center
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(center));
                serverWorld.spawnEntity(lightning);
            }
            
            // Strike lightning in a circle around the explosion
            for (int i = 0; i < 6; i++) {
                double angle = (i / 6.0) * 2 * Math.PI;
                double radius = 3 + serverWorld.random.nextDouble() * 3; // Random radius 3-6 blocks
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                
                // Find the ground level
                BlockPos strikePos = new BlockPos((int) x, center.getY(), (int) z);
                while (strikePos.getY() > center.getY() - 10 && serverWorld.isAir(strikePos)) {
                    strikePos = strikePos.down();
                }
                strikePos = strikePos.up();
                
                LightningEntity surroundingLightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (surroundingLightning != null) {
                    surroundingLightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(strikePos));
                    serverWorld.spawnEntity(surroundingLightning);
                }
            }
            
            // Add enhanced explosion particles
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, 
                this.getX(), this.getY(), this.getZ(), 
                3, 1.0, 1.0, 1.0, 0.0);
                
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                this.getX(), this.getY(), this.getZ(), 
                50, 4.0, 4.0, 4.0, 0.5);
                
            serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                this.getX(), this.getY(), this.getZ(), 
                30, 3.0, 6.0, 3.0, 0.3);
                
            // Soul fire flame for magical effect
            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, 
                this.getX(), this.getY(), this.getZ(), 
                20, 2.0, 3.0, 2.0, 0.2);
        }
    }
}