package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.block.InvisibleFenceBlock;
import org.jlortiz.playercollars.item.*;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.jlortiz.playercollars.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class PlayerCollarsMod implements ModInitializer {
	public static final String MOD_ID = "playercollars";
    public static final CollarItem COLLAR_ITEM = Registry.register(BuiltInRegistries.ITEM, CollarItem.REGISTRY_KEY, new CollarItem(false));
    public static final CollarItem TAGLESS_COLLAR_ITEM = Registry.register(BuiltInRegistries.ITEM, CollarItem.TAGLESS_REGISTRY_KEY, new CollarItem(true));
    public static final ClickerItem CLICKER_ITEM = Registry.register(BuiltInRegistries.ITEM, ClickerItem.REGISTRY_KEY, new ClickerItem());
    public static final DeedItem DEED_OF_OWNERSHIP = Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "deed_of_ownership"), new DeedItem());
    public static final Item DEED_OF_OWNERSHIP_STAMPED = Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(PlayerCollarsMod.MOD_ID, "stamped_deed_of_ownership"), new StampedDeedItem());
    public static final InvisibleFenceBlock INVISIBLE_FENCE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, InvisibleFenceBlock.REGISTRY_KEY,
            new InvisibleFenceBlock(BlockBehaviour.Properties.of().instabreak().sound(SoundType.GLASS).noOcclusion().dynamicShape()));
    public static final BlockItem INVISIBLE_FENCE_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, InvisibleFenceBlock.ITEM_REGISTRY_KEY,
			new BlockItem(INVISIBLE_FENCE_BLOCK, new Item.Properties().setId(InvisibleFenceBlock.ITEM_REGISTRY_KEY)));
    public static final PawSetupItem PAW_CONFIGURATION_ITEM = Registry.register(BuiltInRegistries.ITEM, PawSetupItem.REGISTRY_KEY, new PawSetupItem());
    public static final CollarLockerItem COLLAR_LOCKER_ITEM = Registry.register(BuiltInRegistries.ITEM, CollarLockerItem.REGISTRY_KEY, new CollarLockerItem());
	public static final SpatulaItem SPATULA_ITEM = Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "golden_spatula"), new SpatulaItem());
	public static final GroomingBrushItem GROOMING_BRUSH_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "grooming_brush"),
			new GroomingBrushItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "grooming_brush"))))
	);
	public static final LaserPointerItem LASER_POINTER_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "laser_pointer"),
			new LaserPointerItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "laser_pointer"))))
	);

	public static final SoundEvent CLICKER_ON = Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "clicker_on"),
			SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "clicker_on")));
	public static final SoundEvent CLICKER_OFF = Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "clicker_off"),
			SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "clicker_off")));

	private static final Codec<OwnerComponent> OWNER_COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
			UUIDUtil.AUTHLIB_CODEC.fieldOf("uuid").forGetter(OwnerComponent::uuid),
            Codec.STRING.fieldOf("name").forGetter(OwnerComponent::name),
			ExtraCodecs.optionalEmptyMap(UUIDUtil.AUTHLIB_CODEC).fieldOf("owned").forGetter(OwnerComponent::owned),
			ExtraCodecs.optionalEmptyMap(Codec.STRING).fieldOf("owned_name").forGetter(OwnerComponent::ownedName)
    ).apply(builder, OwnerComponent::new));
	public static final DataComponentType<OwnerComponent> OWNER_COMPONENT_TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "owner_component"),
			DataComponentType.<OwnerComponent>builder().persistent(OWNER_COMPONENT_CODEC).build());
	public static final DataComponentType<Boolean> FORCED_CRAWL_COMPONENT_TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "forced_crawl_component"),
			DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
	public static final DataComponentType<Boolean> DIET_CONTROL_COMPONENT_TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "diet_control_component"),
			DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
	public static final RewardTreatPouchItem REWARD_TREAT_POUCH_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "reward_treat_pouch"),
			new RewardTreatPouchItem(new Item.Properties().durability(64).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "reward_treat_pouch"))))
	);

	private static final Codec<List<Either<TagKey<Block>, ResourceKey<Block>>>> CAN_INTERACT_COMPONENT_CODEC = Codec.withAlternative(
			new ListCodec<>(new EitherCodec<>(TagKey.hashedCodec(Registries.BLOCK), ResourceKey.codec(Registries.BLOCK)), 0, 1024),
			Codec.of(Encoder.error("deprecated"), new ListCodec<>(Identifier.CODEC, 0, 65535).map((x) -> {
				List<Either<TagKey<Block>, ResourceKey<Block>>> ls = new ArrayList<>(x.size());
				for (Identifier id : x) {
					ls.add(Either.right(ResourceKey.create(Registries.BLOCK, id)));
				}
				return ls;
			}))
	);
	public static final DataComponentType<List<Either<TagKey<Block>, ResourceKey<Block>>>> CAN_INTERACT_COMPONENT_TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "can_interact_component"),
			DataComponentType.<List<Either<TagKey<Block>, ResourceKey<Block>>>>builder().persistent(CAN_INTERACT_COMPONENT_CODEC).build());

	private static final Codec<List<Either<TagKey<Item>, ResourceKey<Item>>>> HELD_ITEMS_COMPONENT_CODEC = new ListCodec<>(
			new EitherCodec<>(TagKey.hashedCodec(Registries.ITEM), ResourceKey.codec(Registries.ITEM)), 0, 65535);
	public static final DataComponentType<List<Either<TagKey<Item>, ResourceKey<Item>>>> HELD_ITEMS_COMPONENT_TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "held_items_component"),
			DataComponentType.<List<Either<TagKey<Item>, ResourceKey<Item>>>>builder().persistent(HELD_ITEMS_COMPONENT_CODEC).build());

	public static final Holder<Attribute> ATTR_CLICKER_DISTANCE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath(MOD_ID, "clicker_distance"),
			new RangedAttribute("attribute.playercollars.clicker_distance", 4, 0, 32));
	public static final Holder<Attribute> ATTR_LEASH_DISTANCE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath(MOD_ID, "leash_distance"),
			new RangedAttribute("attribute.playercollars.leash_distance", 4, 2, 16));

	public static final GameRule<Boolean> PLAYER_LEASHES_BREAK_RULE = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.PLAYER)
			.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "player_leashes_break"));
    public static final GameRule<Boolean> LEASHED_PLAYERS_RIDE_ENTITIES = GameRuleBuilder.forBoolean(false)
			.category(GameRuleCategory.PLAYER)
			.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "leashed_players_ride_entities"));
	public static final GameRule<Boolean> ALLOW_ATTACK_OWNER = GameRuleBuilder.forBoolean(false)
			.category(GameRuleCategory.PLAYER)
			.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "player_allow_attack_owner"));
	public static final GameRule<Boolean> ALLOW_UNLEASH_OTHER = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.PLAYER)
			.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "allow_unleash_unowned_player"));

	public static final DogBedBlock[] DOG_BEDS = new DogBedBlock[DyeColor.values().length];
	public static final BedItem[] DOG_BED_ITEMS = new BedItem[DyeColor.values().length];
	public static final TagKey<Item> COLLAR_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "collars"));

	public static final DyeColor[] PAWS_DYE_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY,
			DyeColor.GRAY, DyeColor.BLACK, DyeColor.BLUE, DyeColor.RED, DyeColor.PURPLE};
	public static final PawsItem[] PAWS_ITEMS = new PawsItem[PAWS_DYE_COLORS.length];
	public static final TagKey<Block> PAWS_ALLOW_INTERACT = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "paws_allow_interact"));
	public static final TagKey<Item> PAWS_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "paws"));
	public static final FootPawsItem[] FOOT_PAWS_ITEMS = new FootPawsItem[PAWS_DYE_COLORS.length];
	public static final TagKey<Item> FOOT_PAWS_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "foot_paws"));

	public static final DogBowlBlock[] DOG_BOWLS = new DogBowlBlock[DyeColor.values().length];
	public static final Item[] DOG_BOWL_ITEMS = new Item[DyeColor.values().length];
	public static final BlockEntityType<DogBowlBlock.DogBowlBlockEntity> DOG_BOWL_BLOCK_ENTITY;
	public static final CreativeModeTab GROUP;
	public static final ExtendedMenuType<PawsConfigScreenHandler<Block>, List<Either<TagKey<Block>, ResourceKey<Block>>>> PAWS_BLOCK_CONFIG_SCREEN_HANDLER = new ExtendedMenuType<>(
			PawsConfigScreenHandler.PawsBlockConfigScreenHandler::new, ByteBufCodecs.fromCodec(CAN_INTERACT_COMPONENT_CODEC)
	);
	public static final ExtendedMenuType<PawsConfigScreenHandler<Item>, List<Either<TagKey<Item>, ResourceKey<Item>>>> PAWS_ITEM_CONFIG_SCREEN_HANDLER = new ExtendedMenuType<>(
			PawsConfigScreenHandler.PawsItemConfigScreenHandler::new, ByteBufCodecs.fromCodec(HELD_ITEMS_COMPONENT_CODEC)
	);

    static {
        for (DyeColor c : DyeColor.values()) {
			ResourceKey<Block> blockKey = DogBowlBlock.getRegistryKey(c);
            DOG_BOWLS[c.ordinal()] = Registry.register(BuiltInRegistries.BLOCK, blockKey.identifier(),
                    new DogBowlBlock(c, BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(0.6F).noOcclusion().pushReaction(PushReaction.DESTROY).setId(blockKey)));
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, blockKey.identifier());
            DOG_BOWL_ITEMS[c.ordinal()] = Registry.register(BuiltInRegistries.ITEM, itemKey.identifier(),
                    new BlockItem(DOG_BOWLS[c.ordinal()], new Item.Properties().setId(itemKey)));
        }
        DOG_BOWL_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "dog_bowl"),
				FabricBlockEntityTypeBuilder.create(DogBowlBlock.DogBowlBlockEntity::new, DOG_BOWLS).build()
        );

        GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "group"),
                FabricCreativeModeTab.builder().title(Component.translatable("itemGroup.playercollars"))
                        .icon(COLLAR_ITEM::getDefaultInstance)
                        .displayItems(((displayContext, entries) -> {
                            entries.accept(COLLAR_ITEM);
                            entries.accept(TAGLESS_COLLAR_ITEM);
                            entries.accept(CLICKER_ITEM);
                            entries.accept(COLLAR_LOCKER_ITEM);
                            entries.accept(PAW_CONFIGURATION_ITEM);
                            entries.accept(LASER_POINTER_ITEM);
                            entries.accept(GROOMING_BRUSH_ITEM);
                            entries.accept(REWARD_TREAT_POUCH_ITEM);
                            for (PawsItem p : PAWS_ITEMS)
                                entries.accept(p);
                            for (FootPawsItem p : FOOT_PAWS_ITEMS)
                                entries.accept(p);
                            entries.accept(DEED_OF_OWNERSHIP);
                            entries.accept(SPATULA_ITEM);
                            for (BedItem bed : DOG_BED_ITEMS)
                                entries.accept(bed);
                            for (Item bowl : DOG_BOWL_ITEMS)
                                entries.accept(bowl);
                            entries.accept(INVISIBLE_FENCE_BLOCK_ITEM);
                        })).build());

		Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "paws_block_config"), PAWS_BLOCK_CONFIG_SCREEN_HANDLER);
		Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "paws_item_config"), PAWS_ITEM_CONFIG_SCREEN_HANDLER);
	}

	public static ItemStack filterStacksByOwner(Iterable<SlotEntryReference> stacks, UUID plr, UUID entity) {
		for (SlotEntryReference p : stacks) {
			ItemStack is = p.stack();
			OwnerComponent owner = is.get(OWNER_COMPONENT_TYPE);
			if (owner != null && owner.uuid().equals(plr) &&
					(owner.owned().isEmpty() || owner.owned().get().equals(entity))) {
				return is;
			}
		}
		return null;
	}

	public static InteractionResult pullPlayerTowards(ServerPlayer plr, Vec3 towards, double minDist, double maxDist, UnaryOperator<Double> getFactor) {
		Vec3 vecTo = towards.subtract(plr.position());
		double distance = vecTo.length();
		if (distance < minDist) return InteractionResult.PASS;
		if (distance > maxDist) return InteractionResult.FAIL;

		plr.push(vecTo.scale(Math.abs(getFactor.apply(distance))));
		plr.connection.send(new ClientboundSetEntityMotionPacket(plr));
		// TODO(26.1.2): The old hasImpulse flag no longer exists; the motion packet above keeps the server/client sync.
		return InteractionResult.SUCCESS;
	}

	public static boolean blockLeashKnotBreak(ServerLevel world, Player player, LeashFenceKnotEntity entity) {
		if (entity.equals(((LeashImpl) player).leashplayers$getProxyLeashHolder())) {
			player.sendOverlayMessage(Component.translatable("message.playercollars.no_break_fence").withStyle(ChatFormatting.RED));
			return true;
		}
		if (!world.getGameRules().get(ALLOW_UNLEASH_OTHER)) {
			List<Leashable> list = Leashable.leashableLeashedTo(entity);
			for (Leashable l : list) {
				if (!(l instanceof LeashProxyEntity le)) continue;
				LivingEntity leashTarget = le.getLeashTarget();
				for (ItemStack stack : EquippedTrinkets.getEquipped(leashTarget, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
					OwnerComponent oc = stack.get(OWNER_COMPONENT_TYPE);
					if (oc == null || !oc.owned().orElseGet(leashTarget::getUUID).equals(leashTarget.getUUID())) continue;
					if (!player.getUUID().equals(oc.uuid())) {
						player.sendOverlayMessage(Component.translatable("message.playercollars.no_break_fence_other", le.getLeashTarget().getName()).withStyle(ChatFormatting.RED));
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean attachHeldPlayerLeashesToFence(Player player, Level world, BlockPos pos) {
		if (!(world instanceof ServerLevel serverWorld)) return false;

		LeashFenceKnotEntity knot = null;
		boolean attached = false;
		for (LeashProxyEntity proxy : serverWorld.getEntitiesOfClass(
				LeashProxyEntity.class,
				player.getBoundingBox().inflate(32.0D),
				proxy -> proxy.isAlive() && proxy.getLeashHolder() == player
		)) {
			LivingEntity target = proxy.getLeashTarget();
			ItemStack collar = EquippedTrinkets.findOwned(target, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), player.getUUID(), target.getUUID());
			if (collar == null) continue;

			if (knot == null) {
				knot = LeashFenceKnotEntity.getOrCreateKnot(serverWorld, pos);
			}
			proxy.setLeashedTo(knot, true);
			attached = true;
		}
		return attached;
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "regeneration_effect"), RegenerationEnchantmentEffect.CODEC);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "owner_transfer"), OwnershipCraftingRecipe.Serializer.INSTANCE);
		PayloadTypeRegistry.serverboundPlay().register(PacketUpdateCollar.ID, PacketUpdateCollar.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCollar.ID, PacketUpdateCollar::handle);
		PayloadTypeRegistry.serverboundPlay().register(PacketStampDeed.ID, PacketStampDeed.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketStampDeed.ID, PacketStampDeed::handle);
		PayloadTypeRegistry.serverboundPlay().register(PacketOpenPawsConfig.ID, PacketOpenPawsConfig.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketOpenPawsConfig.ID, PacketOpenPawsConfig::handle);

		PayloadTypeRegistry.clientboundPlay().register(PacketLookAtLerped.ID, PacketLookAtLerped.CODEC);
		for (int i = 0; i < PAWS_DYE_COLORS.length; i++) {
			DyeColor c = PAWS_DYE_COLORS[i];
			ResourceKey<Item> itemKey = PawsItem.getRegistryKey(c);
			PAWS_ITEMS[i] = Registry.register(BuiltInRegistries.ITEM, itemKey,
					new PawsItem(itemKey, c.getFireworkColor(), 0xF196CF));
            itemKey = FootPawsItem.getRegistryKey(c);
			FOOT_PAWS_ITEMS[i] = Registry.register(BuiltInRegistries.ITEM, itemKey,
					new FootPawsItem(itemKey, c.getFireworkColor(), 0xF196CF));
		}

		for (DyeColor c : DyeColor.values()) {
			ResourceKey<Block> blockKey = DogBedBlock.getRegistryKey(c);
			DOG_BEDS[c.ordinal()] = Registry.register(BuiltInRegistries.BLOCK, blockKey, new DogBedBlock(c, blockKey));
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, blockKey.identifier());
			DOG_BED_ITEMS[c.ordinal()] = Registry.register(BuiltInRegistries.ITEM, itemKey,
					new BedItem(DOG_BEDS[c.ordinal()], (new Item.Properties()).stacksTo(1).setId(itemKey)));
		}

		PlayerBlockBreakEvents.BEFORE.register((Level var1, Player player, BlockPos blockPos, BlockState var4, @Nullable BlockEntity var5) -> {
			if (var1.isClientSide()) return true;
			if (player.isSpectator()) return true;
			Entity leashHolderEntity = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
			if (leashHolderEntity instanceof LeashFenceKnotEntity knot && blockPos.equals(knot.getPos())) {
				player.sendOverlayMessage(Component.translatable("message.playercollars.no_break_fence").withStyle(ChatFormatting.RED));
				return false;
			}
			return true;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClientSide() && entity instanceof Player pet) {
				ItemStack stack = player.getItemInHand(hand);

				if (stack.has(DataComponents.FOOD)) {
					// Magic Cooldown: Prevents spamming when holding right-click!
					if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.PASS;

					ItemStack collar = EquippedTrinkets.findOwned(pet, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG), player.getUUID(), pet.getUUID());

					if (collar != null) {
						FoodProperties food = stack.get(DataComponents.FOOD);

						// Check if the pet is actually hungry (or if it's a special food that can always be eaten)
						if (pet.getFoodData().needsFood() || food.canAlwaysEat()) {
							pet.getFoodData().eat(food);

							// Set a 10-tick (half-second) cooldown for the owner
							player.getCooldowns().addCooldown(stack, 10);

							world.playSound(null, pet.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
							((ServerLevel) world).sendParticles(ParticleTypes.HEART, pet.getX(), pet.getY() + 1.0, pet.getZ(), 3, 0.3, 0.3, 0.3, 0.0);

							if (!player.isCreative()) stack.shrink(1);
							return InteractionResult.SUCCESS;
						} else {
							player.sendOverlayMessage(Component.literal("Your pet's tummy is already full!").withStyle(ChatFormatting.GREEN));
							return InteractionResult.FAIL;
						}
					}
				}
			}
			return InteractionResult.PASS;
		});

		AttackEntityCallback.EVENT.register((Player player, Level world, InteractionHand var3, Entity entity, @Nullable EntityHitResult var5) -> {
			if (world.isClientSide()) return InteractionResult.PASS;
			if (player.isSpectator()) return InteractionResult.PASS;

			ServerLevel sworld = (ServerLevel) world;
			for (ItemStack collarStack : EquippedTrinkets.getEquipped(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
				OwnerComponent owner = collarStack.get(OWNER_COMPONENT_TYPE);
				if (owner != null && owner.uuid().equals(entity.getUUID())) {
					// Collared players are allowed to attack owners, but have 75% damage returned to them
					player.sendOverlayMessage(Component.translatable("message.playercollars.no_attack_owner").withStyle(ChatFormatting.RED));

					if (!sworld.getGameRules().get(ALLOW_ATTACK_OWNER)) {
						return InteractionResult.FAIL;
					}

					double f = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
					f = (f - 1) * 0.75 + 1;
					player.hurtServer(sworld, player.damageSources().playerAttack(player), (float) Math.ceil(f));
					return InteractionResult.PASS;
				}
			}

			if (entity instanceof LeashFenceKnotEntity ke && blockLeashKnotBreak(sworld, player, ke)) return InteractionResult.FAIL;
			return InteractionResult.PASS;
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.isEmpty() && PawsItem.hasPaws(player)) {
				if (!world.isClientSide() && stack.has(DataComponents.FOOD)) {
					player.sendOverlayMessage(Component.literal("Paws can't feed themselves by hand.").withStyle(ChatFormatting.RED));
				}
				return InteractionResult.FAIL;
			}

			if (stack.has(DataComponents.FOOD)) {
				for (ItemStack collarStack : EquippedTrinkets.getEquipped(player, (x) -> x.is(PlayerCollarsMod.COLLAR_TAG))) {
					if (collarStack.getOrDefault(PlayerCollarsMod.DIET_CONTROL_COMPONENT_TYPE, false)) {
						if (!world.isClientSide()) {
							player.sendOverlayMessage(Component.literal("You can only eat from your bowl or be hand-fed!").withStyle(ChatFormatting.RED));
						}
						// This firmly stops the item from being used!
						return InteractionResult.FAIL;
					}
				}
			}
			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player.isSpectator()) return InteractionResult.PASS;
			BlockState state = world.getBlockState(hitResult.getBlockPos());
			if (state.is(BlockTags.FENCES) && attachHeldPlayerLeashesToFence(player, world, hitResult.getBlockPos())) {
				return InteractionResult.SUCCESS;
			}
			if (PawsItem.shouldPreventBlockInteraction(player, state)) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}
}
