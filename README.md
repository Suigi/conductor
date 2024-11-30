# Conductor - Ensemble Timers in your Terminal

Display an Ensemble (aka Mob Programming) Timer in the Terminal built into your IDE.
This allows us to stay within our IDE more.

You can run [mob.sh](https://mob.sh) commands from within Conductor.
(Some day it might run some of them automatically based on the rotation timer).

[![asciicast](https://asciinema.org/a/osBaFnrpvGH13GADqdZxjvHwy.svg)](https://asciinema.org/a/osBaFnrpvGH13GADqdZxjvHwy)

## Available Commands

| Command      | Description                                                               |
|--------------|---------------------------------------------------------------------------|
| `start`      | Starts/resumes the clock on the timer                                     |
| `pause`      | Pauses the clock on the timer                                             |
| `rotate`     | Rotates driver and navigator                                              |
| `save`       | Runs `mob next` to save for hand-over                                     |
| `load`       | Runs `mob start` to load latest changes                                   |
| `mob status` | Runs `mob status`                                                         |
| `mob start`  | Runs `mob start --include-uncommitted-changes` for starting a mob session |
| `mob done`   | Runs `mob done --squash-wip` for finishing the mob session                |
| `gs`         | Shows `git status`                                                        |
| `gss`        | Shows `git status --short`                                                |

### "Modal" Text Output

When running external commands (like `git status` or `mob next`) their output is shown instead of the
timer. You can scroll down/up the text with `j`/`k` or jump to the top/end with `g`/`G`.
To return to the timer, press `q`.

## Dependencies

This project uses

* Java 22
* The [jline](https://github.com/jline/jline3) library for interacting with the terminal
* The [just](https://github.com/casey/just) command line runner

## Build and Run

The following command will use the Maven wrapper to package the project:

```shell
just build
```

If you want to re-run the application without building it, you can use:

```shell
just run
```

If you want to re-build and then run it, use:

```shell
just all
```
