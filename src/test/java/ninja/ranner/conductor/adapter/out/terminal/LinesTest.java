package ninja.ranner.conductor.adapter.out.terminal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinesTest {

    @Test
    void linesOfSplitsNewlinesIntoSeparateLines() {
        Lines lines = Lines.of("First\nSecond\n");

        assertThat(lines.all())
                .containsExactly("First", "Second");
    }

    @Test
    void keepsEmptyLinesWhenSeparatingByNewline() {
        Lines lines = Lines.of("First\nSecond\n\nThird");

        assertThat(lines.all())
                .containsExactly("First", "Second", "", "Third");
    }
}