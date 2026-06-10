package org.jlortiz.playercollars.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroomingBrushItem extends Item {
    public GroomingBrushItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!user.level().isClientSide() && entity instanceof Player pet) {
            ((ServerLevel) user.level()).sendParticles(
                    ParticleTypes.HEART,
                    pet.getX(), pet.getY() + 1.0, pet.getZ(),
                    5, 0.3, 0.3, 0.3, 0.0
            );


            user.level().playSound(null, pet.blockPosition(), SoundEvents.WOLF_PANT_BABY.value(), SoundSource.PLAYERS, 1.0f, 1.2f);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
