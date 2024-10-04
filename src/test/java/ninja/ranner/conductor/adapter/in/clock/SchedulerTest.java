package ninja.ranner.conductor.adapter.in.clock;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

class SchedulerTest {

    @Test
    void realSchedulerExecutesCommand() {
        Scheduler scheduler = Scheduler.create(TimeUnit.MILLISECONDS);
        AtomicBoolean wasRun = new AtomicBoolean(false);
        Runnable command = () -> wasRun.set(true);

        //noinspection resource
        scheduler.start(command);

        await().untilAtomic(wasRun, is(true));
    }

    @Test
    void realSchedulerDoesNotExecuteCommandWhenStopped() throws InterruptedException {
        Scheduler scheduler = Scheduler.create(TimeUnit.MILLISECONDS);
        AtomicInteger runCounter = new AtomicInteger();
        Runnable command = runCounter::incrementAndGet;
        //noinspection resource
        scheduler.start(command);

        int currentCount = runCounter.get();
        scheduler.stop();

        Thread.sleep(40);
        assertThat(runCounter)
                .hasValue(currentCount);
    }

    @Test
    void nulledSchedulerDoesNotRunCommand() throws InterruptedException {
        var scheduler = Scheduler.createNull();
        AtomicBoolean wasRun = new AtomicBoolean(false);
        Runnable command = () -> wasRun.set(true);

        scheduler.start(command);

        Thread.sleep(10);
        assertThat(wasRun)
                .isFalse();
    }

    @Test
    void nulledScheduler_afterSimulateTick_runsCommand() {
        var scheduler = Scheduler.createNull();
        AtomicBoolean wasRun = new AtomicBoolean(false);
        Runnable command = () -> wasRun.set(true);
        scheduler.start(command);

        scheduler.simulateTick();

        assertThat(wasRun)
                .isTrue();
    }

    private static ConditionFactory await() {
        return Awaitility.await()
                .pollDelay(Duration.ofMillis(10))
                .atMost(Duration.ofMillis(50));
    }
}
