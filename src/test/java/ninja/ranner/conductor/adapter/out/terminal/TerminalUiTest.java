package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.adapter.OutputTracker;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

public class TerminalUiTest {

    @Nested
    class NulledTerminalUi {

        @Test
        void writesOutputToConfiguredStream() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi tui = TerminalUi
                    .createNull(c -> c.outputTo(outputStream))
                    .terminalUi();

            tui.update(Lines.of(
                    "First line",
                    "",
                    "Third line"
            ));

            assertThat(outputStream.toString())
                    .isEqualTo("""
                            First line
                                                        
                            Third line
                            """);
        }

        @Test
        void clearsScreenBetweenUpdates() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi tui = TerminalUi.createNull(c -> c.outputTo(outputStream)).terminalUi();
            tui.update(Lines.of("First Screen"));

            tui.update(Lines.of("Second Screen"));

            assertThat(outputStream.toString())
                    .isEqualTo("""
                            Second Screen
                            """);
        }

        @Test
        void emitsSimulatedCommand() {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            TerminalUi tui = fixture.terminalUi();
            AtomicReference<String> receivedCommand = new AtomicReference<>("");
            tui.registerCommandHandler(receivedCommand::set);
            fixture.startAsync();

            fixture.controls().simulateCommand("my command");

            await().untilAtomic(receivedCommand, is("my command"));
        }

        @Test
        void runReturnsWhenQuitCommandIsIssued() throws InterruptedException {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            Thread uiThread = fixture.startAsync();

            fixture.controls().simulateCommand("quit");

            uiThread.join(Duration.ofMillis(10));
        }

        @Test
        void tracksUpdatedScreens() {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            OutputTracker<String> trackedScreens = fixture.terminalUi().trackScreens();

            fixture.terminalUi().update(Lines.of("First Line", "", "Third Line"));

            assertThat(trackedScreens.single())
                    .isEqualTo("""
                            First Line
                                                       
                            Third Line
                            """);
        }

        @Test
        void lessRendersTextOnScreen() {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            TerminalUi tui = fixture.terminalUi();
            OutputTracker<String> trackedScreens = tui.trackScreens();
            fixture.startAsync();

            trackedScreens.clear();
            Thread.startVirtualThread(() -> tui.less("First line\nSecond line"));

            await().until(trackedScreens::hasAny);
            assertThat(trackedScreens.single())
                    .isEqualTo("""
                            1: First line
                            2: Second line
                            """);
        }

        @Test
        void lessExitsWhenPressingQ() {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            TerminalUi tui = fixture.terminalUi();
            OutputTracker<String> trackedScreens = tui.trackScreens();
            tui.update(Lines.of("Previous", "Text"));

            Thread.startVirtualThread(() -> tui.less("Some Text"));
            await().until(trackedScreens::hasAny);
            trackedScreens.clear();
            fixture.controls().simulateKey("q");

            await().until(trackedScreens::hasAny);
            assertThat(trackedScreens.single())
                    .isEqualTo("""
                            Previous
                            Text
                            """);
        }

        @Test()
        @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
        void afterLessExitsOriginalKeybindingsAreRestored() throws InterruptedException {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            TerminalUi tui = fixture.terminalUi();
            Thread lessThread = Thread.startVirtualThread(() -> tui.less("Some Text"));

            fixture.controls().simulateKey("q");
            fixture.controls().simulateCommand("quit");

            lessThread.join();
        }

        private static ConditionFactory await() {
            return Awaitility.await()
                    .pollDelay(Duration.ofMillis(10))
                    .atMost(Duration.ofMillis(50));
        }
    }


}
