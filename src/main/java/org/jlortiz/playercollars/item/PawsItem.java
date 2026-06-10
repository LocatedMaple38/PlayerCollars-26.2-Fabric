package org.jlortiz.playercollars.item;

import eu.pb4.trinkets.api.DefaultTrinketSlots;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Consumer;

public class PawsItem extends FootPawsItem {
    public PawsItem(ResourceKey<Item> key, int color, int pawColor) {
        super(key, color, pawColor, DefaultTrinketSlots.HAND_GLOVE, DefaultTrinketSlots.OFFHAND_GLOVE);
    }

    public static boolean hasPaws(LivingEntity entity) {
        return EquippedTrinkets.hasEquipped(entity, (x) -> x.is(PlayerCollarsMod.PAWS_TAG));
    }

    public static boolean shouldPreventBlockInteraction(ItemStack stack, @NotNull BlockState block) {
        return !isHardAllowedBlockInteraction(block);
    }

    public static boolean shouldPreventBlockInteraction(LivingEntity entity, @NotNull BlockState block) {
        return hasPaws(entity) && !isHardAllowedBlockInteraction(block);
    }

    public static boolean isHardAllowedBlockInteraction(@NotNull BlockState block) {
        Block b = block.getBlock();
        return b instanceof DoorBlock
                || b instanceof TrapDoorBlock
                || b instanceof ButtonBlock
                || b instanceof LeverBlock
                || b instanceof DogBedBlock
                || b instanceof DogBowlBlock;
    }

    public static boolean shouldDrop(ItemStack pawsStack, ItemStack thing) {
        return !thing.isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.playercollars.paws.slippery"));
        tooltip.accept(Component.translatable("item.playercollars.paws.interaction"));
    }

    public static ResourceKey<Item> getRegistryKey(DyeColor c) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, c.getName() + "_paws"));
    }
}
