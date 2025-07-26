package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class LightningLens extends Item {
    public LightningLens(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Focuses electrical energy into precise strikes").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("Right-click to call lightning at target").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("More accurate than a staff").formatted(Formatting.BLUE));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            focusLightning(world, player);
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    private void focusLightning(World world, PlayerEntity player) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(100));

        RaycastContext context = new RaycastContext(
            startPos, endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        
        BlockHitResult hitResult = world.raycast(context);
        Vec3d strikePos;
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            strikePos = Vec3d.ofBottomCenter(hitPos.up());
        } else {
            strikePos = endPos;
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.2F);

        if (world instanceof ServerWorld serverWorld) {
            // Create focused beam effect
            Vec3d step = strikePos.subtract(startPos).multiply(0.1);
            for (int i = 1; i < 10; i++) {
                Vec3d beamPos = startPos.add(step.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    beamPos.x, beamPos.y, beamPos.z,
                    1, 0.05, 0.05, 0.05, 0.3);
            }
            
            // Strike lightning
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(strikePos);
                world.spawnEntity(lightning);
            }
        }
    }
}