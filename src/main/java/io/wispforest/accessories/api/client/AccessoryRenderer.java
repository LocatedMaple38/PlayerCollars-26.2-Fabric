package io.wispforest.accessories.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public interface AccessoryRenderer {
    <S extends LivingEntityRenderState> void render(ItemStack stack, SlotReference reference, PoseStack matrices,
                                                    EntityModel<S> entityModel, S renderState,
                                                    MultiBufferSource multiBufferSource, int light, float partialTicks);

    default <S extends LivingEntityRenderState> void renderOnFirstPerson(HumanoidArm arm, ItemStack stack,
                                                                         SlotReference reference, PoseStack matrices,
                                                                         EntityModel<S> entityModel, S renderState,
                                                                         MultiBufferSource multiBufferSource, int light,
                                                                         float partialTicks) {
    }

    static void transformToFace(PoseStack matrices, ModelPart part, Side side) {
        part.translateAndRotate(matrices);
    }
}
