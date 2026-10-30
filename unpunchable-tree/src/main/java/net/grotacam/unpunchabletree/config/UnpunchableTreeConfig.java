package net.grotacam.unpunchabletree.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.rules.HarvestRules;
import net.grotacam.unpunchabletree.rules.HatchetRules;
import net.grotacam.unpunchabletree.rules.ServerRules;

/** Stored in {@code config/unpunchabletree.json}. */
public final class UnpunchableTreeConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(UnpunchableTree.MOD_ID + ".json");

	public static final boolean DEFAULT_PUNCHABLE_TREES = false;
	public static final boolean DEFAULT_PUNCHABLE_GROUND = true;
	public static final boolean DEFAULT_HATCHET_THROWING = true;
	public static final boolean DEFAULT_SHIELD_OVER_THROWING = true;
	public static final int DEFAULT_THROWN_HATCHET_DURABILITY_COST = 3;

	/** Logs and wood can be harvested without an axe. */
	public boolean punchableTrees = DEFAULT_PUNCHABLE_TREES;
	/** Dirt, sand, gravel and other shovel blocks can be harvested without a shovel. */
	public boolean punchableGround = DEFAULT_PUNCHABLE_GROUND;
	/** The Flint Hatchet can be thrown. The options below only matter when this is true. */
	public boolean hatchetThrowing = DEFAULT_HATCHET_THROWING;
	/** With a shield in the off hand, right click raises the shield instead of throwing. */
	public boolean shieldOverThrowing = DEFAULT_SHIELD_OVER_THROWING;
	/** Durability a thrown hatchet loses when it hits an entity. */
	public int thrownHatchetDurabilityCost = DEFAULT_THROWN_HATCHET_DURABILITY_COST;

	private static UnpunchableTreeConfig instance = new UnpunchableTreeConfig();
	private static volatile ServerRules rules = instance.toRules();

	private UnpunchableTreeConfig() {
	}

	public static UnpunchableTreeConfig get() {
		return instance;
	}

	/** The rules this game enforces when it acts as the server (dedicated, singleplayer or LAN host). */
	public static ServerRules rules() {
		return rules;
	}

	private ServerRules toRules() {
		return new ServerRules(
				new HarvestRules(punchableTrees, punchableGround),
				new HatchetRules(hatchetThrowing, shieldOverThrowing, thrownHatchetDurabilityCost));
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				UnpunchableTreeConfig loaded = GSON.fromJson(reader, UnpunchableTreeConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | JsonParseException e) {
				UnpunchableTree.LOGGER.error("Could not read {}, using default values", PATH, e);
			}
		}
		instance.thrownHatchetDurabilityCost = Math.max(0, instance.thrownHatchetDurabilityCost);
		// Writes the file on first launch and adds options introduced by newer versions.
		save();
	}

	public static void save() {
		rules = instance.toRules();
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			UnpunchableTree.LOGGER.error("Could not write {}", PATH, e);
		}
	}
}
