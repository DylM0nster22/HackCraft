package net.dylan.magicmod.item;

import net.dylan.magicmod.MagicMod;
import net.dylan.magicmod.item.custom.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item MAGIC_CRYSTAL = registerItem("magic_crystal", new Item(new Item.Settings()));
    public static Item FIRE_CRYSTAL = registerItem("fire_crystal", new Item(new Item.Settings()));
    public static Item ICE_CRYSTAL = registerItem("ice_crystal", new Item(new Item.Settings()));
    public static Item EARTH_CRYSTAL = registerItem("earth_crystal", new Item(new Item.Settings()));
    public static Item LIGHTNING_CRYSTAL = registerItem("lightning_crystal", new Item(new Item.Settings()));
    public static Item LAVA_CRYSTAL = registerItem("lava_crystal", new Item(new Item.Settings()));
    public static Item QUANTUM_CRYSTAL = registerItem("quantum_crystal", new Item(new Item.Settings()));
    public static Item WIND_CRYSTAL = registerItem("wind_crystal", new Item(new Item.Settings()));
    public static Item ECLIPSE_CRYSTAL = registerItem("eclipse_crystal", new Item(new Item.Settings()));
    public static Item LUNAR_CRYSTAL = registerItem("lunar_crystal", new Item(new Item.Settings()));
    public static Item SOLAR_CRYSTAL = registerItem("solar_crystal", new Item(new Item.Settings()));
    public static Item OBSIDIAN_CRYSTAL = registerItem("obsidian_crystal", new Item(new Item.Settings()));
    public static Item WATER_CRYSTAL = registerItem("water_crystal", new Item(new Item.Settings()));

    public static final Item MASTERSTAFF = new MasterStaff(new Item.Settings().maxCount(1));
    public static final Item FIRESTAFF = new FireStaff(new Item.Settings().maxCount(1));
    public static final Item LIGHTNINGSTAFF = new LightningStaff(new Item.Settings().maxCount(1));
    public static final Item EARTHSTAFF = new EarthStaff(new Item.Settings().maxCount(1));
    public static final Item ICESTAFF = new IceStaff(new Item.Settings().maxCount(1));
    public static final Item QUANTUMSTAFF = new QuantumStaff(new Item.Settings().maxCount(1));
    public static final Item LAVASTAFF = new LavaStaff(new Item.Settings().maxCount(1));
    public static final Item WINDSTAFF = new WindStaff(new Item.Settings().maxCount(1));
    public static final Item ECLIPSESTAFF = new EclipseStaff(new Item.Settings().maxCount(1));
    public static final Item LUNARSTAFF = new LunarStaff(new Item.Settings().maxCount(1));
    public static final Item SOLARSTAFF = new SolarStaff(new Item.Settings().maxCount(1));
    public static final Item OBSIDIANSTAFF = new ObsidianStaff(new Item.Settings().maxCount(1));
    public static final Item WATERSTAFF = new WaterStaff(new Item.Settings().maxCount(1));


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MagicMod.LOGGER.info("Registering Items for " + MagicMod.MOD_ID);



        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(MAGIC_CRYSTAL);
        });

        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "masterstaff"), MASTERSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "firestaff"), FIRESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "lightningstaff"), LIGHTNINGSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "earthstaff"), EARTHSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "icestaff"), ICESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "quantumstaff"), QUANTUMSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "lavastaff"), LAVASTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "windstaff"), WINDSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "eclipsestaff"), ECLIPSESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "lunarstaff"), LUNARSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "solarstaff"), SOLARSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "obsidianstaff"), OBSIDIANSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "waterstaff"), WATERSTAFF);
    }
}
