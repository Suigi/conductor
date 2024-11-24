package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.process.Runner;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.application.Root;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConductorApplication {

    private static final String timerName = "Ensemble Timer Demo";
    private static final ConductorApiClient apiClient = ConductorApiClient
            .create("http://localhost:8088");

    public static void main(String[] args) throws Exception {
        // The list of available commands and how to handle them
        // should move out of this method to ... somewhere else.
        // But, is that an In Adapter or an Application level concern?
        TerminalUi tui = TerminalUi.create(List.of(
                "less",
                "quit",
                "load",
                "save",
                "pause",
                "start",
                "rotate"));

        Root root = new Root(
                Scheduler.create(TimeUnit.SECONDS),
                tui,
                apiClient,
                timerName,
                Runner.create()
        );

        root.startInBackground().join();
    }

}
