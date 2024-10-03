package ninja.ranner.conductor.adapter.in.clock;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.core.Is.is;

class SchedulerTest {

    @Test
    void realSchedulerExecutesCommand() {
        Scheduler scheduler = new Scheduler(TimeUnit.MILLISECONDS);
        AtomicBoolean toggle = new AtomicBoolean(false);
        Runnable command = () -> toggle.set(true);

        //noinspection resource
        scheduler.oncePerSecond(command);

        await().untilAtomic(toggle, is(true));
    }

    @Test
    void nulledSchedulerRunsSimulatedCommand() {
//        var scheduler = Scheduler.createNull();
//        AtomicBoolean toggle = new AtomicBoolean(false);
//        Runnable command = () -> toggle.set(true);
//        scheduler.oncePerSecond(command);
//
//        scheduler.simulateTick();
//
//        assertThat(toggle)
//                .isTrue();
    }

    private static ConditionFactory await() {
        return Awaitility.await()
                .pollDelay(Duration.ofMillis(10))
                .atMost(Duration.ofMillis(50));
    }
}