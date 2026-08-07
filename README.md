# DragonMineZ: Auto Trainer

A [Forge](https://minecraftforge.net/) mod that automates the training minigames in [DragonMineZ](https://www.curseforge.com/minecraft/mc-mods/dragon-mine-z), so you can grind Training Points without manually playing through the sometimes tedious minigames every time.

The training automation runs on your client, but the server decides whether you may use it at all. This mod is required on both the client and the server: a client carrying it cannot join a server without it, and a server carrying it will not admit a client without it. Client and server must also run the same version of the mod.

## Features

- **Auto Training** - automatically plays through whichever minigame you select next.
- **Repeat Training** - once a configured number of levels in the minigame are cleared, it will automatically end the minigame and restart the same minigame, looping indefinitely until you stop it on your own.
- **Levels to Complete Training** - set how many levels should be cleared per minigame run before the loop restarts.
- **In-game settings screen** - a settings button is added into the Minigames tab so you don't need to touch a config file to toggle or change things. 
- **Training sessions** - (disabled by default) cap how long you and others may train automatically per sitting and apply a cooldown.

### Supported minigames

Rhythm, Gravity, Precision, Shadow Boxing, and Control

## Requirements

- Minecraft **1.20.1**
- Minecraft Forge **47.4.10** or newer
- [DragonMineZ](https://www.curseforge.com/minecraft/mc-mods/dragon-mine-z) **2.1.3** (required dependency)

## Installation

1. Install Minecraft Forge 47.4.10+ for Minecraft 1.20.1.
2. Make sure DragonMineZ 2.1.3 is installed.
3. Drop the mod jar into your `/mods` folder. 
4. Launch the game.

## Client

### Client-side configuration

Settings can be changed two ways:

**In-game:** Open the Minigames screen and click **Auto Train Settings** in the bottom of the left panel. From there you can toggle Auto Trainer and Repeat Training, and adjust how many levels to clear per minigame.

**Config file:** `config/dmzautotrainer-client.json`

| Option | Default | Description                                                                         |
|---|---|-------------------------------------------------------------------------------------|
| `enableAutoTrainer` | `false` | Enables automation of the next played minigame                                      |
| `enableRepeatTraining` | `true` | Enables auto-completing and restarting a run after the level target is reached      |
| `levelsToComplete` | `50` | Number of levels to clear before a run ends and (if repeat training is on) restarts |

## Server/Singleplayer

### Training Sessions

An earlier version of this mod was purely client-side, with nothing running on the server at all. That meant a server owner had no way to allow auto-training while also limiting the usage of it. Since this mod automates gameplay a server might reasonably want limited rather than banned outright, an optional server-side feature was added. Thanks goes to Kiziro for pointing out this issue. 

**Config file:** `config/dmzautotrainer-server.json`

| Option | Default | Description                                                                      |
|---|---|----------------------------------------------------------------------------------|
| `enableAutoTrainer` | `true` | Master switch. Set to `false` to disable auto training entirely on this server    |
| `enableSessions` | `false` | Enables the session/cooldown system below                                        |
| `sessionDuration` | `900` | How long (in seconds) a player may auto-train per session                        |
| `sessionCooldown` | `900` | How long (in seconds) a player must wait after a session before starting another |

The server config is read once when the server starts, so changes need a restart to take effect.

When `enableAutoTrainer` is `false`, players still join normally and the Auto Train Settings button still appears in the Minigames tab, greyed out, with "Disabled on this server" shown above it. The auto trainer never runs and the server grants no training sessions.

When enabled:
- The training timer doesn't start counting down until a minigame is actually being played by the auto trainer.
- Leaving the minigame early starts a cooldown proportional to how much of the session was actually used, instead of a full cooldown.
- When a minigame is playing and the session timer runs out, the auto trainer will continue and finish the minigame, as it normally does, and will then start the cooldown.

### What this switch does not do

The automation itself runs on the client, and a server cannot observe a player clicking through a minigame. This switch asks the mod not to run and refuses to grant training sessions; it is not anti-cheat, and a modified build could ignore it. It is intended for honest players on servers whose owner has made a rule, not as an enforcement mechanism.

### Admin Commands

Requires server operator permissions. Targets accept either a player's username (works for offline players as well) or `@a` for everyone currently online.

```
/dmztrainer cooldown reset <targets>
/dmztrainer cooldown set <targets> <seconds>
/dmztrainer cooldown get <targets>
```

There's also a live debug toggle for server admins troubleshooting the session system. It sends every raw and derived session value for a target to your chat every second until toggled off again.

```
/dmztrainer debug
/dmztrainer debug <player>
```

## Acknowledgments

This mod is built entirely on top of [DragonMineZ](https://dragonminez.com) ([GitHub](https://github.com/DragonMineZ/dragonminez)), created by Yuseix and ezShokkoh, with contributions from Bruno, Bati2ra, KyoSleep, JotaJoestar, and Toji71_. DragonMineZ is licensed under the [GNU General Public License v3.0](https://github.com/DragonMineZ/dragonminez/blob/main/LICENSE). See `CREDITS.txt` for full credits.


## License

All Rights Reserved.
