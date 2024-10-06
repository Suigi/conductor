package ninja.ranner.conductor.application;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;
import ninja.ranner.conductor.domain.RemoteTimer;

import java.io.IOException;
import java.util.Optional;

public class Root {
    private final TerminalUi terminalUi;
    private final Scheduler scheduler;
    private final ConductorApiClient apiClient;
    private final String timerName;

    public Root(Scheduler scheduler, TerminalUi terminalUi, ConductorApiClient apiClient, String timerName) {
        this.terminalUi = terminalUi;
        this.scheduler = scheduler;
        this.apiClient = apiClient;
        this.timerName = timerName;
    }

    public Thread startInBackground() {
        return Thread.startVirtualThread(() -> {
            try (AutoCloseable ignored = scheduler.start(this::render)) {
                terminalUi.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void render() {
        try {
            Optional<RemoteTimer> timer = apiClient.fetchTimer(timerName);
            timer.ifPresent(t -> {
                terminalUi.update(new TimerTransformer(null).transform(t));
            });
        } catch (IOException | InterruptedException e) {
            // ignore
        }
    }
}
