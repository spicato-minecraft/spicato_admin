# Spicato Admin

Fabric mod for Minecraft **26.2**. Vanilla-styled pack admin for live config knobs. Open Play or Debug from pause or the `P` key, change sliders, hit Apply — no client relaunch. Existing mob size rolls stay; new rolls use the new curves.

Screens are sectioned by mod domain so this JAR can ship with a pack later. Gameplay mods keep their own `config/<mod>.json`; this mod only edits those files and calls `reload()`.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 26.2              |
| Fabric Loader | 0.19.5+           |
| Fabric API    | 0.161.0+26.2      |
| Java          | 25+               |

## Build

From this directory:

```bash
./gradlew build
```

The output JAR is at `build/libs/spicato_admin-1.0.0.jar` (version from `gradle.properties`).

## Test

Play the indexed local set from [`../dev-pack`](../dev-pack/README.md) (`./gradlew runClient` there). This repo’s `runClient` is for isolated admin work.

```bash
./gradlew test          # unit tests (JUnit + Fabric Loader bootstrap)
./gradlew runGameTest   # server GameTests
./gradlew runClient     # isolated admin client (not the pack launcher)
./gradlew build         # full CI gate (includes unit tests + GameTests)
```

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder (pack play uses `dev-pack`).

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `26.2`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/spicato_admin.git
cd spicato_admin
git checkout 26.2
```

GitHub remote is created when preparing a pack/player ship (Phase 9). Local work uses the version branch from day one.

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `26.2-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `26.2`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose |
|------------|---------|
| Unit tests | Smoke load; V1 catalog keys stay Play/Debug segregated; pending Apply writes temp dirs and reloads without touching live `config/` |
| GameTests  | Smoke load; later: `reload()` visible to the next spawn/kill |

Behavior tests are tracked in [Notion Tasks](https://app.notion.com/p/3b716e8d4da380adbd93cf0a73b39d5c). Manual UAT items live in the Mod Catalog.

## Architecture

Pack-owned client screens. Gameplay mods **never** import this JAR. This JAR may call a gameplay mod's `reload()` when that mod is loaded.

The knob table lives in `spicato_admin.knobs.AdminCatalog`. V1 rows are hardcoded there. Screens must consume that table. Do not add YACL, Cloth, or a per-mod screen class.

Requirements: [Epic: Pack Admin Panel](https://app.notion.com/p/3d016e8d4da38189a022c09782391912).

## Adding knobs (later-mod contract)

A gameplay mod is admin-ready when all three exist. Then add rows to `AdminCatalog` — that is the only UI change.

### 1. JSON at `config/<mod_id>.json`

Same file the mod already uses. Keys are stable; the admin reads and writes them. Do not invent a second store.

### 2. `reload()` and `save()`

| Method | Behavior |
|--------|----------|
| `reload()` | Replace in-memory config from the game `config/` file. Must work mid-session (no client restart). Size-style configs must `compile()` after load. |
| `reloadFrom(Path configDir)` | Same, from a directory. Required for unit tests; Apply uses `reload()` after writing the game config dir. |
| `save()` | Write the current in-memory object to `config/<mod_id>.json`. |

Existing mobs, items, and world state stay put. New events (next spawn, next kill, next TNT use, next size **roll**) read the reloaded values.

Reference implementations: `mob_loot_rebalance.config.ModConfig`, `structure_loot_rebalance.config.PackConfig`, `mob_size_diversity.config.SizeConfig`, `useful_tnt.UsefulTntConfig`, `functional_spyglass.config.SpyglassConfig`.

### 3. Section + knob rows in `AdminCatalog`

| Field | Meaning |
|-------|---------|
| `modId` | Fabric mod id; must match `config/<modId>.json` |
| `page` | `PLAY` (balance) or `DEBUG` (UAT / diagnostics) |
| `key` | Dot-separated JSON path (`elites.spawn_chance`, `presets.set_a`) |
| `label` | Section copy shown in the vanilla UI |
| `widget` | `CHECKBOX`, `SLIDER`, or `CURVE` |
| `min` / `max` / `step` | Slider envelope. For `CURVE`, bounds for peak / min / max. Unused on checkboxes. |

`CURVE` keys point at a preset object with `peak`, `min`, and `max`. Play sliders edit those three; the density graph is Phase 5. Nested rule lists stay out of the admin until a mod actually needs an editor.

Empty Debug sections (Size, TNT) and empty Play sections (Spyglass) are still listed in `SECTIONS` so grouping stays stable when knobs are added later. Pack Admin footer **Page** switches Play vs Debug.

### Checklist (example: Power and Cost)

1. Put tunables in `config/power_and_cost.json` (or the existing file name if it already matches `modId`).
2. Add `reload()` / `reloadFrom` / `save()`; unit-test disk → memory without restarting the process.
3. Append `new AdminSection("power_and_cost", "Power and Cost")` if this is a new mod grouping.
4. Append `AdminKnob` rows with `PLAY` or `DEBUG`.
5. Do not create a new screen, mixin, or widget toolkit.

Apply still writes JSON then calls that mod's `reload()`. Preview sliders do not touch the world until Apply.

In a Loom `runClient` (development), Apply also copies the same files to `applied-config/` next to `run/` so wiping the instance folder does not drop a UAT pass. That snapshot is not shipped defaults — bake those numbers into each gameplay mod's Java defaults and `ShippedDefaults` when the pack is ready to ship.
