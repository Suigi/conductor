package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.RemoteTimer;
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

    public Lines transform(RemoteTimer timer) {
        Lines lines = Lines.of(
                "Time:        %s".formatted(renderRemainingTime(timer)),
                ""
        );
        //noinspection SizeReplaceableByIsEmpty
        if (timer.participants().size() > 0) {
            lines.append("Navigator:   %s".formatted(timer.participants().get(0)));
        }
        if (timer.participants().size() > 1) {
            lines.append("Driver:      %s".formatted(timer.participants().get(1)));
        }
        if (timer.participants().size() > 2) {
            lines.append("Next Driver: %s".formatted(timer.participants().get(2)));
        }
        return lines;
    }

    private String renderRemainingTime(RemoteTimer timer) {
        if (timer.remaining().isZero()) {
            return "Turn is up!";
        }
        long totalSeconds = timer.remaining().getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds - minutes * 60;

        return "%02d:%02d | %s".formatted(minutes, seconds, timer.state());
    }

}
