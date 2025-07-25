package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.damage.DamageSource;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MasterStaff extends Item {
    private Element currentElement = Element.FIRE;
    private long lastToggleTime = 0;
    private static final long TOGGLE_COOLDOWN = 500; // cooldown in milliseconds
    private static final int ICE_DURATION_TICKS = 100; // 5 seconds * 20 ticks per second
    private final Map<BlockPos, Integer> icePositions = new HashMap<>();

    public MasterStaff(Settings settings) {
        super(settings);
        // Register the server tick event listener
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    // Enum for different elements
    private enum Element {
        FIRE,
        LIGHTNING,
        ICE,
        EARTH;

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
                case EARTH:
                    useEarthElement(world, player);
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
                player.sendMessage(Text.literal("Changed element to Lightning").formatted(Formatting.DARK_AQUA), true); // Teal color
            }
            case LIGHTNING -> {
                currentElement = Element.ICE;
                player.sendMessage(Text.literal("Changed element to Ice").formatted(Formatting.BLUE), true); // Blue color
            }
            case ICE -> {
                currentElement = Element.EARTH;
                player.sendMessage(Text.literal("Changed element to Earth").formatted(Formatting.DARK_GREEN), true); // Red color
            }
            case EARTH -> {
                currentElement = Element.FIRE;
                player.sendMessage(Text.literal("Changed element to Fire").formatted(Formatting.RED), true); // Teal color
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

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            Text elementText;
            switch (currentElement) {
                case FIRE:
                    elementText = Text.literal("Element: Fire").formatted(Formatting.RED);
                    break;
                case LIGHTNING:
                    elementText = Text.literal("Element: Lightning").formatted(Formatting.DARK_AQUA);
                    break;
                case ICE:
                    elementText = Text.literal("Element: Ice").formatted(Formatting.BLUE);
                    break;
                case EARTH:
                    elementText = Text.literal("Element: Earth").formatted(Formatting.DARK_GREEN);
                    break;
                default:
                    elementText = Text.literal("Element: Unknown").formatted(Formatting.GRAY);
                    break;

            }
            tooltip.add(elementText);
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    private void useEarthElement(World world, PlayerEntity player) {
        // Play ground slam sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            // Define the radius of the effect
            double radius = 5.0D;

            // Get all entities within the radius
            List<Entity> entities = world.getOtherEntities(player, player.getBoundingBox().expand(radius),
                    entity -> entity instanceof LivingEntity && entity != player);

            // Retrieve the 'earth_slam' DamageType from the registry
            RegistryKey<DamageType> earthSlamKey = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("magicmod", "earth_slam"));
            RegistryEntry<DamageType> earthSlamType = world.getRegistryManager()
                    .get(RegistryKeys.DAMAGE_TYPE)
                    .getEntry(earthSlamKey)
                    .orElseThrow(() ->
                            new IllegalStateException("DamageType 'earth_slam' not found in registry"));

            // Apply effects to each entity
            for (Entity entity : entities) {
                // Calculate knockback direction
                Vec3d direction = entity.getPos().subtract(player.getPos()).normalize().multiply(1.5);
                entity.setVelocity(direction.x, 0.5, direction.z);
                entity.velocityModified = true; // Ensure velocity is updated on the client

                // Apply damage
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.damage(new DamageSource(earthSlamType, player), 6.0F);
                }
            }

            // Create particle effects
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(),
                        20, 1.0, 0.5, 1.0, 0.1);
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
                                // Schedule this ice block for removal
                                icePositions.put(mutablePos.toImmutable(), ICE_DURATION_TICKS);
                            }
                        }
                    }
                }

            }
        }
    }

    private void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<BlockPos, Integer>> iterator = icePositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                // Remove ice blocks from all worlds
                for (World world : server.getWorlds()) {
                    BlockPos pos = entry.getKey();
                    if (world.getBlockState(pos).isOf(Blocks.ICE)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
                iterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
    }
}




