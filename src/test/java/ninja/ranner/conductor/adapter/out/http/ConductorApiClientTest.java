package ninja.ranner.conductor.adapter.out.http;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ConductorApiClientTest {

    @Test
    void createTimerSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.createTimer("TIMER_NAME", 100);

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

    @Nested
    class RequestTracking {

        @Test
        void tracksCreateTimerCommand() throws Exception {
            ConductorApiClient apiClient = ConductorApiClient.createNull();
            var trackedCommands = apiClient.trackCommands();

            apiClient.createTimer("my_timer", 12);

            assertThat(trackedCommands.single())
                    .isEqualTo(new ConductorApiClient.Command.CreateTimer("my_timer", 12));
        }

    }


}
