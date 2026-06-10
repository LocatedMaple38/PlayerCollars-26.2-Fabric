package org.jlortiz.playercollars.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jlortiz.playercollars.ClientHooks;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class InvisibleFenceBlock extends FenceBlock {
    public static final ResourceKey<Block> REGISTRY_KEY = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    public static final ResourceKey<Item> ITEM_REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public InvisibleFenceBlock(BlockBehaviour.Properties settings) {
        super(settings.setId(REGISTRY_KEY));
        registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        state = super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (neighborState.is(this) && neighborState.getValue(POWERED) != state.getValue(POWERED)) {
            state = state.setValue(POWERED, neighborState.getValue(POWERED));
        }
        return state;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        BlockState neighbor = ctx.getLevel().getBlockState(ctx.getClickedPos().north());
        boolean shouldPower = neighbor.is(this) && neighbor.getValue(POWERED);
        if (!shouldPower) {
            neighbor =  ctx.getLevel().getBlockState(ctx.getClickedPos().east());
            shouldPower = neighbor.is(this) && neighbor.getValue(POWERED);
        }
        if (!shouldPower) {
            neighbor =  ctx.getLevel().getBlockState(ctx.getClickedPos().south());
            shouldPower = neighbor.is(this) && neighbor.getValue(POWERED);
        }
        if (!shouldPower) {
            neighbor =  ctx.getLevel().getBlockState(ctx.getClickedPos().west());
            shouldPower = neighbor.is(this) && neighbor.getValue(POWERED);
        }
        if (shouldPower) state = state.setValue(POWERED, true);
        return state;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext e) {
            if (state.getValue(POWERED) && e.getEntity() instanceof LivingEntity livingEntity) {
                return EquippedTrinkets.hasEquipped(livingEntity, (y) -> y.is(PlayerCollarsMod.COLLAR_TAG)) ?
                        super.getCollisionShape(state, world, pos, context) : Shapes.empty();
            }
            // Vertical collision is cached using EntityShapeContext.ABSENT.
            // This will be re-checked if something actually lands on the fence, so this is safe for players.
            // It can cause unusual behaviour if something tries to pathfind through it, so that is left disabled.
            if (e.getEntity() == null) return super.getCollisionShape(state, world, pos, context);
        }
        return Shapes.empty();
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (state.getValue(POWERED) && random.nextFloat() < 0.25) {

            // 馃檲 Musia's Magic Client Check! Is the person watching the screen a pet?
            if (ClientHooks.shouldSuppressInvisibleFenceParticles()) {
                return;
            }

            ParticleUtils.spawnParticles(world, pos, 1, 0.5, 0.5, true, DustParticleOptions.REDSTONE);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.PASS;
        if (EquippedTrinkets.hasEquipped(player, (y) -> y.is(PlayerCollarsMod.COLLAR_TAG))) {
            player.sendOverlayMessage(Component.translatable("block.playercollars.invisible_fence.toggle_fail").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        state = state.setValue(POWERED, !state.getValue(POWERED));
        world.setBlock(pos, state, 7);
        player.sendOverlayMessage(Component.translatable(
                state.getValue(POWERED) ? "block.playercollars.invisible_fence.toggle_on"
                        : "block.playercollars.invisible_fence.toggle_off")
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (EquippedTrinkets.hasEquipped(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
            return 0.0f;
        }

        return super.getDestroyProgress(state, player, world, pos);
    }
}
