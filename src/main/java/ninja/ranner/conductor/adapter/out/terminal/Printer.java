package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.Timer;

public class Printer {

    public void print(Timer timer) {
        if (timer.remainingSeconds() > 0) {
            System.out.println("Time left: " + timer.remainingSeconds() + " second");
        } else {
            System.out.println("Turn is up!");
        }
    }
}
