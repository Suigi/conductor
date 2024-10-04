package ninja.ranner.conductor.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class OutputTracker<T> {
    private final List<T> entries = new ArrayList<>();

    void add(T entry) {
        entries.add(entry);
    }

    public Stream<T> all() {
        return entries.stream();
    }

    public T single() {
        if (entries.isEmpty()) {
            throw new IllegalStateException("Expected output to have a single element, but output was empty");
        }
        if (entries.size() > 1) {
            throw new IllegalStateException("Expected output to have a single element, but found: " + entries.size());
        }
        return entries.getFirst();
    }

}
