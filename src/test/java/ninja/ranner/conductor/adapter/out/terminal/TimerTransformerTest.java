package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.Timer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimerTransformerTest {

    @Test
    void showsCurrentTime() {
        Timer timer = new Timer(5);
        TimerTransformer transformer = new TimerTransformer(timer);

        Lines result = transformer.transform();

        assertThat(result.all())
                .containsExactly("5");
    }

    @Test
    void showsTimerIsUpWhenOutOfTime() {
        Timer timer = new Timer(0);
        TimerTransformer transformer = new TimerTransformer(timer);

        Lines result = transformer.transform();

        assertThat(result.all())
                .containsExactly("Timer's up!");
    }
}
