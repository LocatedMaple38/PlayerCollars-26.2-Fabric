package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketStampDeed;

public class DeedItemScreen extends Screen {
    private final OwnerComponent owner;
    private final Component name;

    public DeedItemScreen(ItemStack is, Entity plr) {
        super(is.getHoverName());
        this.owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        this.name = plr.getName();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("item.playercollars.deed_of_ownership.stamp"), this::stampDeed).bounds(this.width / 2 - 80, this.height / 2 + 72, 160, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (x) -> onClose()).bounds(this.width / 2 - 80, this.height / 2 + 95, 160, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractBlurredBackground(context);
        extractMenuBackground(context);
        // TODO draw background
//        extractBackgroundTexture();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership"), this.width / 2, this.height / 2 - 88, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line1", name, owner.name()), this.width / 2, this.height / 2 - 55, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line2"), this.width / 2, this.height / 2 - 38, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line3"), this.width / 2, this.height / 2 - 26, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line4"), this.width / 2, this.height / 2 - 14, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line5"), this.width / 2, this.height / 2 - 2, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line6"), this.width / 2, this.height / 2 + 10, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line7"), this.width / 2, this.height / 2 + 23, -1);
        context.centeredText(font, Component.translatable("item.playercollars.deed_of_ownership.line8"), this.width / 2, this.height / 2 + 40, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void stampDeed(Button btn) {
        ClientPlayNetworking.send(PacketStampDeed.INSTANCE);
        onClose();
    }
}
