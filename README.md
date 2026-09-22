# FortuneRift

A Minecraft mod about breaking a Fortune Rift and getting a weighted random event. It is built from the [MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template) as three separate projects:

| Directory | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1` | 1.20.1 | Fabric, Forge | 17 |
| `1.21.1` | 1.21.1 | Fabric, NeoForge, Forge | 21 |
| `26.3` | 26.3 | Fabric, NeoForge | 25 |

Gameplay for 1.0.0 is implemented in `26.3`. The 1.20.1 and 1.21.1 trees are rebranded templates ready for the same behavior.

## Build

From a version directory:

```powershell
.\gradlew.bat build
```

Mod id: `fortunerift`. Package: `io.nightbeam.fortunerift`.

## Play

Break a Fortune Rift. The server always rolls `fortunerift:guaranteed` (twice for blessed, cursed, ancient, and void rifts, three times for mythic), then one of 100 events. Natural rifts do not drop with Silk Touch. A rift you placed drops itself only when `allowSilkTouch` is true, and that pickup does not also roll loot. Explosions and pistons remove the block without rolling an event.

The CurseForge page is `marketplace/CURSEFORGE.md`. The mod icon is `marketplace/icon.png` and `fortunerift.png` inside each version's resources.

Config file: `config/fortunerift.json`.

Commands, permission level 2:

- `/fortunerift give <player> <rift> [amount]`
- `/fortunerift event <event> [player]`
- `/fortunerift random <player>`
- `/fortunerift reload`
- `/fortunerift list`
- `/fortunerift fortune <player>`
- `/fortunerift setfortune <player> <value>`

`<rift>` is `fortune`, `blessed`, `cursed`, `ancient`, `void`, or `mythic`.

The Rift Journal lists events you have seen. Secret events stay as `???` until you see them.

## API

Other mods on the same loader can call `io.nightbeam.fortunerift.api.FortuneRiftApi`:

- `register(RiftEvent event)` adds an event.
- `addActivationListener` can cancel a break before the roll.
- `addCompletionListener` hears the chosen id.

Datapacks can add `data/fortunerift/events/<id>.json` with `type` set to `loot_burst`, `status_effect`, or `mob_spawn`, or override `weight`, `rarity`, flags, and `enabled` on a built-in id. A Java `script` is not replaced by a datapack.
