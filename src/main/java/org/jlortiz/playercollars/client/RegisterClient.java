package org.jlortiz.playercollars.client;

import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import org.jlortiz.playercollars.ClientHooks;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.CollarDyeScreen;
import org.jlortiz.playercollars.client.screen.DeedItemScreen;
import org.jlortiz.playercollars.client.screen.PetControlScreen;
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.network.PacketOpenPetControl;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientHooks.setCollarDyeScreenOpener((stack, playerId) ->
                Minecraft.getInstance().setScreen(new CollarDyeScreen(stack, playerId)));
        ClientHooks.setDeedScreenOpener((stack, player) ->
                Minecraft.getInstance().setScreen(new DeedItemScreen(stack, player)));
        ClientHooks.setInvisibleFenceParticleFilter(() -> {
            Minecraft client = Minecraft.getInstance();
            return client.player != null && EquippedTrinkets.hasEquipped(client.player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG));
        });

        CollarRenderer collarRenderer = new CollarRenderer();
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, collarRenderer);
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.TAGLESS_COLLAR_ITEM, collarRenderer);
        PawRenderer renderer = new PawRenderer();
        for (FootPawsItem p : PlayerCollarsMod.PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(p, renderer);
        FootPawRenderer renderer2 = new FootPawRenderer();
        for (FootPawsItem p : PlayerCollarsMod.FOOT_PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(p, renderer2);
        ClientPlayNetworking.registerGlobalReceiver(PacketLookAtLerped.ID, (payload, context) ->
                context.client().execute(() -> RotationLerpHandler.beginClickTurn(payload.vec())));
        ClientPlayNetworking.registerGlobalReceiver(PacketOpenPetControl.ID, (payload, context) ->
                context.client().execute(() -> {
                    Minecraft client = context.client();
                    if (client.screen instanceof PetControlScreen screen && screen.isFor(payload.petId())) {
                        screen.update(payload);
                    } else {
                        client.setScreen(new PetControlScreen(payload));
                    }
                }));
        LevelRenderEvents.END_MAIN.register(RotationLerpHandler::turnTowardsClick);
        LaserRenderer.register();
    }
}
