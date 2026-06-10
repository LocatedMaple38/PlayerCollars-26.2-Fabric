package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 500)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    // Ideally this should be in MixinServerPlayerEntity, but I'm *very* wary about overriding methods in the player
    @Inject(method = "interactOn", at = @At("RETURN"), cancellable = true)
    private void leashplayers$onInteract(Entity entity, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> info) {
        if (info.getReturnValue() != InteractionResult.PASS) return;
        if (((Object) this) instanceof ServerPlayer player && entity instanceof LeashImpl impl) {
            info.setReturnValue(impl.leashplayers$interact(player, hand));
            info.cancel();
        }
    }
}
