package ninja.ranner.conductor.adapter.out.terminal;

import ninja.ranner.conductor.adapter.OutputListener;
import ninja.ranner.conductor.adapter.OutputTracker;
import org.jline.builtins.Completers;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.DumbTerminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerminalUi {
    private LineReader reader;
    private final ATerminal terminal;
    private final OutputListener<String> screenListener = new OutputListener<>();
    private Lines lines = Lines.of();
    private String footer = "";

    private TerminalUi(ATerminal terminal, Completer completer) {
        this.terminal = terminal;
        reader = this.terminal.createReader(completer);
    }

    public static TerminalUi create(List<String> completionCandidates) throws IOException {
        return new TerminalUi(new WrappedTerminal(
                TerminalBuilder.terminal()),
                new StringsCompleter(completionCandidates)
        );
    }

    public String readCommand() {
        String line;
        try {
            line = reader.readLine("> ").trim();
            render();
        } catch (UserInterruptException | EndOfFileException e) {
            line = "quit";
        }
        return line;
    }

    public String readCommand(List<String> availableCommands) {
        Completers.TreeCompleter treeCompleter = new Completers.TreeCompleter(availableCommands
                .stream()
                .map(this::commandToNode)
                .toList());
        reader = terminal.createReader(
                treeCompleter
        );
        return readCommand();
    }

    private Completers.TreeCompleter.Node commandToNode(String c) {
        if (c.contains(" ")) {
            String[] split = (c.split(" "));
            return Completers.TreeCompleter.node(
                    split[0],
                    Completers.TreeCompleter.node(split[1]));
        } else {
            return Completers.TreeCompleter.node(c);
        }
    }

    public void enterCursorAddressingMode() {
        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.flush();

        // If we don't wait here, the terminal size is wrong
        // on first render, and the prompt is not at the very
        // bottom. I'd love to know a better way to handle this.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitCursorAddressingMode() {
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.flush();
    }

    public void update(Lines lines) {
        this.lines = lines;
        screenListener.emit(lines.toString());
        render();
    }

    private void render() {
        terminal.puts(InfoCmp.Capability.clear_screen);

        lines.all().forEach(terminal::println);
        moveCursorToBottom();
        terminal.print(footer);

        if (reader.isReading()) {
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
        }
        terminal.flush();
    }

    private void moveCursorToBottom() {
        int linesToBottom = terminal.getSize().getRows() - lines.size();
        for (int i = 0; i < linesToBottom - 1; i++) {
            terminal.puts(InfoCmp.Capability.cursor_down);
        }
    }

    // ~~~ Embedded Stub below ~~~

    public static Fixture createNull() {
        return createNull(c -> c);
    }

    public static Fixture createNull(Function<Config, Config> configure) {
        Config config = configure.apply(new Config());
        return config.createFixture();
    }

    public OutputTracker<String> trackScreens() {
        return screenListener.track();
    }

    public void less(String text) {
        less("", () -> text);
    }

    public void less(String intro, Supplier<String> text) {
        var previousLines = lines;
        var previousFooter = footer;
        update(Lines.of(intro));
        footer = terminal.styledString("Press q to exit.", AttributedStyle.DEFAULT.inverse());
        update(linesWithNumbers(text.get()));
        BindingReader bindingReader = terminal.createBindingReader();
        KeyMap<String> keys = new KeyMap<>();
        keys.bind("exit-less", "q");
        while (!bindingReader.readBinding(keys).equals("exit-less")) {
            // we want to add support for scrolling with j/k here
        }
        footer = previousFooter;
        update(previousLines);
    }

    private Lines linesWithNumbers(String text) {
        Lines lines = Lines.of();
        String[] split = text.split("\n");
        for (int i = 1; i <= split.length; i++) {
            lines.append("%d: %s".formatted(i, split[i - 1]));
        }
        return lines;
    }

    public record Fixture(TerminalUi terminalUi, Controls controls, OutputTracker<String> trackedScreens) {
        public void waitForScreen() {
            while (!hasNonEmptyScreen()) {
                Thread.yield();
            }
        }

        public void waitForScreenCount(int expectedCount) {
            while (trackedScreens.all().count() < expectedCount) {
                Thread.yield();
            }
        }

        private boolean hasNonEmptyScreen() {
            return trackedScreens.hasAny() && !trackedScreens.last().isBlank();
        }
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

        public void simulateKey(String key) {
            simulatePrintWriter.print(key);
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
                TerminalUi terminalUi = new TerminalUi(new StubTerminal(
                        in,
                        this.outputStream),
                        NullCompleter.INSTANCE);
                fixture = new Fixture(
                        terminalUi,
                        new Controls(out),
                        terminalUi.trackScreens()
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

        LineReader createReader(Completer completer);

        BindingReader createBindingReader();

        void print(String text);

        String styledString(String text, AttributedStyle style);
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
        public LineReader createReader(Completer completer) {
            return LineReaderBuilder.builder()
                    .terminal(this.terminal)
                    .completer(completer)
                    .build();
        }

        @Override
        public BindingReader createBindingReader() {
            terminal.enterRawMode();
            return new BindingReader(terminal.reader());
        }

        @Override
        public void println(String line) {
            this.terminal.writer().println(line);
        }

        @Override
        public void print(String text) {
            this.terminal.writer().print(text);
        }

        @Override
        public String styledString(String text, AttributedStyle style) {
            return new AttributedString(text, style).toAnsi(terminal);
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
            if (InfoCmp.Capability.cursor_down.equals(capability)) {
                printWriter.println();
            }
        }

        @Override
        public void println(String line) {
            printWriter.println(line);
        }

        @Override
        public void print(String text) {
            printWriter.print(text);
        }

        @Override
        public String styledString(String text, AttributedStyle style) {
            return "[" + text + "]";
        }

        @Override
        public LineReader createReader(Completer completer) {
            try {
                DumbTerminal dumbTerminal = new DumbTerminal(
                        inputStream,
                        new ByteArrayOutputStream());
                return LineReaderBuilder
                        .builder()
                        .terminal(dumbTerminal)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BindingReader createBindingReader() {
            try {
                DumbTerminal dumbTerminal = new DumbTerminal(
                        inputStream,
                        new ByteArrayOutputStream());
                return new BindingReader(dumbTerminal.reader());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
