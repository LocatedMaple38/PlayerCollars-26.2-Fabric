package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.LaserPointerItem;
import org.joml.Matrix4f;

public class LaserRenderer {

    public static void register() {
        // This event runs every single frame right before the screen is finished drawing!
        LevelRenderEvents.END_MAIN.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null) return;

            // Loop through EVERY player the client can see!
            for (net.minecraft.client.player.AbstractClientPlayer player : client.level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                ItemStack laser = mainHand.is(PlayerCollarsMod.LASER_POINTER_ITEM) ? mainHand : (offHand.is(PlayerCollarsMod.LASER_POINTER_ITEM) ? offHand : null);

                if (laser != null) {
                    int reachLevel = 0;
                    var reg = client.level.registryAccess().lookup(Registries.ENCHANTMENT);
                    if (reg.isPresent()) {
                        var entry = reg.get().get(LaserPointerItem.LASER_REACH_KEY);
                        if (entry.isPresent()) {
                            reachLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(entry.get(), laser);
                        }
                    }

                    double maxDistance = 32.0 * (1.0 + reachLevel);
                    HitResult hit = player == client.player && client.hitResult != null
                            ? client.hitResult
                            : player.pick(maxDistance, client.getDeltaTracker().getGameTimeDeltaPartialTick(true), false);

                    // It only renders if it successfully hits a block within range!
                    if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit
                            && hit.getLocation().distanceToSqr(player.getEyePosition()) <= maxDistance * maxDistance) {
                        drawLaserSquare(context, blockHit);
                    }
                }
            }
        });
    }

    private static void drawLaserSquare(LevelRenderContext context, BlockHitResult hit) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 hitPos = hit.getLocation();
        Direction side = hit.getDirection();

        PoseStack matrices = context.poseStack();
        matrices.pushPose();

        // 1. Move to the exact spot we hit, relative to the camera
        matrices.translate(hitPos.x - cameraPos.x, hitPos.y - cameraPos.y, hitPos.z - cameraPos.z);

        // 2. Push the square out from the block by a tiny fraction so it doesn't clip into the wall (Z-fighting)
        matrices.translate(side.getStepX() * 0.015f, side.getStepY() * 0.015f, side.getStepZ() * 0.015f);

        // 3. Rotate the matrix so our square lies perfectly flat against the block face!
        matrices.mulPose(side.getRotation());

        Matrix4f positionMatrix = matrices.last().pose();
        VertexConsumer buffer = context.bufferSource().getBuffer(RenderTypes.debugQuads());

        // The size of your laser dot!
        float size = 0.05f;

        // 4. Draw the 4 corners of our bright green square! (R, G, B, Alpha)
        buffer.addVertex(positionMatrix, -size, -size, 0).setColor(0, 255, 0, 255);
        buffer.addVertex(positionMatrix, -size, size, 0).setColor(0, 255, 0, 255);
        buffer.addVertex(positionMatrix, size, size, 0).setColor(0, 255, 0, 255);
        buffer.addVertex(positionMatrix, size, -size, 0).setColor(0, 255, 0, 255);

        matrices.popPose();
    }
}
