package net.grotacam.unpunchabletree.item;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.grotacam.unpunchabletree.entity.ThrownHatchet;
import net.grotacam.unpunchabletree.rules.HatchetRules;
import net.grotacam.unpunchabletree.rules.ServerRules;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * A regular axe that can also be thrown like a trident: hold right click, release to throw.
 * A throw that hits an entity costs extra durability; the hatchet sticks into blocks otherwise.
 * Besides the usual axe enchantments, it accepts Loyalty, and no other trident enchantment.
 */
public class FlintHatchetItem extends AxeItem {
	/** Total melee damage, including the player's base damage of 1. */
	public static final float ATTACK_DAMAGE = 5.0F;
	/** Total attack speed, including the player's base attack speed of 4. */
	public static final float ATTACK_SPEED = 1.4F;
	/** A throw hits harder than a melee attack, but less than a thrown trident (8). */
	public static final float THROWN_DAMAGE = 7.0F;

	private static final float PLAYER_BASE_ATTACK_DAMAGE = 1.0F;
	private static final float PLAYER_BASE_ATTACK_SPEED = 4.0F;
	/** Same minimum charge time as the trident. */
	private static final int MIN_CHARGE_TICKS = 10;
	private static final float THROW_SPEED = 2.0F;
	private static final float THROW_INACCURACY = 1.0F;

	/**
	 * The "arm raised" pose of the trident. Minecraft 1.21.11 renamed it from SPEAR to TRIDENT
	 * (SPEAR now being the new spears' pose), so it is looked up by name to support both.
	 */
	private static final ItemUseAnimation THROW_ANIMATION = findThrowAnimation();

	public FlintHatchetItem(ToolMaterial material, Properties properties) {
		super(material, ATTACK_DAMAGE - PLAYER_BASE_ATTACK_DAMAGE, ATTACK_SPEED - PLAYER_BASE_ATTACK_SPEED, properties);
	}

	private static ItemUseAnimation findThrowAnimation() {
		try {
			return ItemUseAnimation.valueOf("TRIDENT");
		} catch (IllegalArgumentException e) {
			return ItemUseAnimation.valueOf("SPEAR");
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		HatchetRules rules = ServerRules.forLevel(level).hatchet();
		if (!rules.throwingAllowed()) {
			return InteractionResult.PASS;
		}
		// Passing lets the shield (or any blocking item) in the off hand be used instead.
		if (rules.shieldOverThrowing() && hand == InteractionHand.MAIN_HAND
				&& player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)) {
			return InteractionResult.PASS;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return THROW_ANIMATION;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof Player player)) {
			return false;
		}
		if (getUseDuration(stack, user) - remainingUseTicks < MIN_CHARGE_TICKS) {
			return false;
		}

		if (level instanceof ServerLevel serverLevel) {
			ThrownHatchet hatchet = Projectile.spawnProjectileFromRotation(
					ThrownHatchet::new, serverLevel, stack, player, 0.0F, THROW_SPEED, THROW_INACCURACY);
			if (player.hasInfiniteMaterials()) {
				hatchet.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			} else {
				player.getInventory().removeItem(stack);
			}
			level.playSound(null, hatchet, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return true;
	}

	/**
	 * Fabric hook for the enchantments this item accepts, in anvils and commands (ACCEPTABLE)
	 * and in the enchanting table (PRIMARY). Adds Loyalty to the regular axe enchantments, as long
	 * as the hatchet can be thrown.
	 */
	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		if (enchantment.is(Enchantments.LOYALTY)) {
			return ServerRules.current().hatchet().throwingAllowed();
		}
		return super.canBeEnchantedWith(stack, enchantment, context);
	}
}
