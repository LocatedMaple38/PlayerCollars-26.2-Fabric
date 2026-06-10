package org.jlortiz.playercollars.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow @Final Inventory inventory;

    @Shadow @Nullable public abstract ItemEntity drop(ItemStack stack, boolean retainOwnership);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void playercollars$modifyPawDestroySpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (!EquippedTrinkets.hasEquipped(this, (x) -> x.is(PlayerCollarsMod.PAWS_TAG))) return;

        cir.setReturnValue(Math.max(cir.getReturnValue() * 0.1f, 0.05f));
    }

    @Redirect(method="attack", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D", ordinal=0), require=0)
    private double getAttributeValue(Player instance, Holder<Attribute> registryEntry) {
        double ret = instance.getAttributeValue(registryEntry);
        if (!EquippedTrinkets.hasEquipped(this, (x) -> x.is(PlayerCollarsMod.PAWS_TAG))) return ret;
        return (ret - 1) * 0.75f + 1;
    }

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(PlayerCollarsMod.ATTR_LEASH_DISTANCE).add(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
    }

    @Inject(method = "aiStep", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;tick()V", shift = At.Shift.AFTER))
    private void playercollars$dropPawItems(CallbackInfo ci) {
        if (level().isClientSide()) return;
        for (ItemStack pawsStack : EquippedTrinkets.getEquipped(this, (x) -> x.is(PlayerCollarsMod.PAWS_TAG))) {
            if (PawsItem.shouldDrop(pawsStack, inventory.getSelectedItem())) {
                playercollars$moveHeldItemAway(inventory.getSelectedSlot());
            }
            if (PawsItem.shouldDrop(pawsStack, inventory.getItem(Inventory.SLOT_OFFHAND))) {
                playercollars$moveHeldItemAway(Inventory.SLOT_OFFHAND);
            }
        }
    }

    @Unique
    private void playercollars$moveHeldItemAway(int slot) {
        ItemStack stack = inventory.removeItemNoUpdate(slot);
        if (stack.isEmpty()) return;

        int selected = inventory.getSelectedSlot();
        int inventorySize = Math.min(36, inventory.getContainerSize());
        for (int i = 0; i < inventorySize; i++) {
            if (i == selected || i == Inventory.SLOT_OFFHAND || i == slot) continue;
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, stack);
                return;
            }
        }

        drop(stack, true);
    }

    @Inject(method="updatePlayerPose", at= @At("TAIL"))
    private void playercollars$forceCrawl(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Pose pose = player.getPose();

        if (!player.getAbilities().flying && (pose == Pose.CROUCHING || pose == Pose.STANDING)) {
            for (ItemStack collarStack : EquippedTrinkets.getEquipped(this, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
                if (collarStack.getOrDefault(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, false)) {
                    player.setPose(Pose.SWIMMING);
                    return;
                }
            }
        }
    }
}
