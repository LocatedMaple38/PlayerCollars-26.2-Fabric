package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.MapColor;
import net.minecraft.client.data.*;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.item.FootPawsItem;

import java.util.Optional;

public class ModelDataGenerator extends FabricModelProvider {
    public ModelDataGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        ModelTemplate baseModel = new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/white_dog_bed")), Optional.empty(), TextureSlot.PARTICLE);
        for (DogBedBlock bed : PlayerCollarsMod.DOG_BEDS) {
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(bed)
                    .with(PropertyDispatch.properties(BedBlock.FACING, BedBlock.PART).generate(
                            (dir, part) -> {
                                if (part == BedPart.HEAD)
                                    dir = dir.getOpposite();
                                return Variant.variant()
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.values()[dir.get2DDataValue()])
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R0)
                                        .with(VariantProperties.MODEL, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/" + bed.getColor().getName() + "_dog_bed"));
                            }
                    )));
            if (bed.getColor() != DyeColor.WHITE)
                baseModel.create(bed, TextureMapping.particle(DatagenEntrypoint.WOOLS[bed.getColor().ordinal()].getBlock()), blockStateModelGenerator.modelOutput);
        }

        ModelTemplate[] bowlModels = new ModelTemplate[5];
        for (int i = 0; i < 4; i++) {
            bowlModels[i] = new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/red_dog_bowl_"+i)), Optional.empty(), TextureSlot.PARTICLE);
        }
        bowlModels[4] = new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/red_dog_bowl_milk")), Optional.empty(), TextureSlot.PARTICLE);
        for (DogBowlBlock bowl : PlayerCollarsMod.DOG_BOWLS) {
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(bowl)
                    .with(PropertyDispatch.properties(DogBowlBlock.MILK, DogBowlBlock.LEVEL).generate((milk, level) -> Variant.variant()
                            .with(VariantProperties.MODEL, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_" + (milk ? "milk" :level))))));
            if (bowl.color != DyeColor.RED) {
                for (int i = 0; i < 4; i++)
                    bowlModels[i].create(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_"+i),
                            TextureMapping.particle(DatagenEntrypoint.TERRACOTTAS[bowl.color.ordinal()].getBlock()), blockStateModelGenerator.modelOutput);
                bowlModels[4].create(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_milk"),
                        TextureMapping.particle(DatagenEntrypoint.TERRACOTTAS[bowl.color.ordinal()].getBlock()), blockStateModelGenerator.modelOutput);
            }
        }

        TextureMapping glassTexture = TextureMapping.cube(Blocks.GLASS);
        Identifier glassPost = ModelTemplates.FENCE_POST.create(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassTexture, blockStateModelGenerator.modelOutput);
        Identifier glassSide = ModelTemplates.FENCE_SIDE.create(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassTexture, blockStateModelGenerator.modelOutput);
        blockStateModelGenerator.blockStateOutput.accept(BlockModelGenerators.createFence(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassPost, glassSide));
        blockStateModelGenerator.registerSimpleItemModel(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, ModelTemplates.FENCE_INVENTORY.create(ModelLocationUtils.getModelLocation(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK_ITEM),
                TextureMapping.cube(Blocks.GLASS), blockStateModelGenerator.modelOutput));
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        Item whiteBed = PlayerCollarsMod.DOG_BED_ITEMS[DyeColor.WHITE.ordinal()];
        Identifier bedItemModel = itemModelGenerator.createFlatItemModel(whiteBed, whiteBed, ModelTemplates.FLAT_ITEM);
        for (int i = 0; i < DyeColor.values().length; i++) {
            ItemModel.Unbaked m = ItemModelUtils.tintedModel(bedItemModel, new Constant(DyeColor.values()[i].getFireworkColor()));
            itemModelGenerator.itemModelOutput.accept(PlayerCollarsMod.DOG_BED_ITEMS[i], m);
        }

        Identifier pawsModel = Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "item/paws");
        for (FootPawsItem i : PlayerCollarsMod.PAWS_ITEMS) {
            itemModelGenerator.itemModelOutput.accept(i, ItemModelUtils.tintedModel(pawsModel, new Dye(i.color), new MapColor(i.beansColor)));
        }
        pawsModel = Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "item/foot_paws");
        for (FootPawsItem i : PlayerCollarsMod.FOOT_PAWS_ITEMS) {
            itemModelGenerator.itemModelOutput.accept(i, ItemModelUtils.tintedModel(pawsModel, new Dye(i.color), new MapColor(i.beansColor)));
        }

        itemModelGenerator.generateFlatItem(PlayerCollarsMod.DEED_OF_OWNERSHIP, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PlayerCollarsMod.SPATULA_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(PlayerCollarsMod.COLLAR_LOCKER_ITEM, ModelTemplates.FLAT_ITEM);

        for (DyeColor c : DyeColor.values()) {
            ModelTemplate baseBowl = new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "block/" + c.getName() + "_dog_bowl_3")), Optional.empty());
            itemModelGenerator.generateFlatItem(PlayerCollarsMod.DOG_BOWL_ITEMS[c.ordinal()], baseBowl);
        }
    }
}
