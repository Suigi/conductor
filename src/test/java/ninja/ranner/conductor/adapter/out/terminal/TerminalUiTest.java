package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.adapter.OutputTracker;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
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
            Thread.startVirtualThread(tui::run);

            fixture.controls().simulateCommand("my command");

            await().untilAtomic(receivedCommand, is("my command"));
        }

        @Test
        void runReturnsWhenQuitCommandIsIssued() throws InterruptedException {
            TerminalUi.Fixture fixture = TerminalUi.createNull();
            TerminalUi tui = fixture.terminalUi();
            Thread uiThread = Thread.startVirtualThread(tui::run);

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

        private static ConditionFactory await() {
            return Awaitility.await()
                    .pollDelay(Duration.ofMillis(10))
                    .atMost(Duration.ofMillis(50));
        }
    }


}
