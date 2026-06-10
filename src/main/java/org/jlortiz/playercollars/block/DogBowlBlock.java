package org.jlortiz.playercollars.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Optional;

public class DogBowlBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE_BASE = Shapes.or(
            Block.box(2.0, 0.0, 1.0, 14.0, 5.0, 2.0),
            Block.box(2.0, 0.0, 14.0, 14.0, 5.0, 15.0),
            Block.box(1.0, 0.0, 1.0, 2.0, 5.0, 15.0),
            Block.box(14.0, 0.0, 1.0, 15.0, 5.0, 15.0)
            );
    private static final VoxelShape[] SHAPE = new VoxelShape[] {
            Shapes.or(SHAPE_BASE, Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0)),
            Shapes.or(SHAPE_BASE, Block.box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0)),
            Shapes.or(SHAPE_BASE, Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)),
            Shapes.or(SHAPE_BASE, Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0))
    };
    public static final IntegerProperty LEVEL = BlockStateProperties.AGE_3;
    public static final BooleanProperty MILK = BlockStateProperties.SNOWY;
    public final DyeColor color;

    public DogBowlBlock(DyeColor c, Properties settings) {
        super(settings);
        color = c;
        registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 0).setValue(MILK, false));
    }

    public static ResourceKey<Block> getRegistryKey(DyeColor c) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, c.getName() + "_dog_bowl"));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DogBowlBlockEntity(pos, state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() :
                super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos blockPos = pos.below();
        return canSupportRigidBlock(world, blockPos) || canSupportCenter(world, blockPos, Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int level = state.getValue(LEVEL);
        return (level < 0 || level > 3) ? SHAPE_BASE : SHAPE[level];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return InteractionResult.FAIL;

        if (stack.has(DataComponents.FOOD) && EquippedTrinkets.hasEquipped(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
            if (!world.isClientSide()) {
                player.sendOverlayMessage(Component.literal("Naughty pet! Only your owner can feed you!").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (stack.is(Items.MILK_BUCKET) && be.getCount() == 0) {
            be.insert(stack);
            state = state.setValue(MILK, true);
            world.setBlock(pos, state, 2);
            if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            player.makeSound(SoundEvents.BUCKET_EMPTY);
            return InteractionResult.SUCCESS;
        }

        if (stack.get(DataComponents.FOOD) == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        int decr = be.insert(stack);
        if (decr > 0) {
            stack.shrink(decr);
            state = state.setValue(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
            world.setBlock(pos, state, 2);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.getBlockEntity(pos) instanceof DogBowlBlockEntity be) be.drop();
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return InteractionResult.PASS;
        if (EquippedTrinkets.hasEquipped(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
            return eatFromBowl(state, world, pos, player, be);
        }

        ItemStack is = be.take();
        if (is.isEmpty()) return InteractionResult.PASS;
        if (is.is(Items.MILK_BUCKET)) {
            state = state.setValue(MILK, false);
            world.setBlock(pos, state, 2);
            if (!world.isClientSide()) player.removeAllEffects();
            player.makeSound(SoundEvents.GENERIC_DRINK.value());
            return InteractionResult.SUCCESS;
        }

        state = state.setValue(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
        world.setBlock(pos, state, 2);

        FoodProperties food = is.get(DataComponents.FOOD);
        if (food != null && player.canEat(food.canAlwaysEat())) {
            Consumable consume = is.get(DataComponents.CONSUMABLE);
            if (consume == null) {
                player.getFoodData().eat(food);
            } else {
                consume.onConsume(world, player, is);
            }
            return InteractionResult.SUCCESS;
        } else if (!player.addItem(is)) {
            player.drop(is, true);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult eatFromBowl(BlockState state, Level world, BlockPos pos, Player player, DogBowlBlockEntity be) {
        ItemStack is = be.peekOne();
        if (is.isEmpty()) return InteractionResult.PASS;
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (is.is(Items.MILK_BUCKET)) {
            be.take();
            state = state.setValue(MILK, false);
            world.setBlock(pos, state, 2);
            player.removeAllEffects();
            player.makeSound(SoundEvents.GENERIC_DRINK.value());
            return InteractionResult.SUCCESS;
        }

        FoodProperties food = is.get(DataComponents.FOOD);
        if (food == null || !player.canEat(food.canAlwaysEat())) {
            return InteractionResult.FAIL;
        }

        Consumable consume = is.get(DataComponents.CONSUMABLE);
        if (consume == null) {
            player.getFoodData().eat(food);
        } else {
            consume.onConsume(world, player, is);
        }
        be.take();
        state = state.setValue(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
        world.setBlock(pos, state, 2);
        player.makeSound(SoundEvents.GENERIC_EAT.value());
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, MILK);
    }

    public static class DogBowlBlockEntity extends BlockEntity {
        private ItemStack inBowl = ItemStack.EMPTY;

        public DogBowlBlockEntity(BlockPos pos, BlockState state) {
            super(PlayerCollarsMod.DOG_BOWL_BLOCK_ENTITY, pos, state);
        }

        @Override
        protected void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            inBowl = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            if (!inBowl.isEmpty())
                output.store("item", ItemStack.CODEC, inBowl);
        }

        protected int getCount() {
            return inBowl.getCount();
        }

        protected int insert(ItemStack is) {
            if (inBowl.isEmpty()) {
                inBowl = is.copy();
                setChanged();
                return is.getCount();
            }
            if (is.is(inBowl.getItem())) {
                int count = Math.min(is.getCount(), inBowl.getMaxStackSize() - inBowl.getCount());
                inBowl.grow(count);
                setChanged();
                return count;
            }
            return 0;
        }

        protected ItemStack take() {
            if (inBowl.isEmpty()) return ItemStack.EMPTY;
            ItemStack is = inBowl.copyWithCount(1);
            inBowl.shrink(1);
            setChanged();
            return is;
        }

        protected ItemStack peekOne() {
            if (inBowl.isEmpty()) return ItemStack.EMPTY;
            return inBowl.copyWithCount(1);
        }

        protected void drop() {
            if (inBowl.isEmpty() || inBowl.is(Items.MILK_BUCKET) || level == null) return;
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inBowl));
            inBowl = ItemStack.EMPTY;
        }
    }
}
