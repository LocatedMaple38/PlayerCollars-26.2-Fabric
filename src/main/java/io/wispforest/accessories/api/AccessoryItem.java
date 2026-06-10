package io.wispforest.accessories.api;

import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AccessoryItem extends Item {
    public AccessoryItem(Properties properties) {
        super(properties);
    }

    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
    }

    public DropRule getDropRule(ItemStack stack, SlotReference reference, DamageSource source) {
        return DropRule.DEFAULT;
    }
}
