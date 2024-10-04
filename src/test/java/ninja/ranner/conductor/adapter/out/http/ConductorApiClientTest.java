package ninja.ranner.conductor.adapter.out.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class ConductorApiClientTest {

    @Test
    void createTimerSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient conductorApiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        conductorApiClient.createTimer("TIMER_NAME", 100);

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers"),
                        """
                                {
                                  "name": "TIMER_NAME",
                                  "durationSeconds": 100
                                }
                                """
                ));
    }

    public static class ConductorApiClient {
        private final HttpClient httpClient;
        private final String baseUrl;

        ConductorApiClient(HttpClient httpClient, String baseUrl) {
            this.httpClient = httpClient;
            this.baseUrl = baseUrl;
        }

        private void createTimer(String timerName, int durationSeconds) throws IOException, InterruptedException {
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


}
