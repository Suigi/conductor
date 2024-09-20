package ninja.ranner.conductor.adapter.in.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final ScheduledExecutorService executor;
    private final TimeUnit timeUnit;

    public Scheduler(TimeUnit timeUnit) {
        executor = Executors.newSingleThreadScheduledExecutor();
        this.timeUnit = timeUnit;
    }

    public AutoCloseable oncePerSecond(Runnable command) {
        executor.scheduleAtFixedRate(
                command,
                1,
                1,
                timeUnit
        );
        return executor;
    }
}
