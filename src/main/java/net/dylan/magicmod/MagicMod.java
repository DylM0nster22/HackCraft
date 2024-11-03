package net.dylan.magicmod;

import net.dylan.magicmod.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magicmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
	}
}