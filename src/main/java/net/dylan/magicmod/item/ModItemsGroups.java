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
                        entries.add(ModItems.MAGIC_CRYSTAL);
                        entries.add(ModItems.MAGIC_SWORD);
                        entries.add(ModBlocks.MAGIC_CRYSTAL_BLOCK);
                        entries.add(ModBlocks.MAGIC_TNT);
                        entries.add(ModBlocks.MAGIC_CRYSTAL_ORE);
                        entries.add(ModBlocks.MAGIC_CRYSTAL_DEEPSLATE_ORE);
                        entries.add(ModItems.POWERSTAFF);

                    }).build());


    public static void registerItemGroups() {
        MagicMod.LOGGER.info("Registering Item Groups for " + MagicMod.MOD_ID);
    }
}
