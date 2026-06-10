package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class CollarRenderer implements TrinketRenderer {

    private static void submitItem(ItemStack stack, LivingEntity entity, PoseStack matrixStack, SubmitNodeCollector collector, int light) {
        ItemStack renderStack = stack.copy();
        renderStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        renderStack.remove(DataComponents.ENCHANTMENTS);

        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, renderStack, ItemDisplayContext.HEAD, entity);
        renderState.submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    public void submit(ItemStack itemStack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> entityModel, PoseStack matrixStack,
                       SubmitNodeCollector collector, int light, LivingEntityRenderState renderState, float tickProgress, float partialTick) {
        if (!(entityModel instanceof PlayerModel model)) return;
        LivingEntity entity = slot.inventory().getAttachment().getEntity();

        matrixStack.pushPose();
        try {
            ModelPart body = model.body;
            boolean hasChestplate = entity.getItemBySlot(EquipmentSlot.CHEST).is(ItemTags.CHEST_ARMOR);
            matrixStack.translate(body.x * 0.0625f, body.y * 0.0625f, body.z * 0.0625f);
            matrixStack.mulPose(new Quaternionf().rotateXYZ(body.xRot, body.yRot, body.zRot + (float) Math.PI));
            matrixStack.scale((hasChestplate ? 0.7f : 0.85f) * body.xScale, 0.85f * body.yScale, (hasChestplate ? 1.1f : 0.85f) * body.zScale);
            matrixStack.translate(0, hasChestplate ? 0.475 : 0.4125, -0.005);
            submitItem(itemStack, entity, matrixStack, collector, light);
        } finally {
            matrixStack.popPose();
        }
    }
}
