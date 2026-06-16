package org.jlortiz.playercollars.mixin;

import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Input;
import org.jlortiz.playercollars.PetControlHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Shadow public abstract boolean hasClientLoaded();

    @Inject(
            method = "handleChat",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryHandleChat(Ljava/lang/String;ZLjava/lang/Runnable;)V"),
            cancellable = true
    )
    private void playercollars$blockRestrictedSpeech(ServerboundChatPacket packet, CallbackInfo ci) {
        if (PetControlHelper.handleBlockedSpeech(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleChatCommand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryHandleChat(Ljava/lang/String;ZLjava/lang/Runnable;)V"),
            cancellable = true
    )
    private void playercollars$blockUnsignedCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (PetControlHelper.handleBlockedCommand(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleSignedChatCommand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryHandleChat(Ljava/lang/String;ZLjava/lang/Runnable;)V"),
            cancellable = true
    )
    private void playercollars$blockSignedCommand(ServerboundChatCommandSignedPacket packet, CallbackInfo ci) {
        if (PetControlHelper.handleBlockedCommand(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerInput", at = @At("HEAD"), cancellable = true)
    private void playercollars$restrainPlayerInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (!PetControlHelper.isMovementRestrained(player)) return;

        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        player.setLastClientInput(Input.EMPTY);
        if (hasClientLoaded()) {
            player.resetLastActionTime();
            player.setShiftKeyDown(false);
        }
        ci.cancel();
    }
}
