package net.dylan.magicmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BoulderEntity extends ProjectileEntity {

    public BoulderEntity(EntityType<? extends BoulderEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initDataTracker() {
        // Initialize data parameters if needed
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    public void tick() {
        super.tick();
        // Add custom behavior here
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient()) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                entityHitResult.getEntity().damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), 10.0F);
            }
            this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        // Write custom data to NBT
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        // Read custom data from NBT
    }
}
