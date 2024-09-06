# What are we doing?

## Tasks

- [ ] Display the timer
  - [ ] Local only, first
  - [ ] Render time remaining
  - [ ] Update every second
  - [ ] Pause the timer
  - [ ] Resume the timer
  - [ ] Show "Timer ended"
  - [ ] Share it with some server
    - [ ] Share things that happen through some kind of events / commands?
- [ ] Run mob commands
  - [ ] Display mob command output
- [ ] Explore what else we can do with Spring Shell
  - [ ] What other views are there?
  - [ ] Can we show scrolling text?

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
Driver:    Lada (please push)
Navigator: Tom

(R)otate, (S)ave, Save with (m)essage
```

Rotate: will rotate driver and navigator (it also pushes any unpushed changes)

```text
Next turn:
Driver:    Daniel (please fetch)
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