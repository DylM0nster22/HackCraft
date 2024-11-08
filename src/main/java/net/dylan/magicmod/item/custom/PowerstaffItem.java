package net.dylan.magicmod.item.custom;

import net.dylan.magicmod.MagicMod;
import net.dylan.magicmod.entity.BoulderEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        LIGHTNING,
        ICE;

        @Override
        public String toString() {
            return super.toString();
        }
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
                case ICE:
                    useIceElement(world, player);
                    break;
            }
        }

        // Return the action result to indicate success
        return TypedActionResult.success(itemStack, world.isClient());
    }

    private void toggleElement(PlayerEntity player) {
        switch (currentElement) {
            case FIRE -> {
                currentElement = Element.LIGHTNING;
                player.sendMessage(Text.literal("Changed element to Lightning"), true);
            }
            case LIGHTNING -> {
                currentElement = Element.ICE;
                player.sendMessage(Text.literal("Changed element to Ice"), true);
            }
            case ICE -> {
                currentElement = Element.FIRE;
                player.sendMessage(Text.literal("Changed element to Fire"), true);
            }
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

            HitResult hitResult = world.raycast(new net.minecraft.world.RaycastContext(
                    startPos, endPos,
                    net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    player
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos targetPos = blockHitResult.getBlockPos();

                // Spawn a lightning bolt at the target position
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(targetPos));
                    world.spawnEntity(lightning);
                }
            }
        }
    }

    private void useIceElement(World world, PlayerEntity player) {
        // Play ice sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            // Perform a ray trace to find the targeted entity with extended range (50 blocks)
            Vec3d startPos = player.getCameraPosVec(1.0F);
            Vec3d direction = player.getRotationVec(1.0F);
            Vec3d endPos = startPos.add(direction.multiply(50)); // Extended range to 50 blocks

            EntityHitResult entityHitResult = ProjectileUtil.raycast(player, startPos, endPos,
                    player.getBoundingBox().stretch(direction.multiply(50)).expand(1.0D),
                    entity -> !entity.isSpectator() && entity.isAlive(),
                    50);

            if (entityHitResult != null) {
                Entity target = entityHitResult.getEntity();
                BlockPos targetPos = target.getBlockPos();

                // Determine the size of the encasement based on the entity's bounding box
                Box boundingBox = target.getBoundingBox();
                double targetWidth = boundingBox.maxX - boundingBox.minX;
                double targetHeight = boundingBox.maxY - boundingBox.minY;
                int encasementRadius = (int) Math.ceil(targetWidth / 2);
                int encasementHeight = (int) Math.ceil(targetHeight);

                // Define the area around the entity to encase in ice, scaling with the entity's size
                BlockPos.Mutable mutablePos = new BlockPos.Mutable();
                for (int x = -encasementRadius; x <= encasementRadius; x++) {
                    for (int y = 0; y <= encasementHeight; y++) {
                        for (int z = -encasementRadius; z <= encasementRadius; z++) {
                            mutablePos.set(targetPos.getX() + x, targetPos.getY() + y, targetPos.getZ() + z);
                            if (world.getBlockState(mutablePos).isAir()) {
                                world.setBlockState(mutablePos, Blocks.ICE.getDefaultState());
                            }
                        }
                    }
                }

                // Schedule the ice to melt after a delay (e.g., 5 seconds)
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> {
                    for (int x = -encasementRadius; x <= encasementRadius; x++) {
                        for (int y = 0; y <= encasementHeight; y++) {
                            for (int z = -encasementRadius; z <= encasementRadius; z++) {
                                mutablePos.set(targetPos.getX() + x, targetPos.getY() + y, targetPos.getZ() + z);
                                if (world.getBlockState(mutablePos).isOf(Blocks.ICE)) {
                                    world.setBlockState(mutablePos, Blocks.AIR.getDefaultState());
                                }
                            }
                        }
                    }
                }, 5, TimeUnit.SECONDS);

                // Shutdown the scheduler after task completion
                scheduler.shutdown();
            }
        }
    }
}




