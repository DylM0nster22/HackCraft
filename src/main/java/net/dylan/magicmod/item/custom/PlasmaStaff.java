package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class PlasmaStaff extends Item {
    private static final int PLASMA_BOLT_COUNT = 8;
    
    public PlasmaStaff(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Fires superheated plasma bolts").formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.literal("Shoots 8 plasma projectiles in a spread").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Creates intense electrical and fire effects").formatted(Formatting.RED));
            tooltip.add(Text.literal("High damage energy weapon").formatted(Formatting.DARK_RED));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            firePlasmaBarrage(world, player);
        }
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void firePlasmaBarrage(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        
        // Play plasma charging sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 2.0F);

        if (world instanceof ServerWorld serverWorld) {
            // Create charging effect
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = 0.5 + Math.random() * 1.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + 1 + Math.random() * 1;
                
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    1, 0.1, 0.1, 0.1, 0.3);
                    
                serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    1, 0.05, 0.05, 0.05, 0.1);
            }

            // Fire plasma bolts in a spread pattern
            for (int i = 0; i < PLASMA_BOLT_COUNT; i++) {
                // Create spread pattern
                double spreadAngle = (i - PLASMA_BOLT_COUNT / 2.0) * 0.2; // 0.2 radians spread
                Vec3d rotatedDirection = rotateVector(lookDirection, spreadAngle);
                
                // Create plasma bolt (using small fireball with enhanced effects)
                SmallFireballEntity plasmaBolt = new SmallFireballEntity(world, player, 
                    rotatedDirection.x, rotatedDirection.y, rotatedDirection.z);
                
                Vec3d spawnPos = playerPos.add(lookDirection.multiply(2));
                plasmaBolt.setPos(spawnPos.x, spawnPos.y + 1.5, spawnPos.z);
                plasmaBolt.setVelocity(rotatedDirection.multiply(2.5));
                
                world.spawnEntity(plasmaBolt);
                
                // Create plasma trail effect
                for (int j = 0; j < 5; j++) {
                    Vec3d trailPos = spawnPos.add(rotatedDirection.multiply(j * 0.5));
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        trailPos.x, trailPos.y, trailPos.z,
                        2, 0.1, 0.1, 0.1, 0.2);
                        
                    serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        trailPos.x, trailPos.y, trailPos.z,
                        1, 0.05, 0.05, 0.05, 0.1);
                }
            }

            // Create muzzle flash effect
            Vec3d muzzlePos = playerPos.add(lookDirection.multiply(2.5));
            serverWorld.spawnParticles(ParticleTypes.FLASH,
                muzzlePos.x, muzzlePos.y + 1.5, muzzlePos.z,
                1, 0, 0, 0, 0);
                
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                muzzlePos.x, muzzlePos.y + 1.5, muzzlePos.z,
                3, 0.3, 0.3, 0.3, 0.1);

            // Play firing sound
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        }
    }

    private Vec3d rotateVector(Vec3d vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        // Rotate around Y axis
        double newX = vector.x * cos - vector.z * sin;
        double newZ = vector.x * sin + vector.z * cos;
        
        return new Vec3d(newX, vector.y, newZ);
    }
}