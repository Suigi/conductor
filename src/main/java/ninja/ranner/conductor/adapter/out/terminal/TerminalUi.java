package ninja.ranner.conductor.adapter.out.terminal;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TerminalUi {
    private final LineReader reader;
    private final ATerminal terminal;
    private Lines lines = Lines.of();
    private Consumer<String> commandHandler = ignored -> {
    };

    private TerminalUi(ATerminal terminal) {
        this.terminal = terminal;
        reader = this.terminal.createReader();
    }

    public static TerminalUi create() throws IOException {
        return new TerminalUi(new WrappedTerminal(TerminalBuilder.terminal()));
    }

    public void run() {
        enterCursorAddressingMode();
        try {
            lineReaderLoop();
        } finally {
            exitCursorAddressingMode();
        }
    }

    private void lineReaderLoop() {
        String line = "";
        while (!line.equals("quit") && !line.equals("q")) {
            render();
            try {
                line = reader.readLine("> ");
            } catch (UserInterruptException | EndOfFileException e) {
                line = "quit";
            }
            commandHandler.accept(line);
        }
    }

    private void enterCursorAddressingMode() {
        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.flush();
    }

    private void exitCursorAddressingMode() {
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.flush();
    }

    public void update(Lines lines) {
        this.lines = lines;
        render();
    }

    private void render() {
        terminal.puts(InfoCmp.Capability.clear_screen);

        lines.all().forEach(terminal::println);

        int linesToBottom = terminal.getSize().getRows() - lines.size();
        for (int i = 0; i < linesToBottom - 1; i++) {
            terminal.puts(InfoCmp.Capability.cursor_down);
        }

        if (reader.isReading()) {
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
        }
        terminal.flush();
    }

    public void registerCommandHandler(Consumer<String> handler) {
        this.commandHandler = handler;
    }

    // ~~~ Embedded Stub below ~~~

    public static Fixture createNull() {
        return createNull(c -> c);
    }

    public static Fixture createNull(Function<Config, Config> configure) {
        Config config = configure.apply(new Config());
        return config.createFixture();
    }

    public record Fixture(TerminalUi terminalUi, Controls controls) {
    }

    public static class Controls {
        private final PrintWriter simulatePrintWriter;

        public Controls(OutputStream simulatedInputStream) {
            simulatePrintWriter = new PrintWriter(simulatedInputStream);
        }

        public void simulateCommand(String command) {
            simulatePrintWriter.println(command);
            simulatePrintWriter.flush();
        }
    }

    public static class Config {
        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public Config outputTo(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        private Fixture createFixture() {
            Fixture fixture;
            try {
                PipedInputStream in = new PipedInputStream();
                PipedOutputStream out = new PipedOutputStream(in);
                fixture = new Fixture(
                        new TerminalUi(new StubTerminal(
                                in,
                                this.outputStream)),
                        new Controls(out)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return fixture;
        }
    }

    private interface ATerminal {
        void println(String line);

        Size getSize();

        void flush();

        void puts(InfoCmp.Capability capability);

        LineReader createReader();
    }

    public static class WrappedTerminal implements ATerminal {
        private final Terminal terminal;

        public WrappedTerminal(Terminal terminal) {
            this.terminal = terminal;
        }

        @Override
        public Size getSize() {
            return this.terminal.getSize();
        }

        @Override
        public void flush() {
            this.terminal.flush();
        }

        @Override
        public void puts(InfoCmp.Capability capability) {
            this.terminal.puts(capability);
        }

        @Override
        public LineReader createReader() {
            return LineReaderBuilder.builder().terminal(this.terminal).build();
        }

        @Override
        public void println(String line) {
            this.terminal.writer().println(line);
        }
    }

    private static class StubTerminal implements ATerminal {

        private final InputStream inputStream;
        private final ByteArrayOutputStream outputStream;
        private final PrintWriter printWriter;

        public StubTerminal(
                InputStream inputStream,
                ByteArrayOutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            printWriter = new PrintWriter(this.outputStream);
        }

        @Override
        public Size getSize() {
            return new Size(80, 10);
        }

        @Override
        public void flush() {
            printWriter.flush();
        }

        @Override
        public void puts(InfoCmp.Capability capability) {
            if (InfoCmp.Capability.clear_screen.equals(capability)) {
                outputStream.reset();
            }
        }

        @Override
        public void println(String line) {
            printWriter.println(line);
        }

        @Override
        public LineReader createReader() {
            try {
                Terminal terminal = TerminalBuilder
                        .builder()
                        .streams(inputStream, new ByteArrayOutputStream())
                        .dumb(true)
                        .build();
                return LineReaderBuilder
                        .builder()
                        .terminal(terminal)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
