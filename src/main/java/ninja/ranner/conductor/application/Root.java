package ninja.ranner.conductor.application;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.terminal.Lines;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;

import java.io.IOException;
import java.util.Map;

public class Root {
    private final TerminalUi terminalUi;
    private final Scheduler scheduler;
    private final ConductorApiClient apiClient;
    private final String timerName;
    private final Map<String, CommandHandler> commandHandlers = Map.of(
            "q", CommandHandler.of(this::quit),
            "quit", CommandHandler.of(this::quit),
            "less", CommandHandler.of(this::less),
            "start", CommandHandler.of(this::start),
            "pause", CommandHandler.of(this::pause),
            "rotate", CommandHandler.of(this::rotate)
    );

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

    @FunctionalInterface
    public interface CommandHandler {
        void handle(String command);

        static CommandHandler of(Runnable runnable) {
            return ignored -> runnable.run();
        }
    }

    private volatile boolean keepRunning = true;

    public Root(Scheduler scheduler, TerminalUi terminalUi, ConductorApiClient apiClient, String timerName) {
        this.terminalUi = terminalUi;
        this.scheduler = scheduler;
        this.apiClient = apiClient;
        this.timerName = timerName;
    }

    public Thread startInBackground() {
        return Thread.startVirtualThread(() -> {
            terminalUi.enterCursorAddressingMode();

            scheduler.start(this::fetchAndRenderTimer);

            try {
                terminalUi.update(Lines.of("Welcome to Conductor"));
                while (keepRunning) {
                    readAndHandleCommand();
                }
            } finally {
                scheduler.stop();
                terminalUi.exitCursorAddressingMode();
            }
        });
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

    private void less() {
        scheduler.stop();
        terminalUi.less("Hello, less!");
        scheduler.resume();
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

}
