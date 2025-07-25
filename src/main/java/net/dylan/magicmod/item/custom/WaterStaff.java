package net.dylan.magicmod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaterStaff extends Item {
    private static final int WAVE_RANGE = 10;
    private static final int COOLDOWN_TICKS = 200; // 10 seconds
    private static final int WATER_LIFETIME_SECONDS = 5;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public WaterStaff(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            Vec3d lookVec = user.getRotationVec(1.0F);
            Direction direction = Direction.getFacing(lookVec.x, lookVec.y, lookVec.z);
            BlockPos startPos = user.getBlockPos().offset(direction);

            for (int i = 0; i < WAVE_RANGE; i++) {
                BlockPos pos = startPos.offset(direction, i);
                if (world.isAir(pos) || world.getBlockState(pos).isReplaceable()) {
                    world.setBlockState(pos, Blocks.WATER.getDefaultState());
                    scheduleWaterRemoval(world, pos);
                } else if (world.getBlockState(pos).isOf(Blocks.FIRE)) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                } else if (world.getBlockState(pos).isOf(Blocks.LAVA)) {
                    if (world.getBlockState(pos).getFluidState().isStill()) {
                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                    } else {
                        world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                    }
                }
            }

            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
            world.emitGameEvent(user, GameEvent.ITEM_INTERACT_START, user.getBlockPos());
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void scheduleWaterRemoval(World world, BlockPos pos) {
        scheduler.schedule(() -> {
            if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }, WATER_LIFETIME_SECONDS, TimeUnit.SECONDS);
    }
}
