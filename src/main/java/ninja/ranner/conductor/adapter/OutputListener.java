package ninja.ranner.conductor.adapter;

import java.util.ArrayList;

public class OutputListener<T> {
    private final ArrayList<OutputTracker<T>> trackers = new ArrayList<>();

    public OutputTracker<T> track() {
        OutputTracker<T> outputTracker = new OutputTracker<>();
        trackers.add(outputTracker);
        return outputTracker;
    }

    public void emit(T entry) {
        trackers.forEach(t -> t.add(entry));
    }
}
