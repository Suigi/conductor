package ninja.ranner.conductor.application;

import ninja.ranner.conductor.adapter.OutputTracker;
import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.http.ConductorApiClient;
import ninja.ranner.conductor.adapter.out.process.Runner;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;
import ninja.ranner.conductor.domain.RemoteTimer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RootTest {

    @Test
    void onTick_fetchesRemoteTimer() {
        Scheduler scheduler = Scheduler.createNull();
        ConductorApiClient apiClient = ConductorApiClient.createNull();
        OutputTracker<ConductorApiClient.Command> trackedCommands = apiClient.trackCommands();
        TerminalUi tui = TerminalUi.createNull().terminalUi();
        Root root = new Root(scheduler, tui, apiClient, "my_timer_name", Runner.createNull());
        root.startInBackground();

        scheduler.simulateTick();

        assertThat(trackedCommands.single())
                .isEqualTo(new ConductorApiClient.Command.FetchTimer(
                        "my_timer_name"));
    }

    @Test
    void onTick_rendersRemoteTimer() {
        RemoteTimer remoteTimer = new RemoteTimer(
                "timer-name",
                Duration.ofSeconds(55),
                RemoteTimer.State.Paused,
                List.of("Anna", "Bobby")
        );
        Scheduler scheduler = Scheduler.createNull();
        ConductorApiClient apiClient = ConductorApiClient.createNull(c -> c
                .returning(remoteTimer));
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        OutputTracker<String> trackedScreens = tuiFixture.trackedScreens();
        Root root = new Root(scheduler, tuiFixture.terminalUi(), apiClient, null, Runner.createNull());
        root.startInBackground();

        trackedScreens.clear();
        scheduler.simulateTick();

        tuiFixture.waitForScreen();
        assertThat(trackedScreens.last())
                .isEqualTo(
                        new TimerTransformer(null)
                                .transform(remoteTimer)
                                .toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"quit", "q"})
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    void onQuitCommand_terminatesMainThread(String simulatedCommand) throws InterruptedException {
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        Root root = new Root(Scheduler.createNull(),
                tuiFixture.terminalUi(),
                ConductorApiClient.createNull(),
                "IRRELEVANT",
                Runner.createNull());
        Thread mainThread = root.startInBackground();

        tuiFixture.controls().simulateCommand(simulatedCommand);

        mainThread.join();
    }

    @Test
    void onPauseCommand_pausesRemoteTimer() throws InterruptedException {
        ConductorApiClient apiClient = ConductorApiClient.createNull();
        var trackedCommands = apiClient.trackCommands();
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        Root root = new Root(
                Scheduler.createNull(),
                tuiFixture.terminalUi(),
                apiClient,
                "my timer",
                Runner.createNull()
        );
        Thread mainThread = root.startInBackground();

        tuiFixture.controls().simulateCommand("pause");
        tuiFixture.controls().simulateCommand("quit");

        mainThread.join();
        assertThat(trackedCommands.single())
                .isEqualTo(new ConductorApiClient.Command.PauseTimer("my timer"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("timerCommandCases")
    void onTimerCommand_sendsToRemoteTimer(String command, ConductorApiClient.Command expectedApiCommand) throws InterruptedException {
        ConductorApiClient apiClient = ConductorApiClient.createNull();
        var trackedCommands = apiClient.trackCommands();
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        Root root = new Root(
                Scheduler.createNull(),
                tuiFixture.terminalUi(),
                apiClient,
                "my timer",
                Runner.createNull()
        );
        Thread mainThread = root.startInBackground();

        tuiFixture.controls().simulateCommand(command);
        tuiFixture.controls().simulateCommand("quit");

        mainThread.join();
        assertThat(trackedCommands.single())
                .isEqualTo(expectedApiCommand);
    }

    public static Stream<Arguments> timerCommandCases() {
        return Stream.of(
                Arguments.of("start", new ConductorApiClient.Command.StartTimer("my timer")),
                Arguments.of("pause", new ConductorApiClient.Command.PauseTimer("my timer")),
                Arguments.of("rotate", new ConductorApiClient.Command.NextTurn("my timer"))
        );
    }

    @Test
    void onRunnerCommand_printsRunnerOutputToLess() {
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        Runner runner = Runner.createNull(new Runner.RunResult(0, "> Mob Status Output <"));
        Root root = new Root(
                Scheduler.createNull(),
                tuiFixture.terminalUi(),
                ConductorApiClient.createNull(),
                "IRRELEVANT",
                runner
        );
        root.startInBackground();
        assertThat(tuiFixture.trackedScreens().all())
                .containsExactly("Welcome to Conductor\n");
        tuiFixture.trackedScreens().clear();
        assertThat(tuiFixture.trackedScreens().all())
                .isEmpty();

        tuiFixture.controls().simulateCommand("mob status");

        tuiFixture.waitForScreenCount(2);
        assertThat(tuiFixture.trackedScreens().all())
                .containsExactly(
                        """
                        Running command
                          > mob status
                        """,
                        """
                        1: > Mob Status Output <
                        """
                );
        assertThat(tuiFixture.trackedScreens().last())
                .contains("> Mob Status Output <");
    }

    @ParameterizedTest
    @MethodSource("runnerCommands")
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    void onRunnerCommand_runsAssociatedShellCommand(String command, String expectedRunnerCommand) throws InterruptedException {
        TerminalUi.Fixture tuiFixture = TerminalUi.createNull();
        Runner runner = Runner.createNull();
        OutputTracker<String> trackedCommands = runner.trackCommands();
        Root root = new Root(
                Scheduler.createNull(),
                tuiFixture.terminalUi(),
                ConductorApiClient.createNull(),
                "IRRELEVANT",
                runner
        );
        Thread mainThread = root.startInBackground();

        tuiFixture.controls().simulateCommand(command);
        tuiFixture.controls().simulateKey("q");

        tuiFixture.controls().simulateCommand("quit");
        mainThread.join();
        assertThat(trackedCommands.single())
                .isEqualTo(expectedRunnerCommand);
    }

    public static Stream<Arguments> runnerCommands() {
        return Stream.of(
                // mob.sh
                Arguments.of("mob start", "mob start --include-uncommitted-changes"),
                Arguments.of("mob done", "mob done --squash-wip"),
                Arguments.of("mob status", "mob status"),
                Arguments.of("save", "mob next"),
                Arguments.of("load", "mob start"),
                // git
                Arguments.of("gs", "git -c color.status=always status"),
                Arguments.of("gss", "git -c color.status=always status --short")
        );
    }

}