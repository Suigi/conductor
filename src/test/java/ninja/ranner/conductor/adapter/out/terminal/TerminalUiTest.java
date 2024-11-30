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

    }

    @Nested
    class Less {

        @Test
        void lessRendersTextOnScreen() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));

            startLessAndWait(tuiFixture, "First line\nSecond line");
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
        void lessDisplaysTheTopOfTheContent() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));

            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);
            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               1: Line 1
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               """);
        }

        @Test
        void lessBindsJToMoveDownOneLine() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);
            simulateKeyAndWait(tuiFixture, "j");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               10: Line 10
                               """);
        }

        @Test
        void oneLineDownCommandCapsAtTheEndOfText() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);

            simulateKeyAndWait(tuiFixture, "j");
            simulateKeyAndWait(tuiFixture, "j");
            simulateKeyAndWait(tuiFixture, "j");
            simulateKeyAndWait(tuiFixture, "j");
            simulateKeyAndWait(tuiFixture, "j");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               10: Line 10
                               11: Line 11
                               12: Line 12
                               """);
        }

        @Test
        void lessBindsKToMoveUpOneLine() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);
            simulateKeyAndWait(tuiFixture, "j");
            simulateKeyAndWait(tuiFixture, "j");

            simulateKeyAndWait(tuiFixture, "k");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               10: Line 10
                               """);
        }

        @Test
        void oneLineUpCommandCapsAtTheStartOfText() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);
            simulateKeyAndWait(tuiFixture, "j");

            simulateKeyAndWait(tuiFixture, "k");
            simulateKeyAndWait(tuiFixture, "k");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               1: Line 1
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               """);
        }

        @Test
        void bindsUppercaseGToJumpToEndOfText() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         Line 12
                                         """);

            simulateKeyAndWait(tuiFixture, "G");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               10: Line 10
                               11: Line 11
                               12: Line 12
                               """);
        }

        @Test
        void jumpToEndCommandStaysOnTopIfTextFitsOnScreen() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         """);

            simulateKeyAndWait(tuiFixture, "G");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               1: Line 1
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               """);
        }

        @Test
        void bindsLowercaseGToJumpToStartOfText() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TerminalUi.Fixture tuiFixture = TerminalUi.createNull(c -> c.outputTo(outputStream));
            startLessAndWait(tuiFixture, """
                                         Line 1
                                         Line 2
                                         Line 3
                                         Line 4
                                         Line 5
                                         Line 6
                                         Line 7
                                         Line 8
                                         Line 9
                                         Line 10
                                         Line 11
                                         """);
            simulateKeyAndWait(tuiFixture, "j");

            simulateKeyAndWait(tuiFixture, "g");

            assertThat(tuiFixture.trackedScreens().last())
                    .isEqualTo("""
                               1: Line 1
                               2: Line 2
                               3: Line 3
                               4: Line 4
                               5: Line 5
                               6: Line 6
                               7: Line 7
                               8: Line 8
                               9: Line 9
                               """);
        }

        private static void startLessAndWait(TerminalUi.Fixture tuiFixture, String text) {
            Thread.startVirtualThread(() -> tuiFixture.terminalUi().less(text));
            tuiFixture.waitForScreen();
        }

        private static void simulateKeyAndWait(TerminalUi.Fixture tuiFixture, String key) {
            tuiFixture.trackedScreens().clear();
            tuiFixture.controls().simulateKey(key);
            tuiFixture.waitForScreen();
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

    }

    private static ConditionFactory await() {
        return Awaitility.await()
                .pollDelay(Duration.ofMillis(10))
                .atMost(Duration.ofMillis(50));
    }

}
