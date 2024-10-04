package ninja.ranner.conductor.adapter.in.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final TimeUnit timeUnit;
    private final CommandExecutor commandExecutor;
    private Runnable command;
    private AutoCloseable runningCommand;

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

    public AutoCloseable start(Runnable command) {
        this.command = command;
        runningCommand = commandExecutor.schedule(command, 1, 1, timeUnit);
        return runningCommand;
    }

    public void stop() {
        try {
            runningCommand.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void simulateTick() {
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
            return () -> {
            };
        }
    }
}
