package net.dylan.magicmod.item;

import net.dylan.magicmod.MagicMod;
import net.dylan.magicmod.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemsGroups {
    public static final ItemGroup MAGIC_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(MagicMod.MOD_ID, "magic"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.MAGIC_CRYSTAL))
                    .displayName(Text.translatable("itemgroup.magicmod.magic"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.FIRESTAFF);
                        entries.add(ModItems.MASTERSTAFF);
                        entries.add(ModItems.LIGHTNINGSTAFF);
                        entries.add(ModItems.EARTHSTAFF);
                        entries.add(ModItems.ICESTAFF);
                        entries.add(ModItems.QUANTUMSTAFF);
                        entries.add(ModItems.LAVASTAFF);
                        entries.add(ModItems.WINDSTAFF);
                        entries.add(ModItems.ECLIPSESTAFF);
                        entries.add(ModItems.LUNARSTAFF);
                        entries.add(ModItems.SOLARSTAFF);
                        entries.add(ModItems.OBSIDIANSTAFF);
                        entries.add(ModItems.WATERSTAFF);

                        entries.add(ModItems.MAGIC_CRYSTAL);
                        entries.add(ModItems.FIRE_CRYSTAL);
                        entries.add(ModItems.ICE_CRYSTAL);
                        entries.add(ModItems.EARTH_CRYSTAL);
                        entries.add(ModItems.LIGHTNING_CRYSTAL);
                        entries.add(ModItems.LAVA_CRYSTAL);
                        entries.add(ModItems.WIND_CRYSTAL);
                        entries.add(ModItems.QUANTUM_CRYSTAL);
                        entries.add(ModItems.ECLIPSE_CRYSTAL);
                        entries.add(ModItems.LUNAR_CRYSTAL);
                        entries.add(ModItems.SOLAR_CRYSTAL);
                        entries.add(ModItems.OBSIDIAN_CRYSTAL);
                        entries.add(ModItems.WATER_CRYSTAL);

                        entries.add(ModBlocks.MAGIC_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.FIRE_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.ICE_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.EARTH_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.LIGHTNING_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.LAVA_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.WIND_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.QUANTUM_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.ECLIPSE_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.LUNAR_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.SOLAR_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.OBSIDIAN_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.WATER_CRYSTAL_BLOCK);

                        entries.add(ModBlocks.MAGIC_CRYSTAL_ORE);
                        entries.add(ModBlocks.MAGIC_CRYSTAL_DEEPSLATE_ORE);




                    }).build());


    public static void registerItemGroups() {
        MagicMod.LOGGER.info("Registering Item Groups for " + MagicMod.MOD_ID);
    }
}
