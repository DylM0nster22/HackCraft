package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;


import java.util.List;

public class LightningStaff extends Item {
    public LightningStaff(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            // Set up ray tracing parameters
            Vec3d startPos = player.getCameraPosVec(1.0F);
            Vec3d direction = player.getRotationVec(1.0F);
            Vec3d endPos = startPos.add(direction.multiply(100)); // Adjust distance as needed

            // First, try to find an entity along the ray
            EntityHitResult entityHitResult = findEntityHit(world, player, startPos, endPos);
            if (entityHitResult != null && entityHitResult.getEntity() != null) {
                // Target the entity with lightning
                spawnLightning(world, entityHitResult.getEntity().getPos());
            } else {
                // If no entity was hit, do a block ray trace
                RaycastContext context = new RaycastContext(
                        startPos,
                        endPos,
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        player
                );
                HitResult hitResult = world.raycast(context);

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos targetPos = blockHitResult.getBlockPos().up(); // Target the block above
                    spawnLightning(world, Vec3d.ofBottomCenter(targetPos));
                }
            }
        }

        // Play sound and return
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.0F);
        return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
    }

    private void spawnLightning(World world, Vec3d position) {
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(position);
            world.spawnEntity(lightning);
        }
    }

    private EntityHitResult findEntityHit(World world, PlayerEntity player, Vec3d start, Vec3d end) {
        Box box = new Box(start, end).expand(1.0);
        List<Entity> entities = world.getOtherEntities(player, box, entity -> entity.isCollidable());
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            Vec3d hitPos = entityBox.raycast(start, end).orElse(null);
            if (hitPos != null) {
                double distance = start.squaredDistanceTo(hitPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        return closestEntity != null ? new EntityHitResult(closestEntity) : null;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("When you use this item it will shoot a lightning bolt where your looking").formatted(Formatting.DARK_AQUA));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

}



