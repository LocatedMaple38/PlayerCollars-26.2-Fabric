package org.jlortiz.playercollars.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    private static final ThreadLocal<Boolean> playercollars$applyingCollarThorns = ThreadLocal.withInitial(() -> false);

    public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(at=@At("TAIL"), method="hurtServer")
    private void checkCollarThorns(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (playercollars$applyingCollarThorns.get()) return;

        ServerPlayer victim = (ServerPlayer) (Object) this;
        Entity causingEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (!(causingEntity instanceof LivingEntity attacker)) return;
        if (playercollars$isSameEntity(causingEntity, victim) || playercollars$isSameEntity(directEntity, victim)) return;

        // Reflected/self-fired projectiles can report the shooter as the causing entity.
        // Applying collar thorns back to the same player re-enters hurtServer until the server overflows the stack.
        if (playercollars$isProjectileOwnedBy(directEntity, victim)) return;

        playercollars$applyingCollarThorns.set(true);
        try {
            for (ItemStack stack : EquippedTrinkets.getEquipped(this, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(world, attacker, source, stack);
            }
        } finally {
            playercollars$applyingCollarThorns.set(false);
        }
    }

    private static boolean playercollars$isSameEntity(Entity entity, Entity target) {
        return entity != null && entity.getUUID().equals(target.getUUID());
    }

    private static boolean playercollars$isProjectileOwnedBy(Entity entity, Entity owner) {
        return entity instanceof Projectile projectile && playercollars$isSameEntity(projectile.getOwner(), owner);
    }
}
