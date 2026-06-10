package org.jlortiz.playercollars.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class DogBedBlock extends BedBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 6, 16);

    public DogBedBlock(DyeColor color, ResourceKey<Block> key) {
        super(color, BlockBehaviour.Properties.of()
                .sound(SoundType.WOOL).strength(0.2F).noOcclusion().ignitedByLava()
                .pushReaction(PushReaction.DESTROY).setId(key));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public static ResourceKey<Block> getRegistryKey(DyeColor c) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, c.getName() + "_dog_bed"));
    }
}
