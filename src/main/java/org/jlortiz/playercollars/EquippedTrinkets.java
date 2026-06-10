package org.jlortiz.playercollars;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EquippedTrinkets {
    private EquippedTrinkets() {
    }

    public record EquippedStack(TrinketSlotAccess slot, ItemStack stack) {
    }

    public static List<ItemStack> getEquipped(LivingEntity entity, Predicate<ItemStack> predicate) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return List.of();

        List<ItemStack> stacks = new ArrayList<>();
        attachment.forEach((slot, stack) -> {
            if (!stack.isEmpty() && predicate.test(stack)) {
                stacks.add(stack);
            }
        });
        return stacks;
    }

    public static List<EquippedStack> getEquippedStacks(LivingEntity entity, Predicate<ItemStack> predicate) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return List.of();

        List<EquippedStack> stacks = new ArrayList<>();
        attachment.forEach((slot, stack) -> {
            if (!stack.isEmpty() && predicate.test(stack)) {
                stacks.add(new EquippedStack(slot, stack));
            }
        });
        return stacks;
    }

    public static List<EquippedStack> getAllEquippedStacks(LivingEntity entity) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return List.of();

        List<EquippedStack> stacks = new ArrayList<>();
        attachment.forEach((slot, stack) -> {
            if (!stack.isEmpty()) {
                stacks.add(new EquippedStack(slot, stack));
            }
        });
        return stacks;
    }

    public static void forEachEquipped(LivingEntity entity, Consumer<ItemStack> consumer) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return;

        attachment.forEach((slot, stack) -> {
            if (!stack.isEmpty()) {
                consumer.accept(stack);
            }
        });
    }

    public static boolean hasEquipped(LivingEntity entity, Predicate<ItemStack> predicate) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return false;
        return attachment.isEquipped(stack -> !stack.isEmpty() && predicate.test(stack));
    }

    @Nullable
    public static ItemStack findOwned(LivingEntity entity, Predicate<ItemStack> predicate, UUID owner, UUID wearer) {
        return firstEquipped(entity, stack -> {
            if (!predicate.test(stack)) return false;
            OwnerComponent ownerComponent = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
            return ownerComponent != null
                    && ownerComponent.uuid().equals(owner)
                    && (ownerComponent.owned().isEmpty() || ownerComponent.owned().get().equals(wearer));
        });
    }

    @Nullable
    public static ItemStack firstEquipped(LivingEntity entity, Predicate<ItemStack> predicate) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return null;

        ItemStack[] found = new ItemStack[1];
        attachment.forEachWhileTrue((slot, stack) -> {
            if (!stack.isEmpty() && predicate.test(stack)) {
                found[0] = stack;
                return false;
            }
            return true;
        });
        return found[0];
    }
}
