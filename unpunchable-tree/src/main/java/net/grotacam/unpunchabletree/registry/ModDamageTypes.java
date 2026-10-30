package net.grotacam.unpunchabletree.registry;

import net.grotacam.unpunchabletree.UnpunchableTree;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

/** Damage types are data driven, see {@code data/unpunchabletree/damage_type/}. */
public final class ModDamageTypes {
	public static final ResourceKey<DamageType> THROWN_HATCHET =
			ResourceKey.create(Registries.DAMAGE_TYPE, UnpunchableTree.id("thrown_hatchet"));

	private ModDamageTypes() {
	}
}
