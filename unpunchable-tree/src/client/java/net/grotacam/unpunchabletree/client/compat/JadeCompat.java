package net.grotacam.unpunchabletree.client.compat;

import java.util.ArrayList;
import java.util.List;
import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import snownee.jade.addon.harvest.ToolHandler;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;

/**
 * Jade plugin, only loaded when Jade is installed (entrypoint {@code jade}).
 * <p>
 * Whether a block needs a tool, and the ✔/✘ next to it, already work without this class: they
 * come from the vanilla "requires correct tool" property, which the mod sets on logs.
 * <p>
 * This class only makes the Flint Hatchet the tool shown for axe blocks, instead of the wooden
 * axe. Jade has no public API for that, so it replaces Jade's own axe tool list (kept as is, with
 * the hatchet added first). If a future Jade version changes these internals, the icon simply
 * stays the wooden axe and a warning is logged.
 */
public class JadeCompat implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		try {
			Identifier axeHandlerId = JadeIds.JADE("axe");
			ToolHandler axeHandler = HarvestToolProvider.TOOL_HANDLERS.get(axeHandlerId);
			if (axeHandler == null) {
				UnpunchableTree.LOGGER.warn("Jade has no axe tool handler, the Flint Hatchet won't be shown as a harvest tool");
				return;
			}

			// The first tool able to harvest a block is the one Jade shows.
			List<Item> tools = new ArrayList<>();
			tools.add(ModItems.FLINT_HATCHET);
			for (ItemStack tool : axeHandler.getTools()) {
				tools.add(tool.getItem());
			}
			// Same id: replaces Jade's axe handler, at the same position.
			HarvestToolProvider.registerHandler(SimpleToolHandler.create(axeHandlerId, tools));
		} catch (LinkageError e) {
			UnpunchableTree.LOGGER.warn("Jade's harvest tool internals changed, the Flint Hatchet won't be shown as a harvest tool", e);
		}
	}
}
