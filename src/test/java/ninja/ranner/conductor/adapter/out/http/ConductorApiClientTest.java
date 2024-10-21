package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.domain.RemoteTimer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

        apiClient.startTimer("timer name");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/timer%20name/start"),
                        ""
                ));
    }

    @Test
    void pauseTimerSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.pauseTimer("timer name");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/timer%20name/pause"),
                        ""
                ));
    }

    @Test
    void nextTurnSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.nextTurn("timer name");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/timer%20name/next_turn"),
                        ""
                ));
    }

    @Test
    void updateParticipantsSendsPostRequest() throws Exception {
        HttpClient httpClient = HttpClient.createNull();
        var trackedRequests = httpClient.trackRequests();
        ConductorApiClient apiClient = new ConductorApiClient(httpClient, "https://conductor-api.example.com");

        apiClient.updateParticipants("timer name", List.of("Amy", "Mia"));

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "POST",
                        URI.create("https://conductor-api.example.com/timers/timer%20name/participants"),
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

        apiClient.fetchTimer("timer name");

        assertThat(trackedRequests.single())
                .isEqualTo(new HttpClient.Request(
                        "GET",
                        URI.create("https://conductor-api.example.com/timers/timer%20name"),
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

    @Nested
    class ConfigurableResponses {

        @Test
        void fetch_withNoConfiguredResponse_returnsDefaultResponse() throws IOException, InterruptedException {
            ConductorApiClient apiClient = ConductorApiClient.createNull();

            Optional<RemoteTimer> timer = apiClient.fetchTimer("any_timer");

            assertThat(timer)
                    .contains(new RemoteTimer("DEFAULT TIMER",
                            Duration.ofMinutes(5),
                            RemoteTimer.State.Waiting,
                            List.of("DEFAULT PARTICIPANT")));
        }

        @Test
        void fetch_withConfiguredResponse_returnsConfiguredRemoteTimer() throws IOException, InterruptedException {
            RemoteTimer remoteTimer = new RemoteTimer(
                    "remote timer",
                    Duration.of(14, ChronoUnit.SECONDS),
                    RemoteTimer.State.Running,
                    List.of("Alpha", "Bravo")
            );
            ConductorApiClient apiClient = ConductorApiClient.createNull(c -> c.returning(remoteTimer));

            Optional<RemoteTimer> timer = apiClient.fetchTimer("IRRELEVANT_NAME");

            assertThat(timer)
                    .contains(remoteTimer);
        }

    }


}
