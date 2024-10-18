package ninja.ranner.conductor.domain;

import java.time.Duration;
import java.util.List;

public record RemoteTimer(
        String name,
        Duration remaining,
        State state,
        List<String> participants
) {
    public enum State {
        Waiting,
        Running,
        Paused,
        Finished
    }
}
