package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jlortiz.playercollars.PetControlOptions;
import org.jlortiz.playercollars.network.PacketOpenPetControl;
import org.jlortiz.playercollars.network.PacketTogglePetControl;

import java.util.UUID;

public class PetControlScreen extends Screen {
    private final UUID petId;
    private String petName;
    private PetControlOptions options;
    private Component titleText;
    private Button speechButton;
    private Button commandsButton;
    private Button visionButton;
    private Button movementButton;

    public PetControlScreen(PacketOpenPetControl payload) {
        super(Component.translatable("gui.playercollars.pet_control.title", payload.petName()));
        this.petId = payload.petId();
        this.petName = payload.petName();
        this.options = payload.options();
        this.titleText = Component.translatable("gui.playercollars.pet_control.title", petName);
    }

    public boolean isFor(UUID petId) {
        return this.petId.equals(petId);
    }

    public void update(PacketOpenPetControl payload) {
        this.petName = payload.petName();
        this.options = payload.options();
        this.titleText = Component.translatable("gui.playercollars.pet_control.title", petName);
        refreshButtons();
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 90;
        int y = this.height / 2 - 48;

        speechButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                ClientPlayNetworking.send(new PacketTogglePetControl(petId, PacketTogglePetControl.Control.SPEECH))
        ).bounds(x, y, 180, 20).build());
        commandsButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                ClientPlayNetworking.send(new PacketTogglePetControl(petId, PacketTogglePetControl.Control.COMMANDS))
        ).bounds(x, y + 24, 180, 20).build());
        visionButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                ClientPlayNetworking.send(new PacketTogglePetControl(petId, PacketTogglePetControl.Control.VISION))
        ).bounds(x, y + 48, 180, 20).build());
        movementButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                ClientPlayNetworking.send(new PacketTogglePetControl(petId, PacketTogglePetControl.Control.MOVEMENT))
        ).bounds(x, y + 72, 180, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + 40, y + 104, 100, 20).build());
        refreshButtons();
    }

    private void refreshButtons() {
        if (speechButton == null) return;

        speechButton.setMessage(Component.translatable(switch (options.speechMode()) {
            case ALLOWED -> "gui.playercollars.pet_control.speech.allowed";
            case MUFFLED -> "gui.playercollars.pet_control.speech.muffled";
            case SILENCED -> "gui.playercollars.pet_control.speech.silenced";
        }));
        commandsButton.setMessage(Component.translatable(options.commandsBlocked()
                ? "gui.playercollars.pet_control.commands.blocked"
                : "gui.playercollars.pet_control.commands.allowed"));
        visionButton.setMessage(Component.translatable(options.visionObscured()
                ? "gui.playercollars.pet_control.vision.obscured"
                : "gui.playercollars.pet_control.vision.normal"));
        movementButton.setMessage(Component.translatable(options.movementRestrained()
                ? "gui.playercollars.pet_control.movement.restrained"
                : "gui.playercollars.pet_control.movement.allowed"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, titleText, this.width / 2, this.height / 2 - 78, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
