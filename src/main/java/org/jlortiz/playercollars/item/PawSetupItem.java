package org.jlortiz.playercollars.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class PawSetupItem extends Item {
    public static final ResourceKey<Item> REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "paw_configurator"));

    public PawSetupItem() {
        super(new Properties().stacksTo(1).setId(REGISTRY_KEY));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack is = user.getItemInHand(hand);
        if (!user.isShiftKeyDown()) return InteractionResult.PASS;
        return interactLivingEntity(is, user, user, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof Player player)) return InteractionResult.PASS;

        ItemStack collarStack = EquippedTrinkets.findOwned(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), user.getUUID(), player.getUUID());
        if (collarStack == null) {
            if (user.level().isClientSide()) {
                user.sendOverlayMessage(Component.translatable("item.playercollars.paw_configurator.no_set_non_owner").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (user.isShiftKeyDown()) {
            if (!user.level().isClientSide()) {
                // Check if the owner is holding food in their off-hand to toggle Diet Control
                if (user.getOffhandItem().has(DataComponents.FOOD)) {
                    boolean isDiet = collarStack.getOrDefault(PlayerCollarsMod.DIET_CONTROL_COMPONENT_TYPE, false);
                    collarStack.set(PlayerCollarsMod.DIET_CONTROL_COMPONENT_TYPE, !isDiet);
                    user.sendOverlayMessage(Component.literal("Pet diet control set to: " + !isDiet).withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    // Otherwise, toggle the Crawl state
                    boolean isCrawling = collarStack.getOrDefault(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, false);
                    collarStack.set(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, !isCrawling);
                    user.sendOverlayMessage(Component.literal("Pet forced crawl set to: " + !isCrawling).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            user.sendOverlayMessage(Component.literal("Paw Patroller config screens are disabled; paws use fixed rules.").withStyle(ChatFormatting.LIGHT_PURPLE));
            return InteractionResult.SUCCESS;
        }
    }
}
