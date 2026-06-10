package org.jlortiz.playercollars.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;

public class PawsConfigScreen<T extends ItemLike> extends AbstractContainerScreen<PawsConfigScreenHandler<T>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller.png");
    private static final Identifier WIDGETS_TEXTURE = Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller_widgets.png");
    private TagLikeListWidget<T> listWidget;
    private ItemStack stack;

    public PawsConfigScreen(PawsConfigScreenHandler<T> screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text, 224, 222);
        stack = ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        inventoryLabelY = imageHeight - 94;
        super.init();
        listWidget = addRenderableWidget(new TagLikeListWidget<>(160, 106, leftPos + 7, topPos + 18,
                minecraft.font.lineHeight, menu.getRegistryKey(), this::handleButtonClick));
        listWidget.setList(menu.listToDisplay);
    }

    private void handleButtonClick(int id) {
        // Call the local function first to prevent a race where the list could be cleared before we update the payload.
        menu.clickMenuButton(minecraft.player, id);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        menu.getSlot(0).setByPlayer(ItemStack.EMPTY);
        if (stack.isEmpty())
            listWidget.setList(menu.listToDisplay);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!ItemStack.matches(menu.getSlot(0).getItem(), stack)) {
            stack = menu.getSlot(0).getItem();
            listWidget.setList(menu.listToDisplay);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = leftPos;
        int y = topPos;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, x + 7, y + 108, stack.isEmpty() ? 16 : 0, 0, 16, 16, 32, 16);
    }
}
