# Conductor - Ensemble Timers in your Terminal

Display an Ensemble (aka Mob Programming) Timer in the Terminal built into your IDE.
This allows us to stay within our IDE more.

In addition, we'll integrate Conductor with the [mob.sh](https://mob.sh) command line tool, so you can run `mob next`
and `mob start` commands from within Conductor (some day it might even run them for you at specific points in time).

[![asciicast](https://asciinema.org/a/osBaFnrpvGH13GADqdZxjvHwy.svg)](https://asciinema.org/a/osBaFnrpvGH13GADqdZxjvHwy)

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
