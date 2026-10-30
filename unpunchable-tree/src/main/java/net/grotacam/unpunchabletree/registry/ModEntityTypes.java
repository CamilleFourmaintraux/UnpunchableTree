package net.grotacam.unpunchabletree.registry;

import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.entity.ThrownHatchet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	/** Same size and network settings as the thrown trident. */
	public static final EntityType<ThrownHatchet> THROWN_HATCHET = register(
			"thrown_hatchet",
			EntityType.Builder.<ThrownHatchet>of(ThrownHatchet::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.eyeHeight(0.13F)
					.clientTrackingRange(4)
					.updateInterval(20)
	);

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, UnpunchableTree.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void initialize() {
		// Loads the class, which registers the entity types.
	}

	private ModEntityTypes() {
	}
}
