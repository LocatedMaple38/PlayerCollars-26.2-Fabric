package org.jlortiz.playercollars.item;

import eu.pb4.trinkets.api.DefaultTrinketSlots;
import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.MapItemColor;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class FootPawsItem extends Item {
    public final int color, beansColor;

    public FootPawsItem(ResourceKey<Item> key, int color, int beansColor) {
        this(key, color, beansColor, DefaultTrinketSlots.FEET_SHOES);
    }

    protected FootPawsItem(ResourceKey<Item> key, int color, int beansColor, String... slots) {
        super(new Item.Properties().stacksTo(1).setId(key)
                .component(DataComponents.DYED_COLOR, new DyedItemColor(color | 0xFF000000))
                .component(DataComponents.MAP_COLOR, new MapItemColor(beansColor))
                .component(TrinketDataComponents.EQUIPMENT, TrinketEquippable.DEFAULT
                        .withSlots(slots)
                        .withEquipOnInteract(true))
        );
        this.color = color | 0xFF000000;
        this.beansColor = beansColor;
    }

    public static ResourceKey<Item> getRegistryKey(DyeColor c) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, c.getName() + "_foot_paws"));
    }

}
