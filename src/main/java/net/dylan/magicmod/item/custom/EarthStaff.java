package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class EarthStaff extends Item {
    public EarthStaff(Settings settings) {
        super(settings);
    }

    // Enum for different elements
    private enum Element {
        EARTH;

        @Override
        public String toString() {
            return super.toString();
        }
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        useEarthElement(world, user);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Element: Earth").formatted(Formatting.DARK_GREEN));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

}
