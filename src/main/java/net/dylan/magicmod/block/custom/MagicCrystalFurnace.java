package net.dylan.magicmod.block.custom;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MagicCrystalFurnace extends AbstractFurnaceBlock {
    
    public MagicCrystalFurnace(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MagicCrystalFurnaceBlockEntity(pos, state);
    }

    @Override
    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MagicCrystalFurnaceBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY() + 0.0;
            double z = (double)pos.getZ() + 0.5;
            
            if (random.nextDouble() < 0.1) {
                world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = state.get(FACING);
            Direction.Axis axis = direction.getAxis();
            double defaultOffset = random.nextDouble() * 0.6 - 0.3;
            double xOffset = axis == Direction.Axis.X ? (double)direction.getOffsetX() * 0.52 : defaultOffset;
            double zOffset = axis == Direction.Axis.Z ? (double)direction.getOffsetZ() * 0.52 : defaultOffset;
            
            // Enhanced magical particles
            world.addParticle(ParticleTypes.FLAME, x + xOffset, y + random.nextDouble() * 6.0 / 16.0, z + zOffset, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.SMOKE, x + xOffset, y + random.nextDouble() * 6.0 / 16.0, z + zOffset, 0.0, 0.0, 0.0);
            
            // Magical crystal particles
            world.addParticle(ParticleTypes.ENCHANT, x + xOffset, y + random.nextDouble() * 6.0 / 16.0, z + zOffset, 0.0, 0.1, 0.0);
            world.addParticle(ParticleTypes.END_ROD, x + xOffset, y + random.nextDouble() * 6.0 / 16.0, z + zOffset, 0.0, 0.05, 0.0);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.MAGIC_CRYSTAL_FURNACE_BLOCK_ENTITY, MagicCrystalFurnaceBlockEntity::tick);
    }
}