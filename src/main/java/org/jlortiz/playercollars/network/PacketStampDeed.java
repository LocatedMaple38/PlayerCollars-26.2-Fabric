package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Optional;

public class PacketStampDeed implements CustomPacketPayload {
    public static final PacketStampDeed INSTANCE = new PacketStampDeed();
    public static final CustomPacketPayload.Type<PacketStampDeed> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "stamp_deed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketStampDeed> CODEC = StreamCodec.unit(INSTANCE);

    private PacketStampDeed() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ItemStack is = context.player().getMainHandItem();
            if (!is.isEmpty() && is.is(PlayerCollarsMod.DEED_OF_OWNERSHIP)) {
                OwnerComponent owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
                if (owner == null || owner.owned().isPresent()) return;
                String plrName = context.player().getName().getString();
                is = new ItemStack(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED);
                is.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, new OwnerComponent(
                   owner.uuid(), owner.name(), Optional.of(context.player().getUUID()), Optional.of(plrName)
                ));
                context.player().getInventory().setItem(context.player().getInventory().getSelectedSlot(), is);
            }
        });
    }
}
