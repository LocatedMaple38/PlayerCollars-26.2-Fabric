package io.wispforest.accessories.api.client;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class AccessoriesRendererRegistry {
    private AccessoriesRendererRegistry() {
    }

    public static void registerRenderer(Item item, Supplier<? extends AccessoryRenderer> renderer) {
        // No-op placeholder until Accessories has a 26.1.2 Fabric release.
    }
}
