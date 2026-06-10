package org.jlortiz.playercollars.leash.mixin;

import com.mojang.authlib.GameProfile;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity extends Player implements LeashImpl {
    @Shadow public abstract boolean hasDisconnected();

    @Shadow public abstract ServerLevel level();

    @Shadow public ServerGamePacketListenerImpl connection;
    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;
    @Unique
    private int leashplayers$lastage;
    @Unique
    private double leashplayer$loyalty;
    @Unique
    private static final double FIREWORK_SEARCH_RADIUS = 128.0;
    @Unique
    private static final ResourceKey<Enchantment> SHORT_LEASH_KEY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "short_leash"));

    public MixinServerPlayerEntity(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Unique
    private void leashplayers$update() {
        if (
                leashplayers$holder != null && (
                        !leashplayers$holder.isAlive()
                                || !isAlive()
                                || hasDisconnected()
                )
        ) {
            leashplayers$detach();
            leashplayers$drop();
        }

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy = null;
            }
            else {
                Entity holderActual = leashplayers$holder;
                Entity holderTarget = leashplayers$proxy.getLeashHolder();

                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach();
                    leashplayers$drop();
                }
                else if (holderTarget != holderActual) {
                    leashplayers$attach(holderTarget);
                }
            }
        }

        leashplayers$apply();
    }

    @Unique
    private void leashplayers$apply() {
        Entity holder = leashplayers$holder;
        if (holder == null) return;
        if (holder.level() != level()) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        InteractionResult result;
        if (Math.abs(getY() - holder.getY()) > 6 + leashplayer$loyalty) {
            result = InteractionResult.FAIL;
        } else {
            // Don't pull on the Y axis - it'll make the unfortunate player fly all over the place
            Vec3 pos = new Vec3(holder.getX(), getY(), holder.getZ());
            result = PlayerCollarsMod.pullPlayerTowards((ServerPlayer) (Object) this, pos,
                    leashplayer$loyalty, leashplayer$loyalty + 6, (x) -> Math.min(0.15 * (x - leashplayer$loyalty), 0.375) / x);
        }

        if (result == InteractionResult.FAIL) {
            if (level().getGameRules().get(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE)) {
                leashplayers$detach();
                leashplayers$drop();
            } else {
                // leashplayers$killFireworksOfPlayer(); // Ended up not using this
                this.setDeltaMovement(Vec3.ZERO);
                leashplayers$proxy.setPos(holder.position());
                leashplayers$proxy.setYRot(leashplayers$proxy.getYRot());
                leashplayers$proxy.setXRot(leashplayers$proxy.getXRot());
                connection.teleport(holder.getX(), holder.getY(), holder.getZ(), getYRot(), getXRot());
            }
        }
    }

    @Unique
    private void leashplayers$killFireworksOfPlayer() {
        for (FireworkRocketEntity rocket : level().getEntitiesOfClass(
                FireworkRocketEntity.class,
                getBoundingBox().inflate(FIREWORK_SEARCH_RADIUS),
                rocket -> true
        )) {
            Entity owner = rocket.getOwner();
            if (owner != null) {
                UUID ownerUUID = owner.getUUID();
                if (ownerUUID != null && ownerUUID.equals(this.getUUID())) {
                    rocket.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    @Unique
    private void leashplayers$attach(Entity entity) {
        leashplayers$holder = entity;

        if (leashplayers$proxy == null) {
            leashplayers$proxy = new LeashProxyEntity(this);
            level().addFreshEntity(leashplayers$proxy);
        }
        leashplayers$proxy.setLeashedTo(leashplayers$holder, true);

        if (this.isPassenger() && !this.level().getGameRules().get(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES)) {
            this.stopRiding();
        }

        leashplayers$lastage = tickCount;
    }

    @Unique
    private void leashplayers$detach() {
        leashplayers$holder = null;

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.isAlive() || !leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy.proxyRemove();
            }
            leashplayers$proxy = null;
        }
    }

    @Unique
    private void leashplayers$drop() {
        drop(new ItemStack(Items.LEAD), false, true);
    }

    @Unique
    private double leashplayers$getLeashDistance(ItemStack collar) {
        double distance = getAttributeValue(PlayerCollarsMod.ATTR_LEASH_DISTANCE);
        Holder.Reference<Enchantment> enchantment = level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(SHORT_LEASH_KEY)
                .orElse(null);
        if (enchantment != null) {
            distance -= EnchantmentHelper.getItemEnchantmentLevel(enchantment, collar);
        }
        return Mth.clamp(distance, 2.0, 16.0);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("HEAD"), cancellable = true)
    private void leashplayers$startriding(Entity entity, boolean force, boolean teleport, CallbackInfoReturnable<Boolean> cir) {

        boolean isLeashed = this.leashplayers$getProxyLeashHolder() != null;
        boolean disallowMount = !this.level().getGameRules().get(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES);

        if (isLeashed && disallowMount) {
            this.sendOverlayMessage(Component.translatable("message.playercollars.no_ride_entity"));
            cir.cancel();
        }
    }

    @Override
    public Entity leashplayers$getProxyLeashHolder() {
        return leashplayers$proxy == null ? null : leashplayers$proxy.getLeashHolder();
    }

    @Override
    public InteractionResult leashplayers$interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.LEAD && leashplayers$holder == null) {
            ItemStack is = EquippedTrinkets.findOwned(this, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), player.getUUID(), getUUID());
            if (is == null) return InteractionResult.PASS;
            leashplayer$loyalty = leashplayers$getLeashDistance(is);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            leashplayers$attach(player);
            return InteractionResult.SUCCESS;
        }

        if (leashplayers$holder == player && leashplayers$lastage + 20 < tickCount) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
