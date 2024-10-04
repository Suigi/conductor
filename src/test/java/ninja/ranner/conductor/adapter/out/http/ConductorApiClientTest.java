package ninja.ranner.conductor.adapter.out.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

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

    @Test
    void startTimerSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.startTimer("TIMER_NAME");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME/start"),
                        ""
                ));
    }

    @Test
    void pauseTimerSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.pauseTimer("TIMER_NAME");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME/pause"),
                        ""
                ));
    }

    @Test
    @Disabled("test list")
    void nextTurnSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.pauseTimer("TIMER_NAME");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME/pause"),
                        ""
                ));
    }

    @Test
    void updateParticipantsSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.updateParticipants("TIMER_NAME", List.of("Amy", "Mia"));

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME/participants"),
                        """
                                {
                                    "participants": ["Amy", "Mia"]
                                }
                                """
                ));
    }

    @Test
    @Disabled("test list")
    void fetchTimerSendsGetRequest() throws Exception {
    }

    @Test
    @Disabled("test list")
    void fetchTimerReturnsTimer() throws Exception {
    }

    @Test
    @Disabled("test list")
    void fetchTimerWithUnknownTimerNameReturnsEmptyOptional() throws Exception {
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

        @Test
        void tracksStartTimerCommand() throws Exception {
            ConductorApiClient apiClient = ConductorApiClient.createNull();
            var trackedCommands = apiClient.trackCommands();

            apiClient.startTimer("some_timer");

            assertThat(trackedCommands.single())
                    .isEqualTo(new ConductorApiClient.Command.StartTimer("some_timer"));
        }
    }


}
