package ninja.ranner.conductor.adapter.out.process;

import ninja.ranner.conductor.adapter.OutputTracker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RunnerTest {

    @Nested
    @Tag("process")
    class RealRunner {

        @Test
        void returnsProcessOutput() {
            var runner = Runner.create();

            var output = runner.execute("echo", "Hello");

            assertThat(output)
                    .isEqualTo(new Runner.RunResult(0, "Hello"));
        }

        @Test
        void returnsExitCode() {
            var runner = Runner.create();

            var output = runner.execute("sh",
                    "-c",
                    "echo 'My Message'; echo 'My Error' 1>&2; exit 2");

            assertThat(output)
                    .isEqualTo(new Runner.RunResult(2, "My Message", "My Error"));
        }

        @Test
        void throwsExceptionWhenExecutableCannotBeFound() {
            var runner = Runner.create();

            assertThatThrownBy(() -> runner.execute("unknown-command"))
                    .cause()
                    .isExactlyInstanceOf(IOException.class)
                    .hasMessageContaining("unknown-command");
        }
    }

    @Nested
    class NulledRunner {

        @Test
        void returnsDefaultRunResult() {
            var runner = Runner.createNull();

            var result = runner.execute("irrelevant-command");

            assertThat(result)
                    .isEqualTo(new Runner.RunResult(0, "", ""));
        }

        @Test
        void returnsConfiguredRunResult() {
            var runner = Runner.createNull(new Runner.RunResult(
                    54,
                    "Some Output",
                    "Some Error"
            ));

            var result = runner.execute("irrelevant-command");

            assertThat(result)
                    .isEqualTo(new Runner.RunResult(54, "Some Output", "Some Error"));
        }

        @Test
        void tracksRanCommands() {
            Runner runner = Runner.createNull();
            OutputTracker<String> trackedCommands = runner.trackCommands();

            runner.execute("my", "command");

            assertThat(trackedCommands.single())
                    .isEqualTo("my command");
        }
    }

}
