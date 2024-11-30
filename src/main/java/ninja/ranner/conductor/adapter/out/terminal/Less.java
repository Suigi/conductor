package ninja.ranner.conductor.adapter.out.terminal;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;

class Less {
    private final BindingReader bindingReader;
    private final RenderTarget target;
    private final String text;
    private final KeyMap<String> keyMap;
    private int position = 1;

    Less(String text, BindingReader bindingReader, RenderTarget renderTarget) {
        this.bindingReader = bindingReader;
        this.target = renderTarget;
        this.keyMap = keyMap();
        this.text = text;
    }

    public void render() {
        while (true) {
            target.update(prefixLineNumbers(position, text, target.size()));
            Command command = readCommand();
            if (command.equals(Command.EXIT)) {
                break;
            }

            switch (command) {
                case LINE_DOWN -> lineDown();
                case LINE_UP -> lineUp();
                case JUMP_TO_START -> position = jumpToStart();
                case JUMP_TO_END -> jumpToEnd();
            }
        }
    }

    private Command readCommand() {
        return Command.valueOf(bindingReader.readBinding(keyMap));
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

    private int jumpToStart() {
        return 1;
    }

    private void jumpToEnd() {
        position = Math.max(1, lineCount() - target.size().getRows() + 2);
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

    public enum Command {
        EXIT,
        LINE_DOWN,
        LINE_UP,
        JUMP_TO_START,
        JUMP_TO_END
    }

    private static KeyMap<String> keyMap() {
        KeyMap<String> keys = new KeyMap<>();
        keys.bind(Command.EXIT.name(), "q");
        keys.bind(Command.LINE_DOWN.name(), "j");
        keys.bind(Command.LINE_UP.name(), "k");
        keys.bind(Command.JUMP_TO_START.name(), "g");
        keys.bind(Command.JUMP_TO_END.name(), "G");
        return keys;
    }

}
