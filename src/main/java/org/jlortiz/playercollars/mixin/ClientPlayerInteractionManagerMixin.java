package org.jlortiz.playercollars.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static boolean shouldPawsBlock(LivingEntity player, BlockState block) {
        return PawsItem.shouldPreventBlockInteraction(player, block);
    }

    @Inject(method="useItemOn", at=@At("HEAD"), cancellable = true)
    private void playercollars$cancelPawInteractions(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isSpectator()) return;
        BlockState block = this.minecraft.level.getBlockState(hitResult.getBlockPos());
        if (shouldPawsBlock(player, block)) cir.setReturnValue(InteractionResult.PASS);
    }
}
