package ninja.ranner.conductor.adapter.out.terminal;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;

import java.util.Arrays;
import java.util.function.Consumer;

class Less {
    private final BindingReader bindingReader;
    private final RenderTarget target;
    private final String text;
    private int position = 1;
    private boolean keepRendering = true;

    Less(String text, BindingReader bindingReader, RenderTarget renderTarget) {
        this.bindingReader = bindingReader;
        this.target = renderTarget;
        this.text = text;
    }

    public void render() {
        while (keepRendering) {
            target.update(prefixLineNumbers(position, text, target.size()));
            Command.read(bindingReader).handle(this);
        }
    }

    private void lineUp() {
        if (position > 1) {
            position--;
        }
    }

    private void lineDown() {
        if (position < (lineCount() - target.size().getRows() + 2)) {
            position++;
        }
    }

    private void jumpToStart() {
        position = 1;
    }

    private void jumpToEnd() {
        position = Math.max(1, lineCount() - target.size().getRows() + 2);
    }

    private void exit() {
        keepRendering = false;
    }

    private int lineCount() {
        return text.split("\n").length;
    }

    private static Lines prefixLineNumbers(int start, String text, Size size) {
        Lines lines = Lines.of();
        String[] split = text.split("\n");
        int end = Math.min(split.length, start + size.getRows() - 2);
        for (int i = start; i <= end; i++) {
            lines.append("%d: %s".formatted(i, split[i - 1]));
        }
        return lines;
    }

    private enum Command {

        EXIT("q", Less::exit),
        LINE_DOWN("j", Less::lineDown),
        LINE_UP("k", Less::lineUp),
        JUMP_TO_START("g", Less::jumpToStart),
        JUMP_TO_END("G", Less::jumpToEnd);

        private static final KeyMap<String> keyMap = keyMap();

        private final String key;
        private final Consumer<Less> handler;

        Command(String key, Consumer<Less> handler) {
            this.key = key;
            this.handler = handler;
        }

        private static KeyMap<String> keyMap() {
            KeyMap<String> keys = new KeyMap<>();
            Arrays.stream(values()).forEach(c -> keys.bind(c.name(), c.key));
            return keys;
        }

        private static Command read(BindingReader reader) {
            return valueOf(reader.readBinding(keyMap));
        }

        private void handle(Less less) {
            handler.accept(less);
        }
    }

}
