package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.domain.RemoteTimer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConductorApiClientTest {

    private static final HttpClient.Response<String> VALID_TIMER_RESPONSE = new HttpClient.Response<>(200, """
            {
              "name": "irrelevant_timer",
              "status": "Waiting",
              "remainingSeconds": 999,
              "participants": []
            }
            """);

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
    void nextTurnSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.nextTurn("TIMER_NAME");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME/next_turn"),
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
    void fetchTimerSendsGetRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull(c -> c.respondingWith(VALID_TIMER_RESPONSE));
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.fetchTimer("TIMER_NAME");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "GET",
                        URI.create("https://conductor-api.example.com/timers/TIMER_NAME"),
                        ""
                ));
    }

    @Test
    void fetchTimerReturnsTimer() throws Exception {
        HttpClient httpClient = HttpClient.createNull(c -> c
                .respondingWith(new HttpClient.Response<>(200, """
                        {
                          "name": "my_timer",
                          "status": "Running",
                          "remainingSeconds": 32,
                          "participants": [
                            "Joe",
                            "Abby"
                          ]
                        }
                        """)));
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        Optional<RemoteTimer> timer = apiClient.fetchTimer("TIMER_NAME");

        assertThat(timer)
                .contains(new RemoteTimer(
                        "my_timer",
                        Duration.of(32, ChronoUnit.SECONDS),
                        RemoteTimer.State.Running,
                        List.of("Joe", "Abby")
                ));
    }

    @Test
    void fetchTimerWithUnknownTimerNameReturnsEmptyOptional() throws Exception {
        HttpClient httpClient = HttpClient.createNull(c -> c
                .respondingWith(new HttpClient.Response<>(404, "")));
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        Optional<RemoteTimer> timer = apiClient.fetchTimer("TIMER_NAME");

        assertThat(timer)
                .isEmpty();
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
