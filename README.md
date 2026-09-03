# Spicato Admin

Fabric mod for Minecraft **1.21.11**. Vanilla-styled pack admin for live config knobs. Open Play or Debug from pause or the `P` key, change sliders, hit Apply — no client relaunch. Existing mob size rolls stay; new rolls use the new curves.

Screens are sectioned by mod domain so this JAR can ship with a pack later. Gameplay mods keep their own `config/<mod>.json`; this mod only edits those files and calls `reload()`.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 1.21.11           |
| Fabric Loader | 0.19.3+           |
| Fabric API    | 0.141.6+1.21.11   |
| Java          | 21+               |

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

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder (pack play uses `dev-pack`).

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `1.21.11`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/spicato_admin.git
cd spicato_admin
git checkout 1.21.11
```

GitHub remote is created when preparing a pack/player ship (Phase 9). Local work uses the version branch from day one.

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `1.21.11-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `1.21.11`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose |
|------------|---------|
| Unit tests | Smoke load; later: pending-vs-applied state, size-graph bins |
| GameTests  | Smoke load; later: `reload()` visible to the next spawn/kill |

Behavior tests are tracked in [Notion Tasks](https://app.notion.com/p/3b716e8d4da380adbd93cf0a73b39d5c). Manual UAT items live in the Mod Catalog.

## Architecture

Pack-owned client screens. Gameplay mods never import this JAR. A later mod is admin-ready when it has JSON at `config/<mod_id>.json`, `reload()` / `save()`, and a section descriptor (`PLAY` or `DEBUG`, widget type, JSON path). Full registry contract lands in Phase 2; V1 sections are hardcoded to that shape.

Requirements: [Epic: Pack Admin Panel](https://app.notion.com/p/3d016e8d4da38189a022c09782391912).
