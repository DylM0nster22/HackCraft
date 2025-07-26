package net.dylan.magicmod.item.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import java.util.List;

public class BerserkerStaff extends Item {
    public BerserkerStaff(Settings settings) { super(settings); }
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal("Placeholder staff implementation").formatted(Formatting.YELLOW));
        } else {
            tooltip.add(Text.literal("Press Shift for more information").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return TypedActionResult.success(player.getStackInHand(hand));
    }
}
