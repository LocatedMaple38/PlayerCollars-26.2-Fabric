package org.jlortiz.playercollars;

import net.minecraft.world.item.ItemStack;

public record PetControlOptions(
        SpeechMode speechMode,
        boolean commandsBlocked,
        boolean visionObscured,
        boolean movementRestrained
) {
    public static final PetControlOptions DEFAULT = new PetControlOptions(SpeechMode.ALLOWED, false, false, false);

    public static PetControlOptions fromCollar(ItemStack collar) {
        if (collar == null || collar.isEmpty()) return DEFAULT;
        return new PetControlOptions(
                collar.getOrDefault(PlayerCollarsMod.SPEECH_MODE_COMPONENT_TYPE, SpeechMode.ALLOWED),
                collar.getOrDefault(PlayerCollarsMod.COMMANDS_BLOCKED_COMPONENT_TYPE, false),
                collar.getOrDefault(PlayerCollarsMod.VISION_OBSCURED_COMPONENT_TYPE, false),
                collar.getOrDefault(PlayerCollarsMod.MOVEMENT_RESTRAINED_COMPONENT_TYPE, false)
        );
    }
}
