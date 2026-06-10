package io.wispforest.accessories.api.slot;

import net.minecraft.world.item.ItemStack;

public record SlotEntryReference(ItemStack stack, Reference reference) {
    public SlotEntryReference(ItemStack stack) {
        this(stack, new Reference());
    }

    public static final class Reference {
        public void setStack(ItemStack stack) {
            // TODO(26.1.2): Restore slot mutation when Accessories publishes a compatible API.
        }
    }
}
