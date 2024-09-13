package ninja.ranner.conductor.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimerTest {

    @Test
    void newTimerWith10SecondsHas10SecondsLeft() {
        Timer timer = new Timer(10);

        assertThat(timer.remainingSeconds())
                .isEqualTo(10);
    }

    @Test
    void timerWith10SecondsAfterTickHas9SecondsLeft() {
        Timer timer = new Timer(10);

        timer.tick();

        assertThat(timer.remainingSeconds())
                .isEqualTo(9);
    }

    @Test
    void timerWith0SecondsAfterTickHas0SecondsLeft() {
        Timer timer = new Timer(1);
        timer.tick();

        timer.tick();

        assertThat(timer.remainingSeconds())
                .isEqualTo(0);
    }
}