# Unpunchable Tree

No more punching trees. Logs and wood behave like stone: without an axe they are
slow to mine and drop nothing. A **Flint Hatchet**, craftable in the inventory,
gets you started.

Optionally (off by default), dirt, sand, gravel and every other shovel block can
need a shovel in the same way.

Fabric mod, required on both the server and the client.

## Features

### Logs need an axe
Logs, wood, stems and hyphae (stripped variants included, everything in
`#minecraft:logs`) only drop when mined with an axe. Without one they take as
long to mine as stone by hand.

This uses the exact vanilla mechanic of stone and ores: logs are flagged as
"requiring the correct tool for drops". As a result:

- any item whose tool component can harvest the block counts: vanilla axes, the
  Flint Hatchet, modded axes, paxels and multitools;
- explosions, the Wither and other non-player sources still drop the block,
  like stone blown up by a creeper;
- mods that show which tool a block needs see it too (see Jade below).

### Flint Hatchet
The first axe, made without a crafting table:

```
. F      F = flint
S .      S = stick
```

(either diagonal works). Flint comes from gravel and sticks from leaves and dead
bushes, all obtainable by hand. The recipe is unlocked once you pick up flint.

It is a full axe: it strips logs, scrapes copper, disables shields, accepts axe
enchantments and is in the `#minecraft:axes` tag. It is repaired with flint, and
appears in both the Tools and Combat creative tabs.

In combat it is a glass cannon between the wooden sword and the wooden axe: axe
abilities and more damage per second, but half the durability.

| | Wooden Sword | Flint Hatchet | Wooden Axe |
| --- | --- | --- | --- |
| Durability | 59 | 30 | 59 |
| Mining speed | – | 2.5 | 2.0 |
| Attack damage | 4 | 5 | 7 |
| Attack speed | 1.6 | 1.4 | 0.8 |
| Enchantability | 15 | 8 | 15 |

#### Throwing
Hold right click (at least half a second, like a trident) and release to throw
the hatchet. It tumbles through the air and:

- **hits an entity**: deals 7 damage (a thrown trident deals 8), plus Sharpness,
  Smite and other damage enchantments, and loses 3 durability (a melee hit costs 2).
  It then drops next to the target, or breaks if it runs out of durability;
- **is blocked or does no damage** (shield, immune mob): drops intact;
- **hits a block**: sticks into it like an arrow or a trident.

It can be picked up again by walking over it. A hatchet thrown in survival never
despawns.

The hatchet accepts **Loyalty** (enchanting table, anvil): it then flies back to
its owner after hitting an entity or a block, exactly like a trident. No other
trident enchantment (Riptide, Impaling, Channeling) can be applied.

By default, a shield (or any other blocking item) in the off hand takes priority:
with one equipped, right click raises the shield instead of throwing. As with any
axe, right clicking a log strips it instead of throwing.

### Ground needs a shovel (optional)
Disabled by default. When enabled, every block in `#minecraft:mineable/shovel`
(dirt, grass, sand, gravel, clay, mud, soul sand, snow, concrete powder...) needs a
shovel in the same way.

## Configuration

`config/unpunchabletree.json`:

| Option | Default | Effect |
| --- | --- | --- |
| `punchableTrees` | `false` | Logs and wood can be harvested by hand |
| `punchableGround` | `true` | Shovel blocks can be harvested by hand |
| `hatchetThrowing` | `true` | The Flint Hatchet can be thrown. The options below only apply when this is `true` |
| `shieldOverThrowing` | `true` | With a shield in the off hand, right click blocks instead of throwing |
| `thrownHatchetDurabilityCost` | `3` | Durability lost when a thrown hatchet hits an entity. Unbreaking reduces it; 30 or more breaks an unenchanted hatchet on every hit |

With [Mod Menu](https://modrinth.com/mod/modmenu) and
[Cloth Config](https://modrinth.com/mod/cloth-config) installed, every option can
be edited in game. Neither is required.

The server's settings always win: they are sent to every player on join, so the
client's mining speed and hatchet behavior always match the server. In singleplayer and on LAN worlds
you host, changes made in the Mod Menu screen apply immediately. On a dedicated
server, edit the file and restart the server.

### Tags
The affected blocks come from tags, so data packs and modpacks can change them:

| Tag | Default |
| --- | --- |
| `#unpunchabletree:needs_axe` | `#minecraft:logs` |
| `#unpunchabletree:needs_shovel` | `#minecraft:mineable/shovel` |
| `#unpunchabletree:flint_hatchet_repair_materials` (item) | `minecraft:flint` |

The Flint Hatchet recipe is a regular data pack recipe
(`unpunchabletree:flint_hatchet`) and can be removed or replaced the same way.

## Compatibility

- Minecraft 1.21.9 to 1.21.11, Fabric Loader 0.20.1+, Fabric API. (1.21.9 reworked
  entity rendering, which the thrown hatchet relies on.)
- Must be installed on both sides: the server because of the new item, the client
  because mining speed is predicted client-side.
- Mods that add axes or shovels work automatically as long as their tool component
  can harvest the block (which is how vanilla-style tools are built).
- [Jade](https://modrinth.com/mod/jade) (optional): looking at a log shows that it
  needs a tool, with the Flint Hatchet as the minimum tool, and a ✔ or ✘ depending
  on the held item. The same goes for shovel blocks when they need a shovel. The
  hatchet icon relies on Jade internals (Jade has no API for it); if a Jade update
  changes them, Jade falls back to its wooden axe icon and a warning is logged.

## Building

Requires JDK 21 (the Gradle wrapper downloads Gradle itself).

```
./gradlew build          # jar in build/libs/
./gradlew runClient      # development client, with Mod Menu and Cloth Config
./gradlew runServer
```

## Project layout

```
src/main/java/net/grotacam/unpunchabletree/
  UnpunchableTree.java           entry point, rule sync on join
  rules/                         harvest and hatchet rules, synced to clients
  mixin/BlockStateBaseMixin      flags blocks as requiring the correct tool
  config/UnpunchableTreeConfig   config file (Gson)
  network/ServerRulesPayload     server -> client rule sync
  item/FlintHatchetItem          the hatchet: axe + charge and throw
  entity/ThrownHatchet           the thrown hatchet projectile
  registry/                      items, entity type, damage type, tags
src/client/java/.../client/      sync receiver, thrown hatchet renderer, Jade plugin,
                                 Mod Menu + Cloth Config screen
src/main/resources/
  assets/unpunchabletree/        texture, models, lang; models/item/flint_hatchet_throwing.json
                                 is the in-hand pose while charging a throw
  data/                          recipe, recipe advancement, damage type, tags
```

The code uses official Mojang mappings, like Minecraft 26.1 and later.

## License

MIT, see [LICENSE](LICENSE).
