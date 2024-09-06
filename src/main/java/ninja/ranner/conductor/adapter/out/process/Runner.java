package ninja.ranner.conductor.adapter.out.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

public class Runner {

    private final NullableRuntime runtime;

    private Runner(NullableRuntime runtime) {
        this.runtime = runtime;
    }

    public static Runner create() {
        return new Runner(new WrappedRuntime());
    }

    public static Runner createNull() {
        return new Runner(StubRuntime.withDefaultResult());
    }

    public static Runner createNull(RunResult runResult) {
        return new Runner(StubRuntime.with(runResult));
    }

    public RunResult execute(String... command) {
        try {
            var process = runtime.exec(command);
            process.waitFor();
            return new RunResult(process.exitValue(),
                    readFrom(process.inputReader()),
                    readFrom(process.errorReader()));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFrom(BufferedReader process) throws IOException {
        String output;
        try (var inputReader = process) {
            output = inputReader.lines().collect(Collectors.joining("\n"));
        }
        return output;
    }

    public record RunResult(int exitCode, String stdout, String stderr) {
        public RunResult(int exitCode, String stdout) {
            this(exitCode, stdout, "");
        }
    }

    // ~~~ Embedded Stub below ~~~

    private interface NullableRuntime {
        NullableProcess exec(String... command) throws IOException;
    }

    private static class WrappedRuntime implements NullableRuntime {
        @Override
        public NullableProcess exec(String... command) throws IOException {
            return new WrappedProcess(Runtime.getRuntime().exec(command));
        }
    }


    private interface NullableProcess {
        void waitFor() throws InterruptedException;

        int exitValue();

        BufferedReader inputReader();

        BufferedReader errorReader();
    }

    private record WrappedProcess(Process process) implements NullableProcess {

        @Override
        public void waitFor() throws InterruptedException {
            process.waitFor();
        }

        @Override
        public int exitValue() {
            return process.exitValue();
        }

        @Override
        public BufferedReader inputReader() {
            return process.inputReader();
        }

        @Override
        public BufferedReader errorReader() {
            return process.errorReader();
        }
    }

    private record StubRuntime(RunResult configuredResult) implements NullableRuntime {

        private static StubRuntime withDefaultResult() {
            return new StubRuntime(new RunResult(0, "", ""));
        }

        private static StubRuntime with(RunResult runResult) {
            return new StubRuntime(runResult);
        }

        @Override
        public NullableProcess exec(String... command) throws IOException {
            return new StubProcess(configuredResult);
        }

    }

    private record StubProcess(RunResult configuredResult) implements NullableProcess {

        @Override
        public void waitFor() throws InterruptedException {
        }

        @Override
        public int exitValue() {
            return configuredResult.exitCode();
        }

        @Override
        public BufferedReader inputReader() {
            return new BufferedReader(new StringReader(configuredResult.stdout()));
        }

        @Override
        public BufferedReader errorReader() {
            return new BufferedReader(new StringReader(configuredResult.stderr()));
        }
    }
}
