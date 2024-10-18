package ninja.ranner.conductor.application;

import ninja.ranner.conductor.adapter.OutputTracker;
import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;
import ninja.ranner.conductor.domain.RemoteTimer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootTest {

    @Test
    void onTick_fetchesRemoteTimer() throws InterruptedException {
        Scheduler scheduler = Scheduler.createNull();
        ConductorApiClient apiClient = ConductorApiClient.createNull();
        OutputTracker<ConductorApiClient.Command> trackedCommands = apiClient.trackCommands();
        TerminalUi tui = TerminalUi.createNull().terminalUi();
        Root root = new Root(scheduler, tui, apiClient, "my_timer_name");
        root.startInBackground();
        Thread.sleep(1);

        scheduler.simulateTick();

        assertThat(trackedCommands.single())
                .isEqualTo(new ConductorApiClient.Command.FetchTimer(
                        "my_timer_name"));
    }

    @Test
    void onTick_rendersRemoteTimer() throws InterruptedException {
        RemoteTimer remoteTimer = new RemoteTimer(
                "timer-name",
                Duration.ofSeconds(55),
                RemoteTimer.State.Paused,
                List.of("Anna", "Bobby")
        );
        Scheduler scheduler = Scheduler.createNull();
        ConductorApiClient apiClient = ConductorApiClient.createNull(c -> c
                .returning(remoteTimer));
        TerminalUi tui = TerminalUi.createNull().terminalUi();
        OutputTracker<String> trackedScreens = tui.trackScreens();
        Root root = new Root(scheduler, tui, apiClient, null);
        root.startInBackground();
        Thread.sleep(1);

        scheduler.simulateTick();

        assertThat(trackedScreens.all())
                .containsExactly(
                        // Clear screen
                        "",

                        // Rendered timer
                        new TimerTransformer(null)
                                .transform(remoteTimer)
                                .toString());
    }

}