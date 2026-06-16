package org.jlortiz.playercollars.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.PetControlOptions;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.SpeechMode;

import java.util.UUID;

public record PacketOpenPetControl(
        UUID petId,
        String petName,
        SpeechMode speechMode,
        boolean commandsBlocked,
        boolean visionObscured,
        boolean movementRestrained
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenPetControl> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "open_pet_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenPetControl> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PacketOpenPetControl::petId,
            ByteBufCodecs.STRING_UTF8, PacketOpenPetControl::petName,
            ByteBufCodecs.idMapper(SpeechMode::byId, SpeechMode::ordinal), PacketOpenPetControl::speechMode,
            ByteBufCodecs.BOOL, PacketOpenPetControl::commandsBlocked,
            ByteBufCodecs.BOOL, PacketOpenPetControl::visionObscured,
            ByteBufCodecs.BOOL, PacketOpenPetControl::movementRestrained,
            PacketOpenPetControl::new);

    public static PacketOpenPetControl from(ServerPlayer pet, ItemStack collar) {
        PetControlOptions options = PetControlOptions.fromCollar(collar);
        return new PacketOpenPetControl(
                pet.getUUID(),
                pet.getName().getString(),
                options.speechMode(),
                options.commandsBlocked(),
                options.visionObscured(),
                options.movementRestrained()
        );
    }

    public PetControlOptions options() {
        return new PetControlOptions(speechMode, commandsBlocked, visionObscured, movementRestrained);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
