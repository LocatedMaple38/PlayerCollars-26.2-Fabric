package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.MapItemColor;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

public record PacketUpdateCollar(OwnerState os, int pawColor, int color) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketUpdateCollar> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "update_collar"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateCollar> CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(OwnerState::fromInt, OwnerState::ordinal), PacketUpdateCollar::os,
            ByteBufCodecs.INT, PacketUpdateCollar::pawColor,
            ByteBufCodecs.INT, PacketUpdateCollar::color,
            PacketUpdateCollar::new);
    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        this(os, CollarItem.getPawColor(is), CollarItem.getColor(is));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public enum OwnerState {
        NOP, DEL, ADD;

        public static OwnerState fromInt(int ind) {
            return OwnerState.values()[ind];
        }
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ItemStack is = context.player().getMainHandItem();
            if (!is.isEmpty() && is.getItem() instanceof CollarItem) {
                is.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
                is.set(DataComponents.MAP_COLOR, new MapItemColor(pawColor));
                if (os == OwnerState.DEL) {
                    is.remove(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
                } else if (os == OwnerState.ADD) {
                    is.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, new OwnerComponent(context.player().getUUID(), context.player().getName().getString()));
                }
            }
        });
    }
}
