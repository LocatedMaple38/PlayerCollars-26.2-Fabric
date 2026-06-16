package org.jlortiz.playercollars.datagen;

import io.wispforest.accessories.Accessories;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
    public static final BlockItem[] WOOLS = new BlockItem[DyeColor.values().length];
    public static final BlockItem[] TERRACOTTAS = new BlockItem[DyeColor.values().length];
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        for (DyeColor c : DyeColor.values()) {
            WOOLS[c.ordinal()] = (BlockItem) BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(c.getName() + "_wool"));
            TERRACOTTAS[c.ordinal()] = (BlockItem) BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(c.getName() + "_terracotta"));
        }

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeDataGenerator::new);
        pack.addProvider(ModelDataGenerator::new);
        pack.addProvider(LootTableGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(BlockTagGenerator::new);
        pack.addProvider(EnglishLangProvider::new);
    }

    private static class LootTableGenerator extends FabricBlockLootTableProvider {
        protected LootTableGenerator(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generate() {
            for (int i = 0; i < PlayerCollarsMod.DOG_BEDS.length; i++)
                add(PlayerCollarsMod.DOG_BEDS[i], createSinglePropConditionTable(PlayerCollarsMod.DOG_BEDS[i], BedBlock.PART, BedPart.HEAD));
            for (int i = 0; i < PlayerCollarsMod.DOG_BOWLS.length; i++)
                add(PlayerCollarsMod.DOG_BOWLS[i], createSingleItemTable(PlayerCollarsMod.DOG_BOWL_ITEMS[i]));
            add(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, createSingleItemTable(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK));
        }
    }

    private static class ItemTagGenerator extends FabricTagProvider<Item> {
        public ItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.ITEM, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            tag(ItemTags.DYEABLE).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.CLICKER_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
            tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Accessories.MODID, "necklace"))).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
            tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Accessories.MODID, "hand"))).add(PlayerCollarsMod.PAWS_ITEMS);
            tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Accessories.MODID, "shoes"))).addTag(PlayerCollarsMod.FOOT_PAWS_TAG);
            tag(PlayerCollarsMod.PAWS_TAG).add(PlayerCollarsMod.PAWS_ITEMS);
            tag(PlayerCollarsMod.FOOT_PAWS_TAG).add(PlayerCollarsMod.FOOT_PAWS_ITEMS);
            tag(PlayerCollarsMod.COLLAR_TAG).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM)
                    .addOptionalTag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("petworks", "collars")));
        }
    }

    private static class BlockTagGenerator extends FabricTagProvider<Block> {
        public BlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.BLOCK, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            tag(BlockTags.BEDS).add(PlayerCollarsMod.DOG_BEDS);
            tag(BlockTags.FENCES).add(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK);
        }
    }

    private static class EnglishLangProvider extends FabricLanguageProvider {
        protected EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, registryLookup);
        }

        private static void generateColorNames(TranslationBuilder translationBuilder, String suffix, Function<Integer, DyeColor> getColor, Item... items) {
            String[] keys = new String[items.length];
            for (int i = 0; i < items.length; i++) {
                keys[i] = items[i].getDescriptionId();
            }
            generateColorNames(translationBuilder, suffix, getColor, keys);
        }

        private static void generateColorNames(TranslationBuilder translationBuilder, String suffix, Function<Integer, DyeColor> getColor, Block... blocks) {
            String[] keys = new String[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                keys[i] = blocks[i].getDescriptionId();
            }
            generateColorNames(translationBuilder, suffix, getColor, keys);
        }

        private static void generateColorNames(TranslationBuilder translationBuilder, String suffix, Function<Integer, DyeColor> getColor, String... keys) {
            for (int i = 0; i < keys.length; i++) {
                String pre = getColor.apply(i).getName();
                char []buf = new char[pre.length() + suffix.length()];
                boolean newWord = true;
                for (int j = 0; j < pre.length(); j++) {
                    char c = pre.charAt(j);
                    if (c == '_') {
                        c = ' ';
                        newWord = true;
                    } else if (newWord) {
                        c = Character.toUpperCase(c);
                        newWord = false;
                    }
                    buf[j] = c;
                }
                suffix.getChars(0, buf.length - pre.length(), buf, pre.length());
                translationBuilder.add(keys[i], String.valueOf(buf));
            }
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
            generateColorNames(translationBuilder, " Human-Sized Dog Bed", DyeColor::byId, PlayerCollarsMod.DOG_BED_ITEMS);
            generateColorNames(translationBuilder, " Human-Sized Dog Bed", DyeColor::byId, PlayerCollarsMod.DOG_BEDS);
            generateColorNames(translationBuilder, " Paws", (i) -> PlayerCollarsMod.PAWS_DYE_COLORS[i], PlayerCollarsMod.PAWS_ITEMS);
            generateColorNames(translationBuilder, " Foot Paws", (i) -> PlayerCollarsMod.PAWS_DYE_COLORS[i], PlayerCollarsMod.FOOT_PAWS_ITEMS);
            generateColorNames(translationBuilder, " Dog Bowl", DyeColor::byId, PlayerCollarsMod.DOG_BOWL_ITEMS);
            generateColorNames(translationBuilder, " Dog Bowl", DyeColor::byId, PlayerCollarsMod.DOG_BOWLS);

            try {
                Path existingFilePath = dataOutput.getModContainer().findPath("assets/" + PlayerCollarsMod.MOD_ID + "/lang/en_us.existing.json").get();
                translationBuilder.add(existingFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add existing language file!", e);
            }
        }
    }
}
