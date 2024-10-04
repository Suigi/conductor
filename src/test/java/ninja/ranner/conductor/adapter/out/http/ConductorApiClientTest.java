package ninja.ranner.conductor.adapter.out.http;

import org.junit.jupiter.api.Test;

import java.net.URI;

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

}
