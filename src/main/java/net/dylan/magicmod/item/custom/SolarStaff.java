package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SolarStaff extends Item {
    private static final int COOLDOWN_TICKS = 120; // 6 seconds
    private static final double RANGE = 5.0;
    private static final double CONE_ANGLE = Math.toRadians(45); // 45-degree cone

    public SolarStaff(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return activateStaff(user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return activateStaff(context.getPlayer(), context.getHand());
    }

    private ActionResult activateStaff(PlayerEntity player, Hand hand) {
        if (player == null || player.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.FAIL;
        }

        World world = player.getWorld();
        long timeOfDay = world.getTimeOfDay() % 24000;

        // Check if it's daytime (0 to 12000 ticks)
        if (timeOfDay < 0 || timeOfDay >= 12000) {
            if (!world.isClient) {
                player.sendMessage(Text.literal("The Solar Staff can only be used during the day."), true);
            }
            return ActionResult.FAIL;
        }

        Vec3d playerPos = player.getPos();
        Vec3d lookVec = player.getRotationVec(1.0F);

        // Play sound
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Spawn particles
        for (int i = 0; i < 100; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
            Vec3d particlePos = playerPos.add(lookVec.multiply(2)).add(offsetX, offsetY, offsetZ);
            world.addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }

        // Apply effects to entities
        Box box = new Box(playerPos, playerPos.add(lookVec.multiply(RANGE))).expand(2.0);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, entity -> entity != player);
        for (LivingEntity entity : entities) {
            Vec3d directionToEntity = entity.getPos().subtract(playerPos).normalize();
            double angle = Math.acos(lookVec.dotProduct(directionToEntity));
            if (angle <= CONE_ANGLE) {
                entity.setOnFireFor(5);
                Vec3d knockback = directionToEntity.multiply(1.5);
                entity.addVelocity(knockback.x, knockback.y, knockback.z);
            }
        }

        // Set cooldown
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        return ActionResult.SUCCESS;
    }
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Right-click to make a wall of solar energy.").formatted(Formatting.YELLOW));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}
