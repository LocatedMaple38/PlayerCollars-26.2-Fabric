package org.jlortiz.playercollars.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

import java.util.List;
import java.util.function.Consumer;

public class ClickerItem extends Item {
    public static final ResourceKey<Item> REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "clicker"));
    public ClickerItem() {
        super(new Item.Properties().stacksTo(1).setId(REGISTRY_KEY)
                .component(DataComponents.ENCHANTABLE, new Enchantable(45)));
    }

    @Override
    public InteractionResult use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        p_41433_.startUsingItem(p_41434_);
        if (!p_41432_.isClientSide()) {
            ItemStack is = p_41433_.getItemInHand(p_41434_);
            if (p_41433_.isShiftKeyDown()) {
                if (is.has(DataComponents.INTANGIBLE_PROJECTILE)) {
                    is.remove(DataComponents.INTANGIBLE_PROJECTILE);
                    p_41433_.sendOverlayMessage(Component.translatable("item.playercollars.clicker.turn_disable"));
                } else {
                    is.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                    p_41433_.sendOverlayMessage(Component.translatable("item.playercollars.clicker.turn_enable"));
                }
                return InteractionResult.CONSUME;
            }

            double distance = p_41433_.getAttributeValue(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
            if (distance > 0 && is.has(DataComponents.INTANGIBLE_PROJECTILE)) {
                List<ServerPlayer> plrs = ((ServerLevel) p_41432_).getPlayers((p) -> !p.is(p_41433_) && p.closerThan(p_41433_, distance));
                PacketLookAtLerped packet = new PacketLookAtLerped(p_41433_);
                for (ServerPlayer p : plrs) {
                    ItemStack collar = EquippedTrinkets.findOwned(p, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), p_41433_.getUUID(), p.getUUID());
                    if (collar != null) {
                        ServerPlayNetworking.send(p, packet);
                    }
                }
            }
            p_41432_.playSound(null, p_41433_, PlayerCollarsMod.CLICKER_ON, SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack p_41454_, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean releaseUsing(ItemStack p_41412_, Level p_41413_, LivingEntity p_41414_, int p_41415_) {
        if (!p_41413_.isClientSide()) {
            p_41413_.playSound(null, p_41414_, PlayerCollarsMod.CLICKER_OFF, SoundSource.PLAYERS, 1, 1);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (stack.has(DataComponents.INTANGIBLE_PROJECTILE))
            tooltip.accept(Component.translatable("item.playercollars.clicker.turn"));
    }
}
