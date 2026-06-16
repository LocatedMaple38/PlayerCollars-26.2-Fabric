package org.jlortiz.playercollars;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum SpeechMode implements StringRepresentable {
    ALLOWED("allowed"),
    MUFFLED("muffled"),
    SILENCED("silenced");

    public static final Codec<SpeechMode> CODEC = StringRepresentable.fromEnum(SpeechMode::values);

    private final String serializedName;

    SpeechMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public SpeechMode next() {
        return switch (this) {
            case ALLOWED -> MUFFLED;
            case MUFFLED -> SILENCED;
            case SILENCED -> ALLOWED;
        };
    }

    public static SpeechMode byId(int id) {
        SpeechMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : ALLOWED;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
