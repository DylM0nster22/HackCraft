package net.dylan.magicmod;

import net.dylan.magicmod.block.ModBlocks;
import net.dylan.magicmod.item.ModItems;
import net.dylan.magicmod.item.ModItemsGroups;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magicmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RegistryKey<DamageType> LUNAR_STAFF_DAMAGE_TYPE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("magicmod", "lunar_staff"));

	public static final RegistryKey<PlacedFeature> MAGIC_CRYSTAL_ORE_PLACED_KEY =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("magicmod", "magic_crystal_ore"));

	public static final RegistryKey<PlacedFeature> MAGIC_CRYSTAL_DEEPSLATE_ORE_PLACED_KEY =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("magicmod", "magic_crystal_deepslate_ore"));


	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModItemsGroups.registerItemGroups();
		ModBlocks.registerModBlocks();


		// Initialize other features, like the LightningRodItemSpawner

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				MAGIC_CRYSTAL_ORE_PLACED_KEY
		);

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				MAGIC_CRYSTAL_DEEPSLATE_ORE_PLACED_KEY
		);
	}
}
