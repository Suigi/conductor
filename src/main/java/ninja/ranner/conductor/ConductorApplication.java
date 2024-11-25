package ninja.ranner.conductor;

import ninja.ranner.conductor.application.Root;

public class ConductorApplication {

    private static final String apiBaseUrl = "http://localhost:8088";
    private static final String timerName = "Ensemble Timer Demo";

    public static void main(String[] args) throws Exception {
        Root
                .create(apiBaseUrl, timerName)
                .startInBackground()
                .join();
    }

}
