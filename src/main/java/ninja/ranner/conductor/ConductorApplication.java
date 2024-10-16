package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.application.Root;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConductorApplication {

    private static final String timerName = "my-timer";
    private static final ConductorApiClient apiClient = ConductorApiClient
            .create("http://localhost:8080");

    public static void main(String[] args) throws Exception {
        // The list of available commands and how to handle them
        // should move out of this method to ... somewhere else.
        // But, is that an In Adapter or an Application level concern?
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

        Root root = new Root(
                Scheduler.create(TimeUnit.SECONDS),
                tui,
                apiClient,
                timerName
        );
        root.startInBackground().join();
    }

}
