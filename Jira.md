# What are we doing?

## Tasks

- [.] Display the timer
  - [.] Pause the timer
    - [.] Make the `Scheduler` stop running its `command`
  - [ ] Resume the timer
  - [ ] Show timer state (running/paused/time remaining) in UI
  - [ ] Render time remaining (prettily)
  - [ ] Share it with some server
    - [ ] Share things that happen through some kind of events / commands?
- [ ] Figure out test for where to call `draw` / wiring up the application
- [ ] Run mob commands
  - [ ] Display mob command output
- [ ] Explore what else we can do with Spring Shell
  - [ ] What other views are there?
  - [ ] Can we show scrolling text?

## Done

- [X] Create an out adapter that takes the timer and shows it on the screen
- [X] Create an IN adapter for receiving scheduled callbacks
- [X] When time is up, print "Timer's up!"
- [X] Update every second

## Mission?

During the turn the timer will count down.
```text
Turn timer: 03:15
Driver:     Lada
Navigator:  Tom

(P)ause, (S)ave, Save with (m)essage
```

Save: does `mob next` (so our changes are committed and pushed)

Imagine you're Lada and the turn is up

```text
Turn is up!
Driver:    Lada (please save or rotate)
Navigator: Tom

(R)otate, (S)ave, Save with (m)essage
```

Rotate: will rotate driver and navigator (it also pushes any unpushed changes)

```text
Next turn:
Driver:    Daniel (please load)
Navigator: Lada

(L)oad, (S)tart
```

Load: will call `mob start`
Start: will call `mob start` and start the timer

## Future nice to have (BACKLOG)

- Save opened files for last driver and restore for next driver
- Auto-fetch when somebody pushed
- Sounds
- Talk to Ensembler
