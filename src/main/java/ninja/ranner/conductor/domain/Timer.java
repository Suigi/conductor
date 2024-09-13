package ninja.ranner.conductor.domain;

public class Timer {

    private int remainingSeconds;

    public Timer(int initialSeconds) {
        this.remainingSeconds = initialSeconds;
    }

    public int remainingSeconds() {
        return remainingSeconds;
    }

    public void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
    }
}
