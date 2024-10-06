package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.terminal.Lines;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;
import ninja.ranner.conductor.domain.RemoteTimer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ConductorApplication {

    private static final String timerName = "my-timer";
    private static final ConductorApiClient apiClient = ConductorApiClient.create("http://localhost:8080");

    public static void main(String[] args) throws Exception {
        TerminalUi tui = TerminalUi.create(List.of(
                "quit",
                "load",
                "save",
                "pause",
                "start",
                "rotate"));

        tui.registerCommandHandler(cmd -> {
            try {
                switch (cmd) {
                    case "start" -> apiClient.startTimer(timerName);
                    case "pause" -> apiClient.pauseTimer(timerName);
                    case "rotate" -> apiClient.nextTurn(timerName);
                }
            } catch (Exception e) {
                // ignored (for now)
            }
        });
        Scheduler scheduler = Scheduler.create(TimeUnit.SECONDS);
        try (AutoCloseable ignored = scheduler.start(() -> renderRemoteTimer(tui))) {
            tui.run();
        }
    }

    private static void renderRemoteTimer(TerminalUi terminalUi) {
        TimerTransformer transformer = new TimerTransformer(null);
        try {
            Optional<RemoteTimer> timer = apiClient.fetchTimer(timerName);
            Lines lines = transformer.transform(timer.orElseThrow());
            terminalUi.update(lines);
        } catch (Exception e) {
            terminalUi.update(Lines.of("Failed to fetch remote timer:", e.getMessage()));
        }

    }

}
