package org.jlortiz.playercollars.client;


import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class FootPawRenderer implements TrinketRenderer {
    private static final float FOOT_PAW_INWARD_OFFSET_X = 0.0f;
    private static final float FOOT_PAW_RIGHT_OFFSET_X = -0.015f;
    private static final float FOOT_PAW_LIMB_OFFSET_Y = 0.06f;
    private static final float FOOT_PAW_FLIP_ROTATION = (float) Math.PI;
    private static final float FOOT_PAW_HEEL_OFFSET_Z = -0.015f;
    private static final float FOOT_PAW_MODEL_OFFSET_Z = 0.10f;
    private static final float FOOT_PAW_SCALE = 0.41f;

    private static void submitItem(ItemStack stack, LivingEntity entity, PoseStack matrices, SubmitNodeCollector collector, int light) {
        ItemStack renderStack = stack.copy();
        renderStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        renderStack.remove(DataComponents.ENCHANTMENTS);

        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, renderStack, ItemDisplayContext.FIXED, entity);
        renderState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    private static void renderForLeg(ItemStack stack, PoseStack matrices, PlayerModel model, LivingEntity entity, SubmitNodeCollector collector,
                                     int light, HumanoidRenderState renderState, boolean left) {
        matrices.pushPose();
        if (left) {
            TrinketRenderer.translateToLeftLeg(matrices, model, renderState);
        } else {
            TrinketRenderer.translateToRightLeg(matrices, model, renderState);
        }
        matrices.translate(FOOT_PAW_RIGHT_OFFSET_X + (left ? -FOOT_PAW_INWARD_OFFSET_X : FOOT_PAW_INWARD_OFFSET_X), FOOT_PAW_LIMB_OFFSET_Y, FOOT_PAW_HEEL_OFFSET_Z);
        matrices.rotateAround(new Quaternionf().rotateXYZ((float) -Math.PI / 2, 0, 0), 0, 0, 0);
        matrices.mulPose(new Quaternionf().rotateX(FOOT_PAW_FLIP_ROTATION));
        matrices.translate(0, 0, FOOT_PAW_MODEL_OFFSET_Z);
        matrices.scale(FOOT_PAW_SCALE, FOOT_PAW_SCALE, FOOT_PAW_SCALE);
        submitItem(stack, entity, matrices, collector, light);
        matrices.popPose();
    }

    @Override
    public void submit(ItemStack itemStack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> entityModel, PoseStack matrixStack,
                       SubmitNodeCollector collector, int light, LivingEntityRenderState renderState, float tickProgress, float partialTick) {
        if (!(entityModel instanceof PlayerModel model)) return;
        if (!(renderState instanceof HumanoidRenderState humanoidRenderState)) return;

        LivingEntity entity = slot.inventory().getAttachment().getEntity();
        renderForLeg(itemStack, matrixStack, model, entity, collector, light, humanoidRenderState, false);
        renderForLeg(itemStack, matrixStack, model, entity, collector, light, humanoidRenderState, true);
    }
}
