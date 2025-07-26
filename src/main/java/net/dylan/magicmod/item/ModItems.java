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
    public static Item NECROMANCY_CRYSTAL = registerItem("necromancy_crystal", new Item(new Item.Settings()));
    public static Item TIME_CRYSTAL = registerItem("time_crystal", new Item(new Item.Settings()));
    public static Item VOID_CRYSTAL = registerItem("void_crystal", new Item(new Item.Settings()));
    
    // New crystals for additional staffs
    public static Item ENDER_CRYSTAL = registerItem("ender_crystal", new Item(new Item.Settings()));
    public static Item PORTAL_CRYSTAL = registerItem("portal_crystal", new Item(new Item.Settings()));
    public static Item DIMENSION_CRYSTAL = registerItem("dimension_crystal", new Item(new Item.Settings()));
    public static Item LIFE_CRYSTAL = registerItem("life_crystal", new Item(new Item.Settings()));
    public static Item REGENERATION_CRYSTAL = registerItem("regeneration_crystal", new Item(new Item.Settings()));
    public static Item RESTORATION_CRYSTAL = registerItem("restoration_crystal", new Item(new Item.Settings()));
    public static Item SPIRIT_CRYSTAL = registerItem("spirit_crystal", new Item(new Item.Settings()));
    public static Item BEAST_CRYSTAL = registerItem("beast_crystal", new Item(new Item.Settings()));
    public static Item GUARDIAN_CRYSTAL = registerItem("guardian_crystal", new Item(new Item.Settings()));
    public static Item BUILDER_CRYSTAL = registerItem("builder_crystal", new Item(new Item.Settings()));
    public static Item ARCHITECT_CRYSTAL = registerItem("architect_crystal", new Item(new Item.Settings()));
    public static Item FORTRESS_CRYSTAL = registerItem("fortress_crystal", new Item(new Item.Settings()));
    public static Item STORM_CRYSTAL = registerItem("storm_crystal", new Item(new Item.Settings()));
    public static Item BLIZZARD_CRYSTAL = registerItem("blizzard_crystal", new Item(new Item.Settings()));
    public static Item DROUGHT_CRYSTAL = registerItem("drought_crystal", new Item(new Item.Settings()));
    public static Item BERSERKER_CRYSTAL = registerItem("berserker_crystal", new Item(new Item.Settings()));
    public static Item SHIELD_CRYSTAL = registerItem("shield_crystal", new Item(new Item.Settings()));
    public static Item CURSE_CRYSTAL = registerItem("curse_crystal", new Item(new Item.Settings()));
    public static Item MINING_CRYSTAL = registerItem("mining_crystal", new Item(new Item.Settings()));
    public static Item HARVEST_CRYSTAL = registerItem("harvest_crystal", new Item(new Item.Settings()));
    public static Item REPAIR_CRYSTAL = registerItem("repair_crystal", new Item(new Item.Settings()));
    public static Item PLASMA_CRYSTAL = registerItem("plasma_crystal", new Item(new Item.Settings()));
    public static Item CRYSTAL_CRYSTAL = registerItem("crystal_crystal", new Item(new Item.Settings()));
    public static Item SHADOW_CRYSTAL = registerItem("shadow_crystal", new Item(new Item.Settings()));
    public static Item STEAM_CRYSTAL = registerItem("steam_crystal", new Item(new Item.Settings()));
    public static Item MAGMA_CRYSTAL = registerItem("magma_crystal", new Item(new Item.Settings()));
    public static Item FROST_CRYSTAL = registerItem("frost_crystal", new Item(new Item.Settings()));

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
    public static final Item NECROMANCYSTAFF = new NecromancyStaff(new Item.Settings().maxCount(1));
    public static final Item TIMESTAFF = new TimeStaff(new Item.Settings().maxCount(1));
    public static final Item VOIDSTAFF = new VoidStaff(new Item.Settings().maxCount(1));

    // New staffs
    public static final Item ENDERSTAFF = new EnderStaff(new Item.Settings().maxCount(1));
    public static final Item PORTALSTAFF = new PortalStaff(new Item.Settings().maxCount(1));
    public static final Item DIMENSIONSTAFF = new DimensionStaff(new Item.Settings().maxCount(1));
    public static final Item LIFESTAFF = new LifeStaff(new Item.Settings().maxCount(1));
    public static final Item REGENERATIONSTAFF = new RegenerationStaff(new Item.Settings().maxCount(1));
    public static final Item RESTORATIONSTAFF = new RestorationStaff(new Item.Settings().maxCount(1));
    public static final Item SPIRITSTAFF = new SpiritStaff(new Item.Settings().maxCount(1));
    public static final Item BEASTSTAFF = new BeastStaff(new Item.Settings().maxCount(1));
    public static final Item GUARDIANSTAFF = new GuardianStaff(new Item.Settings().maxCount(1));
    public static final Item BUILDERSTAFF = new BuilderStaff(new Item.Settings().maxCount(1));
    public static final Item ARCHITECTSTAFF = new ArchitectStaff(new Item.Settings().maxCount(1));
    public static final Item FORTRESSSTAFF = new FortressStaff(new Item.Settings().maxCount(1));
    public static final Item STORMSTAFF = new StormStaff(new Item.Settings().maxCount(1));
    public static final Item BLIZZARDSTAFF = new BlizzardStaff(new Item.Settings().maxCount(1));
    public static final Item DROUGHTSTAFF = new DroughtStaff(new Item.Settings().maxCount(1));
    public static final Item BERSERKERSTAFF = new BerserkerStaff(new Item.Settings().maxCount(1));
    public static final Item SHIELDSTAFF = new ShieldStaff(new Item.Settings().maxCount(1));
    public static final Item CURSESTAFF = new CurseStaff(new Item.Settings().maxCount(1));
    public static final Item MININGSTAFF = new MiningStaff(new Item.Settings().maxCount(1));
    public static final Item HARVESTSTAFF = new HarvestStaff(new Item.Settings().maxCount(1));
    public static final Item REPAIRSTAFF = new RepairStaff(new Item.Settings().maxCount(1));
    public static final Item PLASMASTAFF = new PlasmaStaff(new Item.Settings().maxCount(1));
    public static final Item CRYSTALSTAFF = new CrystalStaff(new Item.Settings().maxCount(1));
    public static final Item SHADOWSTAFF = new ShadowStaff(new Item.Settings().maxCount(1));
    public static final Item STEAMSTAFF = new SteamStaff(new Item.Settings().maxCount(1));
    public static final Item MAGMASTAFF = new MagmaStaff(new Item.Settings().maxCount(1));
    public static final Item FROSTSTAFF = new FrostStaff(new Item.Settings().maxCount(1));


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
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "necromancystaff"), NECROMANCYSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "timestaff"), TIMESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "voidstaff"), VOIDSTAFF);
        
        // Register new staffs
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "enderstaff"), ENDERSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "portalstaff"), PORTALSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "dimensionstaff"), DIMENSIONSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "lifestaff"), LIFESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "regenerationstaff"), REGENERATIONSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "restorationstaff"), RESTORATIONSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "spiritstaff"), SPIRITSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "beaststaff"), BEASTSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "guardianstaff"), GUARDIANSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "builderstaff"), BUILDERSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "architectstaff"), ARCHITECTSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "fortressstaff"), FORTRESSSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "stormstaff"), STORMSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "blizzardstaff"), BLIZZARDSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "droughtstaff"), DROUGHTSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "berserkerstaff"), BERSERKERSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "shieldstaff"), SHIELDSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "cursestaff"), CURSESTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "miningstaff"), MININGSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "harveststaff"), HARVESTSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "repairstaff"), REPAIRSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "plasmastaff"), PLASMASTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "crystalstaff"), CRYSTALSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "shadowstaff"), SHADOWSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "steamstaff"), STEAMSTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "magmastaff"), MAGMASTAFF);
        Registry.register(Registries.ITEM, Identifier.of(MagicMod.MOD_ID, "froststaff"), FROSTSTAFF);
    }
}
