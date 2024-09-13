package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.Timer;

public class Printer {

    public void print(Timer timer) {
        if (timer.remainingSeconds() > 0) {
            System.out.printf("Time left: %02d:%02d\n",
                    timer.remainingSeconds() / 60,
                    timer.remainingSeconds() % 60);
        } else {
            System.out.println("Turn is up!");
        }
    }
}
