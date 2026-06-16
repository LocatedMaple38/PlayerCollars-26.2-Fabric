package org.jlortiz.playercollars.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.PetControlHelper;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketOpenPetControl;

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

        if (user.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(user instanceof ServerPlayer owner) || !(player instanceof ServerPlayer pet)) return InteractionResult.FAIL;

        PetControlHelper.ValidationResult result = PetControlHelper.validateOwnerControl(owner, pet);
        if (!result.successful()) {
            if (result.failure() != null) owner.sendOverlayMessage(result.failure().message());
            return InteractionResult.FAIL;
        }

        if (user.isShiftKeyDown()) {
            ItemStack collarStack = result.activeCollar().collar();
            boolean isCrawling = collarStack.getOrDefault(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, false);
            collarStack.set(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, !isCrawling);
            user.sendOverlayMessage(Component.translatable(
                    !isCrawling
                            ? "message.playercollars.pet_control.forced_crawl.enabled"
                            : "message.playercollars.pet_control.forced_crawl.disabled"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            return InteractionResult.SUCCESS;
        } else {
            ServerPlayNetworking.send(owner, PacketOpenPetControl.from(pet, result.activeCollar().collar()));
            return InteractionResult.SUCCESS;
        }
    }
}
