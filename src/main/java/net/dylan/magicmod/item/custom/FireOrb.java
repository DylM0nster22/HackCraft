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

public class FireOrb extends Item {
    public FireOrb(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("A concentrated sphere of fire magic").formatted(Formatting.RED));
            tooltip.add(Text.literal("Right-click to launch a powerful fireball").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("More compact than a staff but equally powerful").formatted(Formatting.YELLOW));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            launchFireball(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void launchFireball(World world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        
        // Play sound
        world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.8F);

        if (world instanceof ServerWorld serverWorld) {
            // Create enhanced fireball
            SmallFireballEntity fireball = new SmallFireballEntity(world, player, 
                lookDirection.x, lookDirection.y, lookDirection.z);
            
            Vec3d spawnPos = playerPos.add(lookDirection.multiply(1.5));
            fireball.setPos(spawnPos.x, spawnPos.y + 1.5, spawnPos.z);
            fireball.setVelocity(lookDirection.multiply(3.0));
            
            world.spawnEntity(fireball);
            
            // Create orb activation particles
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                playerPos.x, playerPos.y + 1.5, playerPos.z,
                15, 0.3, 0.3, 0.3, 0.1);
                
            serverWorld.spawnParticles(ParticleTypes.LAVA,
                playerPos.x, playerPos.y + 1.5, playerPos.z,
                5, 0.2, 0.2, 0.2, 0.05);
        }
    }
}