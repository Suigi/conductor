package ninja.ranner.conductor.adapter.out.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lines {
    private final List<String> lines;

    public Lines(List<String> lines) {
        this.lines = lines;
    }

    public static Lines of(String... lines) {
        return new Lines(Arrays.stream(lines)
                .collect(Collectors.toList()));
    }

    public void append(String... newLines) {
        this.lines.addAll(Arrays.stream(newLines).toList());
    }

    public Stream<String> all() {
        return lines.stream();
    }

    public int size() {
        return lines.size();
    }
}
