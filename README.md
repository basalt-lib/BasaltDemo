<p align="center"><img src="assets/basalt.png" width="96" alt="Basalt"></p>
<h1 align="center">BasaltDemo</h1>
<p align="center">Instanced dungeon for Basalt, public API only.</p>
<p align="center"><a href="https://github.com/basalt-lib/BasaltDemo/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/basalt-lib/BasaltDemo/build.yml?branch=master&amp;label=build&amp;style=flat" alt="build"></a> <img src="https://img.shields.io/badge/Paper-1.21.11-555?style=flat" alt="Paper 1.21.11"> <img src="https://img.shields.io/badge/Java-21%2B-555?style=flat" alt="Java 21+"> <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-555?style=flat" alt="Apache-2.0"></a></p>

## What it shows

- Model built in real blocks, captured as a blueprint, placed in a new virtual world per run
- Runs overlap at the same coordinates, groups don't see each other
- Virtual zombies: the last kill opens the door, breaking the gold block ends the run
- Players go back to where they were in the real world

## Showcase

<p align="center"><a href="https://www.youtube.com/watch?v=KCYVns9mYfQ"><img src="assets/showcase.jpg" width="640" alt="Basalt showcase video"></a></p>
<p align="center"><a href="https://www.youtube.com/watch?v=KCYVns9mYfQ">Watch the showcase on YouTube</a></p>

## Try it

Needs Paper 1.21.11 and [BasaltCore](https://github.com/basalt-lib/BasaltCore), with its API in your local Maven repository. Build with `./gradlew build` (JDK 25), put `build/libs/BasaltDemo-0.1.0-SNAPSHOT.jar` in `plugins/`.

- `/dungeon start`: start a run
- `/dungeon join <player>`: join a player's run
- `/dungeon leave`: leave your run
- `/dungeon list`: running dungeons
- `/dungeon status`: state of your run

`basalt.demo.play` (default `true`): the commands above. `basalt.demo.admin` (default `op`): `/dungeon model visit|capture|rebuild` and the `[player]` argument.

## Configuration

<details><summary><code>plugins/BasaltDemo/config.yml</code></summary>

| Key | Default | Description |
|---|---|---|
| `host.world` | `basalt_demo` | Empty real world created by the plugin |
| `host.model-chunk-x` | `0` | Chunk x of the model |
| `host.model-chunk-z` | `0` | Chunk z of the model |
| `host.play-chunk-x` | `64` | Chunk x of the runs |
| `host.play-chunk-z` | `0` | Chunk z of the runs |
| `host.floor-y` | `64` | Floor y of the model and the runs |
| `run.max-runs` | `8` | Runs at the same time |
| `run.max-players` | `4` | Players per run |
| `run.monster-health` | `6` | Zombie health |
| `run.return-delay-seconds` | `8` | Seconds between the end of a run and the return |

</details>

## License

Apache-2.0, see [LICENSE](LICENSE).

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft. The icon is the Minecraft basalt block, property of Mojang.
