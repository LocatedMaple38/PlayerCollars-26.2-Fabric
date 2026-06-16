package org.jlortiz.playercollars;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ClientHooks {
    private static CollarDyeScreenOpener collarDyeScreenOpener = (stack, playerId) -> {};
    private static DeedScreenOpener deedScreenOpener = (stack, player) -> {};
    private static InvisibleFenceParticleFilter invisibleFenceParticleFilter = () -> false;

    private ClientHooks() {
    }

    public static void setCollarDyeScreenOpener(CollarDyeScreenOpener opener) {
        collarDyeScreenOpener = opener;
    }

    public static void openCollarDyeScreen(ItemStack stack, UUID playerId) {
        collarDyeScreenOpener.open(stack, playerId);
    }

    public static void setDeedScreenOpener(DeedScreenOpener opener) {
        deedScreenOpener = opener;
    }

    public static void openDeedScreen(ItemStack stack, Player player) {
        deedScreenOpener.open(stack, player);
    }

    public static void setInvisibleFenceParticleFilter(InvisibleFenceParticleFilter filter) {
        invisibleFenceParticleFilter = filter;
    }

    public static boolean shouldSuppressInvisibleFenceParticles() {
        return invisibleFenceParticleFilter.shouldSuppress();
    }

    @FunctionalInterface
    public interface CollarDyeScreenOpener {
        void open(ItemStack stack, UUID playerId);
    }

    @FunctionalInterface
    public interface DeedScreenOpener {
        void open(ItemStack stack, Entity player);
    }

    @FunctionalInterface
    public interface InvisibleFenceParticleFilter {
        boolean shouldSuppress();
    }
}
