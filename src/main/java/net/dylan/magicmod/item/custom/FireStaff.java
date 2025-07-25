package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class FireStaff extends Item {
    public FireStaff(Settings settings) {
        super(settings);
    }

    // Enum for different elements
    private enum Element {
        FIRE;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private void useFireElement(World world, PlayerEntity player) {
        // Play fire sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            // Create and launch the fireball entity
            Vec3d look = player.getRotationVec(1.0F);
            double x = player.getX() + look.x * 2;
            double y = player.getEyeY() + look.y * 2;
            double z = player.getZ() + look.z * 2;

            FireballEntity fireball = new FireballEntity(EntityType.FIREBALL, world);
            fireball.setOwner(player);
            fireball.setPos(x, y, z);
            fireball.setVelocity(look.x, look.y, look.z, 1.5F, 1.0F);

            world.spawnEntity(fireball);

            // Add enhanced particle effects around the player
            if (world instanceof ServerWorld serverWorld) {
                // Fire particles around the staff
                Vec3d playerPos = player.getPos();
                serverWorld.spawnParticles(ParticleTypes.FLAME, 
                    playerPos.x, playerPos.y + 1.5, playerPos.z, 
                    15, 0.5, 0.5, 0.5, 0.1);
                    
                // Smoke trail
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, 
                    playerPos.x, playerPos.y + 1.5, playerPos.z, 
                    8, 0.3, 0.3, 0.3, 0.05);
                    
                // Lava particles for dramatic effect
                serverWorld.spawnParticles(ParticleTypes.LAVA, 
                    playerPos.x, playerPos.y + 1.5, playerPos.z, 
                    5, 0.4, 0.4, 0.4, 0.1);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        useFireElement(world, user);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("When you use this item it will shoot a fireball").formatted(Formatting.RED));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

}
