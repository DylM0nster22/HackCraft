package net.dylan.magicmod;

import net.dylan.magicmod.block.ModBlocks;
import net.dylan.magicmod.item.ModItems;
import net.dylan.magicmod.item.ModItemsGroups;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magicmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RegistryKey<PlacedFeature> MAGIC_CRYSTAL_ORE_PLACED_KEY =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("magicmod", "magic_crystal_ore"));

	public static final RegistryKey<PlacedFeature> MAGIC_CRYSTAL_DEEPSLATE_ORE_PLACED_KEY =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("magicmod", "magic_crystal_deepslate_ore"));


	@Override
	public void onInitialize() {
		ModItemsGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

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


