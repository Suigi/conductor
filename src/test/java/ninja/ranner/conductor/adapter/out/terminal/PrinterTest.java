package ninja.ranner.conductor.adapter.out.terminal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrinterTest {
    @Test
    void withTimerOf0Seconds_printsEmptyString() {
        Printer printer = new Printer();

        String output = printer.print();

        assertThat(output)
               .isEqualTo("");
    }

    @Test
    void withTimerOf1Second_prints1() {
        Printer printer = new Printer();

        String output = printer.print();

        assertThat(output)
                .isEqualTo("");
    }
}
