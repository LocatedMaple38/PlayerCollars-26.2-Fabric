package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.UUID;

public record PacketOpenPawsConfig(UUID pawHolder, boolean heldItems) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenPawsConfig> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "paws_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenPawsConfig> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PacketOpenPawsConfig::pawHolder,
            ByteBufCodecs.BOOL, PacketOpenPawsConfig::heldItems,
            PacketOpenPawsConfig::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() ->
                context.player().sendOverlayMessage(Component.literal("Paw Patroller config screens are disabled; paws use fixed rules.").withStyle(ChatFormatting.LIGHT_PURPLE)));
    }
}
