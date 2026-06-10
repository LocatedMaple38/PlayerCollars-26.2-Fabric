package org.jlortiz.playercollars.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Items.class)
public abstract class BoneItemMixin {
    @Shadow public static Item registerItem(String id, Item.Properties settings) { return null; }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Items;registerItem(Ljava/lang/String;)Lnet/minecraft/world/item/Item;", ordinal = 29))
    private static Item injectEquippableBone(String id) {
        return registerItem(id, new Item.Properties().equippable(EquipmentSlot.HEAD));
    }
}