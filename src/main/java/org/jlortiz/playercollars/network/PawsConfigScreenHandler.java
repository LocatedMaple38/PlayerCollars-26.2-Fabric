package org.jlortiz.playercollars.network;

import com.mojang.datafixers.util.Either;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public abstract class PawsConfigScreenHandler<T extends ItemLike> extends AbstractContainerMenu {
    private final Container inventory;
    public final List<Either<TagKey<T>, ResourceKey<T>>> data;
    public List<Either<TagKey<T>, ResourceKey<T>>> listToDisplay;
    protected ItemStack[] pawsStacks;
    protected final Level world;

    public PawsConfigScreenHandler(MenuType<? extends PawsConfigScreenHandler<T>> id, int syncId,
                                   Inventory playerInventory, List<Either<TagKey<T>, ResourceKey<T>>> data) {
        super(id, syncId);
        this.inventory = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                PawsConfigScreenHandler.this.slotsChanged(this);
            }
        };
        this.data = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        this.listToDisplay = data;
        this.world = playerInventory.player.level();
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 176, 108) {
            @Override
            public boolean allowModification(Player playerEntity) {
                return false;
            }

            @Override
            public Optional<ItemStack> tryRemove(int min, int max, Player player) {
                setByPlayer(ItemStack.EMPTY);
                return Optional.of(ItemStack.EMPTY);
            }

            @Override
            public ItemStack remove(int amount) {
                setByPlayer(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack safeInsert(ItemStack stack) {
                this.container.setItem(0, stack.copyWithCount(1));
                return stack;
            }
        });

        for(int j = 0; j < 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 7 + k * 18, 140 + j * 18));
            }
        }

        for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 7 + j * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        this.inventory.setItem(0, getSlot(slot).getItem().copyWithCount(1));
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex == 0) {
            ItemStack is = this.getCarried();
            if (is == null) is = ItemStack.EMPTY;
            this.inventory.setItem(0, is.copyWithCount(1));
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0) return false;
        if (inventory.getItem(0).isEmpty()) {
            if (id >= data.size()) return false;
            data.remove(id);
        } else {
            if (id >= listToDisplay.size()) return false;
            data.add(listToDisplay.get(id));
        }
        return true;
    }

    public void setPawsStack(ItemStack[] is) {
        if (pawsStacks == null)
            pawsStacks = is;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        ItemStack is = inventory.getItem(0);
        this.listToDisplay = is.isEmpty() ? data : genForItem(is.getItem());
    }

    protected abstract List<Either<TagKey<T>, ResourceKey<T>>> genForItem(Item item);

    public abstract ResourceKey<Registry<T>> getRegistryKey();

    public static class PawsBlockConfigScreenHandler extends PawsConfigScreenHandler<Block> {
        public PawsBlockConfigScreenHandler(int syncId, Inventory playerInventory, List<Either<TagKey<Block>, ResourceKey<Block>>> data) {
            super(PlayerCollarsMod.PAWS_BLOCK_CONFIG_SCREEN_HANDLER, syncId, playerInventory, data);
        }

        protected List<Either<TagKey<Block>, ResourceKey<Block>>> genForItem(Item item) {
            if (!(item instanceof BlockItem bi)) return List.of();
            Holder<Block> entry = world.registryAccess().lookupOrThrow(Registries.BLOCK).wrapAsHolder(bi.getBlock());
            Stream<Either<TagKey<Block>, ResourceKey<Block>>> tags = entry.tags().map(Either::left);
            if (entry.unwrapKey().isPresent()) {
                tags = Stream.concat(Stream.of(Either.right(entry.unwrapKey().get())), tags);
            }
            return tags.toList();
        }

        @Override
        public ResourceKey<Registry<Block>> getRegistryKey() {
            return Registries.BLOCK;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            if (pawsStacks != null)
                for (ItemStack ps : pawsStacks)
                    ps.set(PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE, data.isEmpty() ? null : data);
        }
    }

    public static class PawsItemConfigScreenHandler extends PawsConfigScreenHandler<Item> {
        public PawsItemConfigScreenHandler(int syncId, Inventory playerInventory, List<Either<TagKey<Item>, ResourceKey<Item>>> data) {
            super(PlayerCollarsMod.PAWS_ITEM_CONFIG_SCREEN_HANDLER, syncId, playerInventory, data);
        }

        protected List<Either<TagKey<Item>, ResourceKey<Item>>> genForItem(Item item) {
            Holder<Item> entry = world.registryAccess().lookupOrThrow(Registries.ITEM).wrapAsHolder(item);
            Stream<Either<TagKey<Item>, ResourceKey<Item>>> tags = entry.tags().map(Either::left);
            if (entry.unwrapKey().isPresent()) {
                tags = Stream.concat(Stream.of(Either.right(entry.unwrapKey().get())), tags);
            }
            return tags.toList();
        }

        @Override
        public ResourceKey<Registry<Item>> getRegistryKey() {
            return Registries.ITEM;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            if (pawsStacks != null)
                for (ItemStack ps : pawsStacks)
                    ps.set(PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE, data.isEmpty() ? null : data);
        }
    }
}
