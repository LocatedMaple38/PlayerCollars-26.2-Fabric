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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.PetControlHelper;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.SpeechMode;

import java.util.UUID;

public record PacketTogglePetControl(UUID petId, Control control) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTogglePetControl> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "toggle_pet_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTogglePetControl> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PacketTogglePetControl::petId,
            ByteBufCodecs.idMapper(Control::byId, Control::networkId), PacketTogglePetControl::control,
            PacketTogglePetControl::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer owner = context.player();
            ServerPlayer pet = PetControlHelper.findOnlinePlayer(owner, petId);
            PetControlHelper.ValidationResult result = PetControlHelper.validateOwnerControl(owner, pet);
            if (!result.successful()) {
                if (result.failure() != null) owner.sendOverlayMessage(result.failure().message());
                return;
            }

            if (control == Control.UNKNOWN) {
                owner.sendOverlayMessage(Component.translatable("message.playercollars.pet_control.error.invalid_request")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            ItemStack collar = result.activeCollar().collar();
            switch (control) {
                case SPEECH -> {
                    SpeechMode next = collar.getOrDefault(PlayerCollarsMod.SPEECH_MODE_COMPONENT_TYPE, SpeechMode.ALLOWED).next();
                    if (next == SpeechMode.ALLOWED) {
                        collar.remove(PlayerCollarsMod.SPEECH_MODE_COMPONENT_TYPE);
                    } else {
                        collar.set(PlayerCollarsMod.SPEECH_MODE_COMPONENT_TYPE, next);
                    }
                }
                case COMMANDS -> toggleBoolean(collar, PlayerCollarsMod.COMMANDS_BLOCKED_COMPONENT_TYPE);
                case VISION -> toggleBoolean(collar, PlayerCollarsMod.VISION_OBSCURED_COMPONENT_TYPE);
                case MOVEMENT -> toggleBoolean(collar, PlayerCollarsMod.MOVEMENT_RESTRAINED_COMPONENT_TYPE);
                case UNKNOWN -> {
                }
            }

            ServerPlayNetworking.send(owner, PacketOpenPetControl.from(result.activeCollar().pet(), collar));
        });
    }

    private static void toggleBoolean(ItemStack collar, net.minecraft.core.component.DataComponentType<Boolean> componentType) {
        boolean next = !collar.getOrDefault(componentType, false);
        if (next) {
            collar.set(componentType, true);
        } else {
            collar.remove(componentType);
        }
    }

    public enum Control {
        SPEECH,
        COMMANDS,
        VISION,
        MOVEMENT,
        UNKNOWN;

        public static Control byId(int id) {
            Control[] values = values();
            return id >= 0 && id < MOVEMENT.ordinal() + 1 ? values[id] : UNKNOWN;
        }

        public int networkId() {
            return this == UNKNOWN ? -1 : ordinal();
        }
    }
}
