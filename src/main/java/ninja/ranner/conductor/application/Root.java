package ninja.ranner.conductor.application;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.process.Runner;
import ninja.ranner.conductor.adapter.out.terminal.Lines;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Root {
    private final TerminalUi terminalUi;
    private final Scheduler scheduler;
    private final ConductorApiClient apiClient;
    private final Runner runner;
    private final String timerName;
    private final Map<String, CommandHandler> commandHandlers = Map.of(
            // built-in
            "q", CommandHandler.of(this::quit),
            "quit", CommandHandler.of(this::quit),
            // timer controls
            "start", CommandHandler.of(this::start),
            "pause", CommandHandler.of(this::pause),
            "rotate", CommandHandler.of(this::rotate),
            // mob.sh
            "status", CommandHandler.of(() -> runAndPrintLess("mob status")),
            "save", CommandHandler.of(() -> runAndPrintLess("mob next")),
            "load", CommandHandler.of(() -> runAndPrintLess("mob start")),
            // git
            "gss", CommandHandler.of(() -> runAndPrintLess("git -c color.status=always status --short"))
    );

    @FunctionalInterface
    public interface CommandHandler {
        void handle(String command);

        static CommandHandler of(Runnable runnable) {
            return ignored -> runnable.run();
        }
    }

    private volatile boolean keepRunning = true;

    public Root(Scheduler scheduler, TerminalUi terminalUi, ConductorApiClient apiClient, String timerName, Runner runner) {
        this.terminalUi = terminalUi;
        this.scheduler = scheduler;
        this.apiClient = apiClient;
        this.timerName = timerName;
        this.runner = runner;
    }

    public Thread startInBackground() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Thread thread = Thread.startVirtualThread(() -> run(countDownLatch));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return thread;
    }

    private void run(CountDownLatch countDownLatch) {
        terminalUi.enterCursorAddressingMode();

        scheduler.start(this::fetchAndRenderTimer);

        try {
            terminalUi.update(Lines.of("Welcome to Conductor"));
            countDownLatch.countDown();
            while (keepRunning) {
                readAndHandleCommand();
            }
        } finally {
            scheduler.stop();
            terminalUi.exitCursorAddressingMode();
        }
    }

    private void readAndHandleCommand() {
        String command = terminalUi.readCommand();
        CommandHandler handler = commandHandlers.get(command);
        if (handler != null) {
            handler.handle(command);
        }
    }

    private void quit() {
        keepRunning = false;
    }

    private void fetchAndRenderTimer() {
        try {
            TimerTransformer transformer = new TimerTransformer(null);
            apiClient
                    .fetchTimer(timerName)
                    .map(transformer::transform)
                    .ifPresent(terminalUi::update);
        } catch (IOException | InterruptedException e) {
            // ignore
        }
    }

    private void start() {
        try {
            apiClient.startTimer(timerName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void pause() {
        try {
            apiClient.pauseTimer(timerName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void rotate() {
        try {
            apiClient.nextTurn(timerName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runAndPrintLess(String command) {
        scheduler.stop();
        terminalUi.less("Running command\n  " + command, () -> {
            Runner.RunResult result = runner.execute(command.split(" "));
            return result.stdout();
        });
        scheduler.resume();
    }

}
