package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGenerator extends FabricRecipeProvider {
    public RecipeDataGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput recipeExporter) {
        return new RecipeProvider(wrapperLookup, recipeExporter) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, PlayerCollarsMod.COLLAR_ITEM).pattern(" l ").pattern("lil").pattern(" d ")
                        .define('l', Items.LEATHER)
                        .define('i', ConventionalItemTags.GOLD_INGOTS)
                        .define('d', ConventionalItemTags.DYES)
                        .unlockedBy(getHasName(Items.LEATHER), has(Items.LEATHER))
                        .save(output);
                shaped(RecipeCategory.MISC, PlayerCollarsMod.TAGLESS_COLLAR_ITEM).pattern(" l ").pattern("ldl")
                        .define('l', Items.LEATHER)
                        .define('d', ConventionalItemTags.DYES)
                        .unlockedBy(getHasName(Items.LEATHER), has(Items.LEATHER))
                        .save(output);
                shaped(RecipeCategory.MISC, PlayerCollarsMod.CLICKER_ITEM).pattern(" b ").pattern("pip").pattern(" p ")
                        .define('b', ItemTags.BUTTONS)
                        .define('i', ConventionalItemTags.IRON_INGOTS)
                        .define('p', ItemTags.PLANKS)
                        .unlockedBy(getHasName(Items.IRON_INGOT), has(ConventionalItemTags.IRON_INGOTS))
                        .save(output);
                shapeless(RecipeCategory.TOOLS, PlayerCollarsMod.PAW_CONFIGURATION_ITEM)
                        .requires(ConventionalItemTags.REDSTONE_DUSTS)
                        .requires(ConventionalItemTags.COPPER_INGOTS)
                        .requires(PlayerCollarsMod.COLLAR_LOCKER_ITEM)
                        .unlockedBy("has_paws", has(PlayerCollarsMod.PAWS_TAG))
                        .save(output);
                shapeless(RecipeCategory.TOOLS, PlayerCollarsMod.COLLAR_LOCKER_ITEM)
                        .requires(Items.REDSTONE)
                        .requires(Items.IRON_INGOT)
                        .requires(Items.IRON_INGOT)
                        .requires(Items.IRON_BARS)
                        .unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS))
                        .save(output);
                shapeless(RecipeCategory.MISC, PlayerCollarsMod.DEED_OF_OWNERSHIP)
                        .requires(Items.PAPER)
                        .requires(Items.LEAD)
                        .requires(Items.INK_SAC)
                        .requires(Items.FEATHER)
                        .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                        .save(output);
                shaped(RecipeCategory.BUILDING_BLOCKS, PlayerCollarsMod.INVISIBLE_FENCE_BLOCK_ITEM, 3).pattern("grg").pattern("srs")
                        .define('r', Items.REDSTONE)
                        .define('g', Items.GLASS_PANE)
                        .define('s', Items.STONE)
                        .unlockedBy(getHasName(Items.REDSTONE), has(Items.REDSTONE))
                        .save(output);
                shaped(RecipeCategory.TOOLS, PlayerCollarsMod.SPATULA_ITEM).pattern("  g").pattern(" g ").pattern("s  ")
                        .define('g', ConventionalItemTags.GOLD_INGOTS)
                        .define('s', Items.STICK)
                        .unlockedBy(getHasName(Items.GOLD_INGOT), has(ConventionalItemTags.GOLD_INGOTS))
                        .save(output);
                shaped(RecipeCategory.TOOLS, PlayerCollarsMod.GROOMING_BRUSH_ITEM)
                        .pattern(" wd")
                        .pattern(" c ")
                        .define('w', Items.WHEAT)
                        .define('c', Items.COPPER_INGOT)
                        .define('d', Items.WHITE_DYE)
                        .unlockedBy(getHasName(Items.COPPER_INGOT), has(ConventionalItemTags.COPPER_INGOTS))
                        .save(output);
                shaped(RecipeCategory.TOOLS, PlayerCollarsMod.LASER_POINTER_ITEM)
                        .pattern(" g ")
                        .pattern(" e ")
                        .pattern(" i ")
                        .define('g', Items.GLASS_PANE)
                        .define('e', Items.EMERALD)
                        .define('i', ConventionalItemTags.IRON_INGOTS)
                        .unlockedBy(getHasName(Items.EMERALD), has(Items.EMERALD))
                        .save(output);
                shaped(RecipeCategory.TOOLS, PlayerCollarsMod.REWARD_TREAT_POUCH_ITEM)
                        .pattern("sls")
                        .pattern("bcb")
                        .pattern(" w ")
                        .define('s', Items.STRING)
                        .define('l', Items.LEATHER)
                        .define('b', Items.BONE)
                        .define('c', Items.COOKIE)
                        .define('w', Items.WHEAT)
                        .unlockedBy(getHasName(Items.COOKIE), has(Items.COOKIE))
                        .save(output);
                for (DyeColor c : DyeColor.values()) {
                    generateBed(output, PlayerCollarsMod.DOG_BED_ITEMS[c.ordinal()], DatagenEntrypoint.WOOLS[c.ordinal()]);
                    generateBowl(output, PlayerCollarsMod.DOG_BOWL_ITEMS[c.ordinal()], DatagenEntrypoint.TERRACOTTAS[c.ordinal()]);
                }
                for (int i = 0; i < PlayerCollarsMod.PAWS_DYE_COLORS.length; i++) {
                    generatePaws(output, PlayerCollarsMod.PAWS_ITEMS[i], DatagenEntrypoint.WOOLS[PlayerCollarsMod.PAWS_DYE_COLORS[i].ordinal()]);
                    generateFootPaws(output, PlayerCollarsMod.FOOT_PAWS_ITEMS[i], DatagenEntrypoint.WOOLS[PlayerCollarsMod.PAWS_DYE_COLORS[i].ordinal()]);
                }
            }

            private void generateBed(RecipeOutput exporter, BedItem output, Item input) {
                shaped(RecipeCategory.DECORATIONS, output).pattern("w w").pattern("www")
                        .define('w', input)
                        .unlockedBy(getHasName(input), has(input))
                        .group("dog_bed")
                        .save(exporter);
            }

            private void generatePaws(RecipeOutput exporter, PawsItem output, Item input) {
                shaped(RecipeCategory.MISC, output).pattern(" w ").pattern("wlw").pattern(" w ")
                        .define('w', input)
                        .define('l', Items.LEATHER)
                        .unlockedBy(getHasName(input), has(input))
                        .group("paws")
                        .save(exporter);
            }

            private void generateFootPaws(RecipeOutput exporter, Item output, Item input) {
                shaped(RecipeCategory.MISC, output).pattern(" w ").pattern(" w ").pattern("wlw")
                        .define('w', input)
                        .define('l', Items.LEATHER)
                        .unlockedBy(getHasName(Items.LEATHER), has(Items.LEATHER))
                        .group("foot_paws")
                        .save(exporter);
            }

            private void generateBowl(RecipeOutput exporter, Item output, Item input) {
                shaped(RecipeCategory.DECORATIONS, output).pattern("w w").pattern("www")
                        .define('w', input)
                        .unlockedBy(getHasName(input), has(input))
                        .group("dog_bowl")
                        .save(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return PlayerCollarsMod.MOD_ID + "_recipe_generator";
    }
}
