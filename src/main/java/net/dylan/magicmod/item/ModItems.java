package net.dylan.magicmod.item;

import net.dylan.magicmod.MagicMod;
import net.dylan.magicmod.item.custom.PowerstaffItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static  Item MAGIC_SWORD = registerItem("magic_sword", new Item(new Item.Settings()));
    public static  Item MAGIC_CRYSTAL = registerItem("magic_crystal", new Item(new Item.Settings()));

    public static final Item POWERSTAFF = new PowerstaffItem(new Item.Settings().maxCount(1));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MagicMod.LOGGER.info("Registering Items for " + MagicMod.MOD_ID);



        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(MAGIC_SWORD);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(MAGIC_CRYSTAL);
        });

        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "powerstaff"), POWERSTAFF);
    }
}
