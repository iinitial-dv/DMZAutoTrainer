# DragonMineZ: Auto Trainer

A client-side [Minecraft Forge](https://minecraftforge.net/) mod that automates the training minigames in [DragonMineZ](https://www.curseforge.com/minecraft/mc-mods/dragon-mine-z), so you can grind Training Points without manually playing through the sometimes tedious minigames every time.

## Features

- **Auto Training** - automatically plays through whichever minigame you select next.
- **Repeat Training** - once a configured number of levels in the minigame are cleared, it will automatically end the minigame and restart the same minigame, looping indefinitely until you stop it on your own.
- **Levels to Complete Training** - set how many levels should be cleared per minigame run before the loop restarts.
- **In-game settings screen** - a settings button is added into the Minigames tab so you don't need to touch a config file to toggle or change things.
### Supported minigames

Rhythm, Gravity, Precision, Shadow Boxing, and Control

## Requirements

- Minecraft **1.20.1**
- Minecraft Forge **47.4.10** or newer
- [DragonMineZ](https://www.curseforge.com/minecraft/mc-mods/dragon-mine-z) **2.1.3** or newer (required dependency)

## Installation

1. Install Minecraft Forge 47.4.10+ for Minecraft 1.20.1.
2. Make sure DragonMineZ 2.1.3+ is installed.
3. Drop the mod jar into your `mods` folder.
4. Launch the game.

## Configuration

Settings can be changed two ways:

**In-game:** Open the Minigames screen and click **Auto Train Settings** in the top-left panel. From there you can toggle Auto Trainer and Repeat Training, and adjust how many levels to clear per run.

**Config file:** `config/dmzautotrainer-client.toml`

| Option | Default | Description                                                                         |
|---|---|-------------------------------------------------------------------------------------|
| `enableAutoTrainer` | `false` | Enables automation of the next played minigame                                      |
| `enableRepeatTraining` | `true` | Enables auto-completing and restarting a run after the level target is reached      |
| `levelsToComplete` | `50` | Number of levels to clear before a run ends and (if repeat training is on) restarts |

## Disclaimer

This mod automates gameplay that would normally require manual player input. Use it on singleplayer worlds or servers where this kind of client-side automation is explicitly allowed. Check with server admins before using it anywhere else, as it may violate a server's rules even though it is a client-side mod.

## Acknowledgments

This mod is built entirely on top of [DragonMineZ](https://dragonminez.com) ([GitHub](https://github.com/DragonMineZ/dragonminez)), created by Yuseix and ezShokkoh, with contributions from Bruno, Bati2ra, KyoSleep, JotaJoestar, and Toji71_. DragonMineZ is licensed under the [GNU General Public License v3.0](https://github.com/DragonMineZ/dragonminez/blob/main/LICENSE). See `CREDITS.txt` for full credits.


## License

All Rights Reserved.
