package org.jlortiz.playercollars.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;

public class OwnershipCraftingRecipe extends CustomRecipe {
    private PlacementInfo ingredientPlacement;
    private final CraftingBookCategory category;
    private final Ingredient base;

    public OwnershipCraftingRecipe(CraftingBookCategory category, Ingredient base) {
        super();
        this.category = category;
        this.base = base;
    }

    public boolean matches(CraftingInput craftingRecipeInput, Level world) {
        if (!craftingRecipeInput.stackedContents().canCraft(this, null)) return false;

        for (int i = 0; i < craftingRecipeInput.size(); i++) {
            ItemStack is = craftingRecipeInput.getItem(i);
            if (base.test(is) && is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE) != null) return false;
        }
        return true;
    }

    public ItemStack assemble(CraftingInput craftingRecipeInput) {
        ItemStack output = ItemStack.EMPTY;
        OwnerComponent owner = null;

        for(int j = 0; j < craftingRecipeInput.size(); j++) {
            ItemStack is = craftingRecipeInput.getItem(j);
            if (!is.isEmpty()) {
                if (is.is(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED)) {
                    owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
                } else if (base.test(is)) {
                    output = is.copy();
                }
            }
        }

        if (owner == null || output.isEmpty()) return ItemStack.EMPTY;
        output.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, owner);
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingRecipeInput) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(craftingRecipeInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < craftingRecipeInput.size(); i++) {
            ItemStack stack = craftingRecipeInput.getItem(i);
            if (stack.is(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED)) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    private Ingredient getBase() {
        return base;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (ingredientPlacement == null) {
            ingredientPlacement = PlacementInfo.create(List.of(base, Ingredient.of(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED)));
        }

        return ingredientPlacement;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return org.jlortiz.playercollars.item.OwnershipCraftingRecipe.Serializer.INSTANCE;
    }

    public static class Serializer {
        private static final MapCodec<OwnershipCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
            Ingredient.CODEC.fieldOf("base").forGetter(OwnershipCraftingRecipe::getBase)
        ).apply(builder, OwnershipCraftingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, OwnershipCraftingRecipe> PACKET_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, OwnershipCraftingRecipe::getBase, OwnershipCraftingRecipe::new
        );
        public static final RecipeSerializer<OwnershipCraftingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, PACKET_CODEC);
    }
}
