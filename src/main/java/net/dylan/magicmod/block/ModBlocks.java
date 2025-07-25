package net.dylan.magicmod.block;

import net.dylan.magicmod.MagicMod;
import net.dylan.magicmod.block.custom.FireCrystalTnt;
import net.dylan.magicmod.block.custom.IceCrystalTnt;
import net.dylan.magicmod.block.custom.LightningCrystalTnt;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.PickaxeItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ModBlocks {
    public static final Block MAGIC_CRYSTAL_BLOCK = registerBlock("magic_crystal_block",
        new Block(AbstractBlock.Settings.create().strength(6f)
                .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block LIGHTNING_CRYSTAL_BLOCK = registerBlock("lightning_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block QUANTUM_CRYSTAL_BLOCK = registerBlock("quantum_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block ICE_CRYSTAL_BLOCK = registerBlock("ice_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block WIND_CRYSTAL_BLOCK = registerBlock("wind_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block LAVA_CRYSTAL_BLOCK = registerBlock("lava_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block EARTH_CRYSTAL_BLOCK = registerBlock("earth_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block FIRE_CRYSTAL_BLOCK = registerBlock("fire_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block ECLIPSE_CRYSTAL_BLOCK = registerBlock("eclipse_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block LUNAR_CRYSTAL_BLOCK = registerBlock("lunar_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block SOLAR_CRYSTAL_BLOCK = registerBlock("solar_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block OBSIDIAN_CRYSTAL_BLOCK = registerBlock("obsidian_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block WATER_CRYSTAL_BLOCK = registerBlock("water_crystal_block",
            new Block(AbstractBlock.Settings.create().strength(6f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));




    public static final Block MAGIC_CRYSTAL_ORE = registerBlock("magic_crystal_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
                    AbstractBlock.Settings.create().strength(3f).requiresTool().sounds(BlockSoundGroup.STONE)));

    public static final Block MAGIC_CRYSTAL_DEEPSLATE_ORE = registerBlock("magic_crystal_deepslate_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(3, 6),
                    AbstractBlock.Settings.create().strength(4f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    // Crystal TNT Blocks
    public static final Block FIRE_CRYSTAL_TNT = registerBlock("fire_crystal_tnt",
            new FireCrystalTnt(AbstractBlock.Settings.create()
                    .strength(0.0F)
                    .sounds(BlockSoundGroup.GRASS)));

    public static final Block ICE_CRYSTAL_TNT = registerBlock("ice_crystal_tnt",
            new IceCrystalTnt(AbstractBlock.Settings.create()
                    .strength(0.0F)
                    .sounds(BlockSoundGroup.POWDER_SNOW)));

    public static final Block LIGHTNING_CRYSTAL_TNT = registerBlock("lightning_crystal_tnt",
            new LightningCrystalTnt(AbstractBlock.Settings.create()
                    .strength(0.0F)
                    .sounds(BlockSoundGroup.METAL)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(MagicMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, name),
            new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        MagicMod.LOGGER.info("Registering Blocks for " + MagicMod.MOD_ID);


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.MAGIC_CRYSTAL_BLOCK);
            entries.add(ModBlocks.ICE_CRYSTAL_BLOCK);
            entries.add(ModBlocks.OBSIDIAN_CRYSTAL_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.add(ModBlocks.FIRE_CRYSTAL_TNT);
            entries.add(ModBlocks.ICE_CRYSTAL_TNT);
            entries.add(ModBlocks.LIGHTNING_CRYSTAL_TNT);
        });
    }
}
