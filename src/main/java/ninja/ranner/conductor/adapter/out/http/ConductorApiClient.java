package ninja.ranner.conductor.adapter.out.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class ConductorApiClient {
    private final HttpClient httpClient;
    private final String baseUrl;

    ConductorApiClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public void createTimer(String timerName, int durationSeconds) throws IOException, InterruptedException {
        httpClient.sendRequest(HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                                {
                                  "name": "%s",
                                  "durationSeconds": %d
                                }
                                """.formatted(timerName, durationSeconds)
                ))
                .uri(uriTo("/timers"))
                .build());
    }

    private URI uriTo(String path) {
        return URI.create("%s%s".formatted(baseUrl, path));
    }
}
