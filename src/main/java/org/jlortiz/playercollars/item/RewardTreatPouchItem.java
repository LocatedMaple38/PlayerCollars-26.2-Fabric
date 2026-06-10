package org.jlortiz.playercollars.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class RewardTreatPouchItem extends Item {
    public RewardTreatPouchItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!user.level().isClientSide() && entity instanceof Player pet) {
            // Make sure the person giving the treat is the actual owner!
            ItemStack collar = EquippedTrinkets.findOwned(pet, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), user.getUUID(), pet.getUUID());

            if (collar != null) {
                // 馃惥 Give the pet 3 full bars of hunger (6 points) and a little saturation!
                pet.getFoodData().eat(6, 0.6f);

                // Give them a little pop of XP!
                pet.giveExperiencePoints(5);

                // 鈿?Give them the "Zoomies" (Speed and Jump Boost) for 10 seconds (200 ticks)!
                pet.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
                pet.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 1));

                // 馃挅 Happy sparkle particles and an adorable eating sound!
                ((ServerLevel) user.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, pet.getX(), pet.getY() + 1.0, pet.getZ(), 7, 0.3, 0.3, 0.3, 0.0);
                user.level().playSound(null, pet.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 1.2f);

                // Consume a treat from the pouch (damages the item by 1)
                stack.hurtAndBreak(1, user, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

                return InteractionResult.SUCCESS;
            } else {
                user.sendOverlayMessage(Component.literal("You can only give treats to your own pet!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
}
