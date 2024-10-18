package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.domain.RemoteTimer;
import ninja.ranner.conductor.domain.Timer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

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

    @Test
    void rendersRemoteTimer() {
        RemoteTimer timer = new RemoteTimer(
                "A_Timer",
                Duration.of(5, ChronoUnit.MINUTES),
                RemoteTimer.State.Paused,
                List.of("Robin", "Jake", "Sarah")
        );
        TimerTransformer transformer = new TimerTransformer(null);

        Lines lines = transformer.transform(timer);

        assertThat(lines.all())
                .containsExactly(
                        "Time:        05:00 | Paused",
                        "",
                        "Navigator:   Robin",
                        "Driver:      Jake",
                        "Next Driver: Sarah"
                );
    }

    @Test
    void rendersRemoteTimerWithoutParticipants() {
        RemoteTimer timer = new RemoteTimer(
                "A_Timer",
                Duration.of(123, ChronoUnit.SECONDS),
                RemoteTimer.State.Paused,
                Collections.emptyList()
        );
        TimerTransformer transformer = new TimerTransformer(null);

        Lines lines = transformer.transform(timer);

        assertThat(lines.all())
                .containsExactly(
                        "Time:        02:03 | Paused", ""
                );
    }

    @Test
    void rendersTurnIsUpMessageWhenDurationIsZero() {
        RemoteTimer timer = new RemoteTimer(
                "A_Timer",
                Duration.ZERO,
                RemoteTimer.State.Finished,
                Collections.emptyList()
        );
        TimerTransformer transformer = new TimerTransformer(null);

        Lines lines = transformer.transform(timer);

        assertThat(lines.all())
                .containsExactly(
                        "Time:        Turn is up!", ""
                );
    }
}
