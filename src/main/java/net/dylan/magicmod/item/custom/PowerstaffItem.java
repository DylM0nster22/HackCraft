package net.dylan.magicmod.item.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PowerstaffItem extends Item {
    private Element currentElement = Element.FIRE;
    private long lastToggleTime = 0;
    private static final long TOGGLE_COOLDOWN = 500; // cooldown in milliseconds

    public PowerstaffItem(Settings settings) {
        super(settings);
    }

    // Enum for different elements
    private enum Element {
        FIRE,
        LIGHTNING
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            // Toggle element only if the cooldown period has passed
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastToggleTime > TOGGLE_COOLDOWN) {
                toggleElement(player);
                lastToggleTime = currentTime; // Update last toggle time
            }
        } else {
            // Use the staff based on the current element
            switch (currentElement) {
                case FIRE:
                    useFireElement(world, player);
                    break;
                case LIGHTNING:
                    useLightningElement(world, player);
                    break;
            }
        }

        // Return the action result to indicate success
        return TypedActionResult.success(itemStack, world.isClient());
    }

    private void toggleElement(PlayerEntity player) {
        if (currentElement == Element.FIRE) {
            currentElement = Element.LIGHTNING;
            player.sendMessage(Text.literal("Changed element to Lightning"), true);
        } else {
            currentElement = Element.FIRE;
            player.sendMessage(Text.literal("Changed element to Fire"), true);
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
        }
    }

    private void useLightningElement(World world, PlayerEntity player) {
        // Play lightning sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            // Perform a ray trace from the player's eyes to a maximum distance (e.g., 100 blocks)
            Vec3d startPos = player.getCameraPosVec(1.0F);
            Vec3d endPos = startPos.add(player.getRotationVec(1.0F).multiply(100)); // Adjust distance as needed

            BlockHitResult hitResult = world.raycast(new net.minecraft.world.RaycastContext(
                    startPos, endPos,
                    net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    player
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = hitResult.getBlockPos();

                // Spawn a lightning bolt at the target position
                LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                lightning.setPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                world.spawnEntity(lightning);
            }
        }
    }
}
