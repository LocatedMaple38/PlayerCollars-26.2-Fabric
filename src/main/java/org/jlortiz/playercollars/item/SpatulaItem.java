package org.jlortiz.playercollars.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class SpatulaItem extends Item {
    public static final ResourceKey<Item> REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "golden_spatula"));
    public SpatulaItem() {
        super(new Item.Properties().stacksTo(1).durability(8).setId(REGISTRY_KEY));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user.isShiftKeyDown()) {
            InteractionResult res = interactLivingEntity(user.getItemInHand(hand), user, user, hand);
            if (res.consumesAction()) return InteractionResult.SUCCESS;
        }
        return super.use(world, user, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (entity.level().isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel world = (ServerLevel) entity.level();

        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack is = entity.getItemBySlot(slot);
            if (EnchantmentHelper.has(is, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
                entity.spawnAtLocation(world, is);
                count++;
            }
        }

        for (EquippedTrinkets.EquippedStack p : EquippedTrinkets.getAllEquippedStacks(entity)) {
            if (EnchantmentHelper.has(p.stack(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                ItemStack trinketStack = p.stack();
                if (p.slot().set(ItemStack.EMPTY)) {
                    entity.spawnAtLocation(world, trinketStack);
                    count++;
                }
            }
        }

        if (count == 0) return InteractionResult.PASS;
        stack.hurtAndBreak(count, user, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        entity.makeSound(SoundEvents.WOLF_ARMOR_BREAK.value());
        return InteractionResult.SUCCESS;
    }
}
