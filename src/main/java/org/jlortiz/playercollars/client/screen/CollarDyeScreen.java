package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.MapItemColor;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.network.PacketUpdateCollar;

import java.util.UUID;

public class CollarDyeScreen extends Screen {
    private final ItemStack is;
    private final boolean shouldPaw;
    private final int initColor, initPaw;
    private final UUID ownUUID;
    private OwnerComponent owner;

    public CollarDyeScreen(ItemStack is, UUID plr) {
        super(is.getHoverName());
        this.is = is;
        this.ownUUID = plr;
        initColor = CollarItem.getColor(is);
        initPaw = CollarItem.getPawColor(is);
        owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        shouldPaw = is.getItem() instanceof CollarItem ci && !ci.tagless;
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        EditBox dyeField = new EditBox(this.font, x - 30, shouldPaw ? y : y + 25, 100, 20, Component.empty());
        dyeField.setMaxLength(6);
        dyeField.setResponder((s) -> updateTextField(false, s));
        dyeField.setValue(Integer.toHexString(initColor));
        this.addRenderableWidget(dyeField);

        if (shouldPaw) {
            EditBox pawField = new EditBox(this.font, x - 30, y + 25, 100, 20, Component.empty());
            pawField.setMaxLength(6);
            pawField.setResponder((s) -> updateTextField(true, s));
            pawField.setValue(Integer.toHexString(initPaw));
            this.addRenderableWidget(pawField);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            PacketUpdateCollar.OwnerState os = owner == null ? PacketUpdateCollar.OwnerState.DEL : (owner.uuid().equals(ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            ClientPlayNetworking.send(new PacketUpdateCollar(is, os));
            onClose();
        }).bounds(x + 5, y + 50, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (btn) -> {
            is.set(DataComponents.DYED_COLOR, new DyedItemColor(initColor));
            is.set(DataComponents.MAP_COLOR, new MapItemColor(initPaw));
            onClose();
        }).bounds(x - 80, y + 50, 75, 20).build());

        Button ownerButton = Button.builder(Component.empty(), this::updateOwner).bounds(x - 80, y + 72, 160, 20).build();
        if (owner == null) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        } else if (owner.uuid().equals(ownUUID) && owner.owned().isEmpty()) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.owner", owner.name()));
            ownerButton.active = false;
        }
        this.addRenderableWidget(ownerButton);
    }

    private void updateOwner(Button btn) {
        if (owner == null) {
            owner = new OwnerComponent(ownUUID, Minecraft.getInstance().getUser().getName());
            btn.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            owner = null;
            btn.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        }
    }

    private void updateTextField(boolean paw, String s) {
        int col;
        try {
            col = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return;
        }
        if (paw) {
            is.set(DataComponents.MAP_COLOR, new MapItemColor(col));
        } else {
            is.set(DataComponents.DYED_COLOR, new DyedItemColor(col));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(font, Component.translatable("item.playercollars.collar"), this.width / 2 - 75, this.height / 2 + (shouldPaw ? -25 : 1), -1, true);
        if (shouldPaw)
            context.text(font, Component.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
