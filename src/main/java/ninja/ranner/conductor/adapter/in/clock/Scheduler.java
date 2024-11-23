package ninja.ranner.conductor.adapter.in.clock;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final TimeUnit timeUnit;
    private final CommandExecutor commandExecutor;
    private Runnable command;
    private AutoCloseable runningCommand;
    private volatile boolean isRunning;

    private Scheduler(TimeUnit timeUnit, CommandExecutor commandExecutor) {
        this.timeUnit = timeUnit;
        this.commandExecutor = commandExecutor;
    }

    public static Scheduler create(TimeUnit timeUnit) {
        return new Scheduler(timeUnit, new RealExecutor());
    }

    public static Scheduler createNull() {
        return new Scheduler(TimeUnit.MILLISECONDS, new DummyExecutor());
    }

    public void start(Runnable command) {
        Objects.requireNonNull(command, "command may not be null");
        this.command = command;
        resume();
    }

    public void resume() {
        if (command == null) {
            throw new IllegalStateException("Cannot resume a Scheduler that was never started");
        }
        isRunning = true;
        AutoCloseable closeSchedule = commandExecutor.schedule(command, 1, 1, timeUnit);
        runningCommand = () -> {
            isRunning = false;
            closeSchedule.close();
        };
    }

    public void stop() {
        try {
            runningCommand.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void simulateTick() {
        if (!isRunning) {
            return;
        }
        command.run();
    }

    public interface CommandExecutor {
        AutoCloseable schedule(Runnable command, int initialDelay, int period, TimeUnit timeUnit);
    }

    public static class RealExecutor implements CommandExecutor {
        @Override
        public AutoCloseable schedule(Runnable command, int initialDelay, int period, TimeUnit timeUnit) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(
                    command,
                    initialDelay,
                    period,
                    timeUnit
            );
            return executor;
        }
    }

    public static class DummyExecutor implements Scheduler.CommandExecutor {
        @Override
        public AutoCloseable schedule(Runnable command, int initialDelay, int period, TimeUnit timeUnit) {
            return () -> {};
        }
    }
}
