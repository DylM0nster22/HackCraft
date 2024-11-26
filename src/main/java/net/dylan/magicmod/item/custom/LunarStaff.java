package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class LunarStaff extends Item {
    private static final int COOLDOWN_TICKS = 120; // 6 seconds
    private static final double RANGE = 30.0;
    private static final double BEAM_RADIUS = 1.0;
    private static final int DAMAGE = 4; // 2 hearts
    private static final int SLOWNESS_DURATION = 60; // 3 seconds
    private static final int SLOWNESS_AMPLIFIER = 1; // Slowness II

    public LunarStaff(Settings settings) {
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

        // Check if it's nighttime (12000 to 24000 ticks)
        if (timeOfDay < 12000 || timeOfDay >= 24000) {
            if (!world.isClient) {
                player.sendMessage(Text.literal("The Lunar Staff can only be used during the night."), true);
            }
            return ActionResult.FAIL;
        }

        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(RANGE));

        // Raycast to find the target block or entity
        HitResult hitResult = world.raycast(new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.MISS) {
            return ActionResult.FAIL;
        }

        Vec3d hitPos = hitResult.getPos();

        // Play sound
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Spawn particles along the beam path
        for (double d = 0; d < RANGE; d += 0.5) {
            Vec3d particlePos = startPos.add(direction.multiply(d));
            world.addParticle(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }

        // Apply effects to entities within the beam's radius at the target location
        Box box = new Box(hitPos.add(-BEAM_RADIUS, -BEAM_RADIUS, -BEAM_RADIUS), hitPos.add(BEAM_RADIUS, BEAM_RADIUS, BEAM_RADIUS));
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, entity -> entity != player);
        for (LivingEntity entity : entities) {
            entity.damage(player.getDamageSources().playerAttack(player), DAMAGE);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER));
        }

        // Set cooldown
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        return ActionResult.SUCCESS;
    }
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Right-click to shoot a beam of moonlight.").formatted(Formatting.WHITE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}
