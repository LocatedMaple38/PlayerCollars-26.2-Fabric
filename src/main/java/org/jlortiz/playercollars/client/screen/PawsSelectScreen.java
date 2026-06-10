package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jlortiz.playercollars.network.PacketOpenPawsConfig;

import java.util.UUID;

public class PawsSelectScreen extends Screen {
    private final UUID plr;

    public PawsSelectScreen(Entity plr) {
        super(Component.translatable("gui.playercollars.paw_configurator.title", plr.getName()));
        this.plr = plr.getUUID();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(minecraft.font, title, this.width / 2, this.height / 2 - 20, -1);
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.playercollars.paw_configurator.block.open"), (btn) -> {
            ClientPlayNetworking.send(new PacketOpenPawsConfig(plr, false));
            onClose();
        }).bounds(x - 80, y, 160, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.playercollars.paw_configurator.item.open"), (btn) -> {
            ClientPlayNetworking.send(new PacketOpenPawsConfig(plr, true));
            onClose();
        }).bounds(x - 80, y + 22, 160, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
