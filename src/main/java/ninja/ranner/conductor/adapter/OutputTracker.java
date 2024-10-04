package ninja.ranner.conductor.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class OutputTracker<T> {
    private final List<T> entries = new ArrayList<>();

    public Stream<T> all() {
        return entries.stream();
    }

    void add(T entry) {
        entries.add(entry);
    }

}
