package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class PawRenderer implements TrinketRenderer {
    private static final float PAW_OUTWARD_OFFSET_X = 0.05f;
    private static final float PAW_LIMB_OFFSET_Y = 0.08f;
    private static final float PAW_FLIP_ROTATION = (float) Math.PI;
    private static final float PAW_OFFSET_Y = -0.1875f;
    private static final float PAW_OFFSET_Z = -0.125f;
    private static final float PAW_SCALE_X = 0.47f;
    private static final float PAW_SCALE_Y = 0.40f;
    private static final float PAW_SCALE_Z = 0.62f;
    private static final float SLIM_PAW_SCALE_Z = 0.55f;
    private static final float FIRST_PERSON_PAW_SCALE_X = 0.40f;
    private static final float FIRST_PERSON_PAW_SCALE_Y = 0.40f;
    private static final float FIRST_PERSON_PAW_SCALE_Z = 0.55f;
    private static final float FIRST_PERSON_SLIM_PAW_SCALE_X = 0.34f;

    private static void submitItem(ItemStack stack, LivingEntity entity, PoseStack matrices, SubmitNodeCollector collector, int light) {
        ItemStack renderStack = stack.copy();
        renderStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        renderStack.remove(DataComponents.ENCHANTMENTS);

        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, renderStack, ItemDisplayContext.FIXED, entity);
        renderState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    private static void renderForArm(ItemStack stack, PoseStack matrices, PlayerModel model, LivingEntity entity, SubmitNodeCollector collector,
                                     int light, HumanoidRenderState renderState, boolean left) {
        matrices.pushPose();
        if (left) {
            TrinketRenderer.translateToLeftArm(matrices, model, renderState);
        } else {
            TrinketRenderer.translateToRightArm(matrices, model, renderState);
        }
        matrices.translate(left ? PAW_OUTWARD_OFFSET_X : -PAW_OUTWARD_OFFSET_X, PAW_LIMB_OFFSET_Y, 0);
        matrices.mulPose(new Quaternionf().rotateXYZ((float) Math.PI, (float) (left ? Math.PI : -Math.PI)/ 2, 0));
        matrices.mulPose(new Quaternionf().rotateZ(PAW_FLIP_ROTATION));
        matrices.translate(0, PAW_OFFSET_Y, PAW_OFFSET_Z);
        matrices.scale(PAW_SCALE_X, PAW_SCALE_Y, model.slim ? SLIM_PAW_SCALE_Z : PAW_SCALE_Z);
        submitItem(stack, entity, matrices, collector, light);
        matrices.popPose();
    }

    @Override
    public void submitFirstPersonRightArm(ItemStack stack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> entityModel,
                                          ModelPart arm, PoseStack matrices, SubmitNodeCollector collector, int light, LocalPlayer player, boolean sleeveVisible) {
        if (!(entityModel instanceof PlayerModel model)) return;
        matrices.pushPose();
        TrinketRenderer.translateToFirstPersonArm(matrices, arm, HumanoidArm.RIGHT);
        matrices.mulPose(new Quaternionf().rotateXYZ((float) Math.PI, 0, 0));
        matrices.mulPose(new Quaternionf().rotateZ(PAW_FLIP_ROTATION));
        matrices.translate(0.015625, -0.1875, -0.14);
        matrices.scale(model.slim ? FIRST_PERSON_SLIM_PAW_SCALE_X : FIRST_PERSON_PAW_SCALE_X, FIRST_PERSON_PAW_SCALE_Y, FIRST_PERSON_PAW_SCALE_Z);
        submitItem(stack, player, matrices, collector, light);
        matrices.popPose();
    }

    @Override
    public void submitFirstPersonLeftArm(ItemStack stack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> entityModel,
                                         ModelPart arm, PoseStack matrices, SubmitNodeCollector collector, int light, LocalPlayer player, boolean sleeveVisible) {
        if (!(entityModel instanceof PlayerModel model)) return;
        matrices.pushPose();
        TrinketRenderer.translateToFirstPersonArm(matrices, arm, HumanoidArm.LEFT);
        matrices.mulPose(new Quaternionf().rotateXYZ((float) Math.PI, 0, 0));
        matrices.mulPose(new Quaternionf().rotateZ(PAW_FLIP_ROTATION));
        matrices.translate(0, -0.1875, -0.135);
        matrices.scale(model.slim ? FIRST_PERSON_SLIM_PAW_SCALE_X : FIRST_PERSON_PAW_SCALE_X, FIRST_PERSON_PAW_SCALE_Y, FIRST_PERSON_PAW_SCALE_Z);
        submitItem(stack, player, matrices, collector, light);
        matrices.popPose();
    }

    @Override
    public void submit(ItemStack stack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> entityModel, PoseStack matrices,
                       SubmitNodeCollector collector, int light, LivingEntityRenderState renderState, float tickProgress, float partialTick) {
        if (!(entityModel instanceof PlayerModel model)) return;
        if (!(renderState instanceof HumanoidRenderState humanoidRenderState)) return;

        LivingEntity entity = slot.inventory().getAttachment().getEntity();
        renderForArm(stack, matrices, model, entity, collector, light, humanoidRenderState, false);
        renderForArm(stack, matrices, model, entity, collector, light, humanoidRenderState, true);
    }
}
