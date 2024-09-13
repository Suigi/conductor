package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.Timer;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;

public class PrinterTest {

    @Test
    void withTimerOf0Seconds_printsTurnIsUp() throws Exception {
        Printer printer = new Printer();

        String output = tapSystemOut(() ->
                printer.print(new Timer(0)));

        assertThat(output)
                .isEqualTo("Turn is up!\n");
    }

    @Test
    void withTimerOf1Second_prints_00_01() throws Exception {
        Printer printer = new Printer();

        String output = tapSystemOut(() ->
                printer.print(new Timer(1)));

        assertThat(output)
                .isEqualTo("Time left: 00:01\n");
    }

    @Test
    void withTimerOf90Seconds_prints_01_30() throws Exception {
        Printer printer = new Printer();

        String output = tapSystemOut(() ->
                printer.print(new Timer(90)));

        assertThat(output)
                .isEqualTo("Time left: 01:30\n");
    }
}
