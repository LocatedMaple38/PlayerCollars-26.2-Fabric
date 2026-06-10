package org.jlortiz.playercollars.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jlortiz.playercollars.EquippedTrinkets;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

import java.util.List;
import java.util.Optional;

public class LaserPointerItem extends Item {
    // 馃挅 We declare the RegistryKey for our new custom enchantment!
    public static final ResourceKey<Enchantment> LASER_REACH_KEY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "laser_reach"));

    public LaserPointerItem(Properties settings) {
        // We append the EnchantableComponent so the table accepts it!
        // 15 is the enchantability value (similar to iron tools!)
        super(settings.component(DataComponents.ENCHANTABLE, new Enchantable(15)));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            ItemStack stack = user.getItemInHand(hand);

            // Calculate enchanted distance (Base 32 blocks, +32 per level)
            int reachLevel = 0;
            Optional<Holder.Reference<Enchantment>> enchantEntry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(LASER_REACH_KEY);
            if (enchantEntry.isPresent()) {
                reachLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantEntry.get(), stack);
            }
            double maxDistance = 32.0 * (1.0 + reachLevel);

// Cast the ray!
            HitResult hit = user.pick(maxDistance, 0.0F, false);

            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
                Vec3 targetPos = hit.getLocation();

                // Get ALL players in the world! (Infinite distance)
                List<ServerPlayer> plrs = ((ServerLevel) world).getPlayers(p -> !p.is(user));
                PacketLookAtLerped packet = new PacketLookAtLerped(targetPos.x, targetPos.y, targetPos.z);

                for (ServerPlayer p : plrs) {
                    ItemStack collar = EquippedTrinkets.findOwned(p, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), user.getUUID(), p.getUUID());
                    if (collar != null) {
                        ServerPlayNetworking.send(p, packet);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
