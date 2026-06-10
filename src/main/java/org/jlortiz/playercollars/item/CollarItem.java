package org.jlortiz.playercollars.item;

import eu.pb4.trinkets.api.DefaultTrinketSlots;
import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import org.jlortiz.playercollars.ClientHooks;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.function.Consumer;

public class CollarItem extends Item {
    public static final ResourceKey<Item> REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "collar"));
    public static final ResourceKey<Item> TAGLESS_REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "tagless_collar"));
    public final boolean tagless;

    public CollarItem(boolean tagless) {
        super(new Item.Properties().stacksTo(1).setId(tagless ? TAGLESS_REGISTRY_KEY : REGISTRY_KEY)
                .component(DataComponents.ENCHANTABLE, new Enchantable(100))
                .component(DataComponents.DYED_COLOR, new DyedItemColor(MapColor.COLOR_RED.col))
                .component(DataComponents.MAP_COLOR, new MapItemColor(MapColor.COLOR_BLUE.col))
                .component(TrinketDataComponents.EQUIPMENT, TrinketEquippable.DEFAULT
                        .withSlots(DefaultTrinketSlots.CHEST_NECKLACE)
                        .withEquipOnInteract(true)));
        this.tagless = tagless;
    }

    public static int getColor(ItemStack itemStack) {
        DyedItemColor $$1 = itemStack.get(DataComponents.DYED_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.COLOR_RED.col | 0xFF000000;
    }

    public static int getPawColor(ItemStack itemStack) {
        MapItemColor $$1 = itemStack.get(DataComponents.MAP_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.COLOR_BLUE.col;
    }

    @Override
    public InteractionResult use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        ItemStack is = p_41433_.getItemInHand(p_41434_);
        if (p_41433_.isShiftKeyDown() && p_41432_.isClientSide()) {
            ClientHooks.openCollarDyeScreen(is, p_41433_.getUUID());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        OwnerComponent owner = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        if (owner != null && owner.ownedName().isPresent())
            return Component.translatable("item.playercollars.collar.named", owner.ownedName().get());
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (type.isAdvanced() && !tagless) {
            tooltip.accept(Component.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(stack))).setStyle(Style.EMPTY.withColor(CommonColors.GRAY)));
        }
        OwnerComponent owner = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        if (owner != null) {
            tooltip.accept(Component.translatable("item.playercollars.collar.owner", owner.name()).withStyle(ChatFormatting.GRAY));
        }
    }

}
