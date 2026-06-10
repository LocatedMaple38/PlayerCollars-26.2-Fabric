package org.jlortiz.playercollars.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.ClientHooks;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class DeedItem extends Item {
    public static final ResourceKey<Item> REGISTRY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "deed_of_ownership"));

    public DeedItem() {
        super(new Properties().stacksTo(1).setId(REGISTRY_KEY));
    }

    @Override
    public InteractionResult use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        ItemStack is = p_41433_.getItemInHand(p_41434_);
        if (p_41432_.isClientSide()) {
            OwnerComponent owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
            if (owner != null && owner.owned().isEmpty()) {
                if (owner.uuid().equals(p_41433_.getUUID())) {
                    p_41433_.sendOverlayMessage(Component.translatable("item.playercollars.deed_of_ownership.no_self_own"));
                    return InteractionResult.PASS;
                }
                ClientHooks.openDeedScreen(is, p_41433_);
                return InteractionResult.CONSUME;
            }
        } else if (is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE) == null) {
            is.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, new OwnerComponent(p_41433_.getUUID(), p_41433_.getName().getString()));
            p_41433_.sendOverlayMessage(Component.translatable("item.playercollars.deed_of_ownership.filled_out"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE) != null)
            return Component.translatable("item.playercollars.deed_of_ownership.filled");
        return super.getName(stack);
    }
}
