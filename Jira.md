# What are we doing?

## Tasks

- [ ] Have parameters for your own name and which remote timer to use
  - Note: Ensembler only has one active timer at any time, so we could just
    hard-code that one for starters

## Done

- [X] Run mob commands
  - [X] Display any text in `less` triggered by a command
  - [X] Don't show prompt when in `less`
  - [X] Make `less` output scrollable with j/k
  - [X] Run mob.sh and display its output
  - [X] Add `mob start`
  - [X] Add `mob next`
- [X] Add Root commands to completer
- [X] Move commands from `ConductorApplication` into `Root` and add tests
  - [X] Start/Pause/Rotate timer
- [X] Make sure timer names in conductor-api calls are url encoded
- [X] Display participants
- [X] Display the timer
  - [X] Pause the timer
  - [X] Resume the timer
  - [X] Show timer state (running/paused/time remaining) in UI
  - [X] Render time remaining (prettily)
  - [X] Share it with conductor-api backend
- [X] Make the `Scheduler` stop running its `command`
- [X] Create an out adapter that takes the timer and shows it on the screen
- [X] Create an IN adapter for receiving scheduled callbacks
- [X] When time is up, print "Timer's up!"
- [X] Update every second

## Mission

During the turn the timer will count down.
```text
Turn timer: 03:15
Driver:     Lada
Navigator:  Tom

> (command prompt)
```

`save` command: runs `mob next` (so our changes are committed and pushed)

Imagine you're Lada and the turn is up

```text
Turn is up!
Driver:    Lada (please save or rotate)
Navigator: Tom

> save
```

`rotate` command: will rotate driver and navigator (Future: might also run `save` for us)

```text
Next turn:
Driver:    Daniel (please load)
Navigator: Lada

(L)oad, (S)tart
```

`load`: runs `mob start`
`mob start`: runs `mob start --include-uncommitted-changes`

## Future nice to have (BACKLOG)

- Save opened files for last driver and restore for next driver
- Auto-fetch when somebody pushed
- Sounds
