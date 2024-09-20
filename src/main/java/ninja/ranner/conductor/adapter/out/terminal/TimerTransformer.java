package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.Timer;

public class TimerTransformer {

    private final Timer timer;

    public TimerTransformer(Timer timer) {
        this.timer = timer;
    }

    public Lines transform() {
        int remainingSeconds = timer.remainingSeconds();
        if (remainingSeconds == 0) {
            return Lines.of("Timer's up!");
        }
        return Lines.of(String.valueOf(remainingSeconds));
    }
}
