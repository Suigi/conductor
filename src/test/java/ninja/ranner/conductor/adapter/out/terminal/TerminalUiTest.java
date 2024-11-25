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

import static org.assertj.core.api.Assertions.assertThat;

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
                    .startsWith("""
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
                    .startsWith("""
                            Second Screen
                            """);
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            TerminalUi tui = tuiFixture.terminalUi();

            Thread.startVirtualThread(() -> tui.less("First line\nSecond line"));

            tuiFixture.waitForScreen();
            Awaitility.await()
                    .pollDelay(10, TimeUnit.MILLISECONDS)
                    .atMost(100, TimeUnit.MILLISECONDS)
                    .until(() -> outputStream.toString().contains("First line"));
            assertThat(outputStream.toString())
                    .isEqualTo("""
                            1: First line
                            2: Second line
                                                        
                                                        
                                                        
                                                        
                                                        
                                                        
                                                        
                            [Press q to exit.]""");
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
            assertThat(trackedScreens.last())
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
