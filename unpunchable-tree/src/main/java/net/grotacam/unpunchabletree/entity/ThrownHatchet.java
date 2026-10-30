package net.grotacam.unpunchabletree.entity;

import java.util.Collection;
import java.util.List;
import net.grotacam.unpunchabletree.item.FlintHatchetItem;
import net.grotacam.unpunchabletree.registry.ModDamageTypes;
import net.grotacam.unpunchabletree.registry.ModEntityTypes;
import net.grotacam.unpunchabletree.registry.ModItems;
import net.grotacam.unpunchabletree.rules.ServerRules;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A thrown Flint Hatchet. Built on {@link AbstractArrow} like the trident, which gives it gravity,
 * block sticking and pickup for free.
 * <ul>
 *   <li>Hitting an entity deals {@link FlintHatchetItem#THROWN_DAMAGE} (plus damage enchantments)
 *   and costs the configured durability; the hatchet breaks if it runs out, otherwise it drops
 *   next to the target.</li>
 *   <li>Hitting a block makes it stick, and it can be picked up again.</li>
 *   <li>With Loyalty, it flies back to its owner after hitting something, exactly like a trident.</li>
 * </ul>
 */
public class ThrownHatchet extends AbstractArrow {
	private static final EntityDataAccessor<Byte> ID_LOYALTY =
			SynchedEntityData.defineId(ThrownHatchet.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> ID_FOIL =
			SynchedEntityData.defineId(ThrownHatchet.class, EntityDataSerializers.BOOLEAN);
	/** Like the trident, Loyalty kicks in after this many ticks stuck in a block. */
	private static final int LOYALTY_DELAY_IN_GROUND = 4;

	/** Set once it hit an entity: it can't hit another one, and Loyalty brings it back. */
	private boolean dealtDamage;
	private int ticksInGround;
	private int returnSoundTicks;

	public ThrownHatchet(EntityType<? extends ThrownHatchet> type, Level level) {
		super(type, level);
	}

	public ThrownHatchet(Level level, LivingEntity owner, ItemStack stack) {
		super(ModEntityTypes.THROWN_HATCHET, owner, level, stack, null);
		this.entityData.set(ID_LOYALTY, loyaltyOf(stack));
		this.entityData.set(ID_FOIL, stack.hasFoil());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ID_LOYALTY, (byte) 0);
		builder.define(ID_FOIL, false);
	}

	/** Same computation as the trident, so the Loyalty level keeps its vanilla meaning. */
	private byte loyaltyOf(ItemStack stack) {
		return level() instanceof ServerLevel level
				? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(level, stack, this), 0, 127)
				: 0;
	}

	/** Whether the thrown item is enchanted, for the client-side glint. */
	public boolean isFoil() {
		return this.entityData.get(ID_FOIL);
	}

	/** Whether the hatchet is stuck in a block (synced to the client). */
	public boolean isStuck() {
		return isInGround();
	}

	@Override
	public void tick() {
		if (isInGround()) {
			ticksInGround++;
		} else {
			ticksInGround = 0;
		}
		if (ticksInGround > LOYALTY_DELAY_IN_GROUND) {
			dealtDamage = true;
		}

		int loyalty = this.entityData.get(ID_LOYALTY);
		Entity owner = getOwner();
		if (loyalty > 0 && (dealtDamage || isNoPhysics()) && owner != null) {
			if (!canReturnTo(owner)) {
				if (level() instanceof ServerLevel level && this.pickup == Pickup.ALLOWED) {
					spawnAtLocation(level, getPickupItem(), 0.1F);
				}
				discard();
			} else {
				returnTo(owner, loyalty);
			}
		}

		super.tick();
	}

	/** Trident Loyalty movement: fly through blocks towards the owner's eyes. */
	private void returnTo(Entity owner, int loyalty) {
		setNoPhysics(true);
		Vec3 toOwner = owner.getEyePosition().subtract(position());
		setPosRaw(getX(), getY() + toOwner.y * 0.015 * loyalty, getZ());
		setDeltaMovement(getDeltaMovement().scale(0.95).add(toOwner.normalize().scale(0.05 * loyalty)));
		if (returnSoundTicks == 0) {
			playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
		}
		returnSoundTicks++;
	}

	private static boolean canReturnTo(Entity owner) {
		return owner.isAlive() && !(owner instanceof ServerPlayer && owner.isSpectator());
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.FLINT_HATCHET);
	}

	/** The thrown stack is also the weapon, so its damage enchantments apply. */
	@Override
	public ItemStack getWeaponItem() {
		return getPickupItemStackOrigin();
	}

	// Both lookups exist because Minecraft 1.21.11 added the second one; once the hatchet hit an
	// entity, it must not hit another on its way down or back.
	@Override
	protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
		return dealtDamage ? null : super.findHitEntity(from, to);
	}

	@Override
	protected Collection<EntityHitResult> findHitEntities(Vec3 from, Vec3 to) {
		return dealtDamage ? List.of() : super.findHitEntities(from, to);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		dealtDamage = true;
		// Bounce off the target and fall, like a trident.
		setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));

		if (!(level() instanceof ServerLevel level)) {
			return;
		}

		Entity target = result.getEntity();
		Entity owner = getOwner();
		ItemStack weapon = getWeaponItem();

		Holder<DamageType> damageType =
				level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.THROWN_HATCHET);
		DamageSource source = new DamageSource(damageType, this, owner == null ? this : owner);
		float damage = EnchantmentHelper.modifyDamage(level, weapon, target, source, FlintHatchetItem.THROWN_DAMAGE);

		if (!target.hurtServer(level, source, damage)) {
			// Blocked or immune: no damage, no durability lost.
			return;
		}

		EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);
		if (target instanceof LivingEntity living) {
			doKnockback(living, source);
			doPostHurtEffects(living);
		}
		playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.2F);

		// Unbreaking applies; players in creative mode don't lose durability.
		ItemStack beforeDamage = weapon.copy();
		int cost = ServerRules.forLevel(level).hatchet().thrownDurabilityCost();
		weapon.hurtAndBreak(cost, level, owner instanceof ServerPlayer player ? player : null,
				item -> shatter(level, beforeDamage, owner));
	}

	private void shatter(ServerLevel level, ItemStack brokenStack, Entity owner) {
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, brokenStack),
				getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.05);

		Holder<SoundEvent> breakSound = brokenStack.get(DataComponents.BREAK_SOUND);
		if (breakSound != null) {
			level.playSound(null, getX(), getY(), getZ(), breakSound, getSoundSource(),
					0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
		}

		if (owner instanceof Player player) {
			player.awardStat(Stats.ITEM_BROKEN.get(brokenStack.getItem()));
		}
		discard();
	}

	/** A returning hatchet can be caught in mid-air by its owner. */
	@Override
	protected boolean tryPickup(Player player) {
		return super.tryPickup(player)
				|| isNoPhysics() && player == getOwner() && player.getInventory().add(getPickupItem());
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.TRIDENT_HIT_GROUND;
	}

	/** Like the trident: a hatchet thrown by a survival player never despawns. */
	@Override
	protected void tickDespawn() {
		if (this.pickup != Pickup.ALLOWED) {
			super.tickDespawn();
		}
	}
}
