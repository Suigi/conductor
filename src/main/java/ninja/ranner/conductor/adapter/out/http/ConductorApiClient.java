package ninja.ranner.conductor.adapter.out.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ninja.ranner.conductor.adapter.OutputListener;
import ninja.ranner.conductor.adapter.OutputTracker;
import ninja.ranner.conductor.domain.RemoteTimer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConductorApiClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final OutputListener<Command> commandListener = new OutputListener<>();

    ConductorApiClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public static ConductorApiClient create(String baseUrl) {
        return new ConductorApiClient(HttpClient.create(), baseUrl);
    }

    public static ConductorApiClient createNull() {
        return createNull(Function.identity());
    }

    public static ConductorApiClient createNull(Function<Config, Config> configure) {
        Config config = configure.apply(new Config());
        return new ConductorApiClient(
                config.httpClient(),
                "https://conductor-api.example.com");
    }

    public static class Config {

        private RemoteTimer remoteTimer = new RemoteTimer(
                "DEFAULT TIMER",
                Duration.ofMinutes(5),
                RemoteTimer.State.Waiting,
                List.of("DEFAULT PARTICIPANT")
        );

        public Config returning(RemoteTimer remoteTimer) {
            this.remoteTimer = remoteTimer;
            return this;
        }

        HttpClient httpClient() {
            String responseBody = """
                    {
                        "name": "%s",
                        "status": "%s",
                        "remainingSeconds": %d,
                        "participants": [%s]
                    }
                    """.formatted(
                    remoteTimer.name(),
                    remoteTimer.state(),
                    remoteTimer.remaining().getSeconds(),
                    remoteTimer.participants()
                            .stream()
                            .map("\"%s\""::formatted)
                            .collect(Collectors.joining(", ")));
            return HttpClient.createNull(c -> c
                    .respondingWith(new HttpClient.Response<>(
                            200,
                            responseBody)
                    )
            );
        }

    }


    public void createTimer(String timerName, int durationSeconds) throws IOException, InterruptedException {
        send(new Command.CreateTimer(
                timerName,
                durationSeconds));
    }

    public Optional<RemoteTimer> fetchTimer(String timerName) throws IOException, InterruptedException {
        HttpClient.Response<String> response = send(new Command.FetchTimer(timerName));

        if (response.statusCode() == 404) {
            return Optional.empty();
        }

        Timer timer = parseOrFail(response);

        return Optional.of(new RemoteTimer(
                timer.name,
                Duration.of(timer.remainingSeconds, ChronoUnit.SECONDS),
                toDomainState(timer.status),
                timer.participants()
        ));
    }

    private Timer parseOrFail(HttpClient.Response<String> response) {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        Timer timer;
        try {
            timer = jsonMapper.readValue(response.body(), Timer.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON response\n\n%s".formatted(response.body()), e);
        }
        return timer;
    }

    private RemoteTimer.State toDomainState(String status) {
        return switch (status) {
            case "Waiting" -> RemoteTimer.State.Waiting;
            case "Running" -> RemoteTimer.State.Running;
            case "Paused" -> RemoteTimer.State.Paused;
            case "Finished" -> RemoteTimer.State.Finished;
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }

    public void startTimer(String timerName) throws IOException, InterruptedException {
        send(new Command.StartTimer(timerName));
    }

    public void pauseTimer(String timerName) throws IOException, InterruptedException {
        send(new Command.PauseTimer(timerName));
    }

    public void nextTurn(String timerName) throws IOException, InterruptedException {
        send(new Command.NextTurn(timerName));
    }

    public void updateParticipants(String timerName, List<String> participants) throws IOException, InterruptedException {
        send(new Command.UpdateParticipants(timerName, participants));
    }

    private HttpClient.Response<String> send(Command command) throws IOException, InterruptedException {
        commandListener.emit(command);
        HttpRequest request = HttpRequest.newBuilder()
                .method(
                        command.method(),
                        command.body()
                                .map(HttpRequest.BodyPublishers::ofString)
                                .orElse(HttpRequest.BodyPublishers.noBody())
                )
                .uri(URI.create("%s%s".formatted(baseUrl, command.path())))
                .build();
        return httpClient.sendRequest(request);
    }

    public OutputTracker<Command> trackCommands() {
        return commandListener.track();
    }

    public sealed interface Command permits Command.CreateTimer,
            Command.FetchTimer,
            Command.NextTurn,
            Command.PauseTimer,
            Command.StartTimer,
            Command.UpdateParticipants {

        String method();

        String path();

        Optional<String> body();

        record CreateTimer(String name, int durationSeconds) implements Command {
            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String path() {
                return "/timers";
            }

            @Override
            public Optional<String> body() {
                return Optional.of("""
                        {
                          "name": "%s",
                          "durationSeconds": %d
                        }
                        """.formatted(name, durationSeconds)
                );
            }
        }

        record StartTimer(String timerName) implements Command {
            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String path() {
                return buildPath("timers", timerName, "start");
            }

            @Override
            public Optional<String> body() {
                return Optional.empty();
            }
        }

        record PauseTimer(String timerName) implements Command {
            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String path() {
                return buildPath("timers", timerName, "pause");
            }

            @Override
            public Optional<String> body() {
                return Optional.empty();
            }
        }

        record UpdateParticipants(String timerName, List<String> participants) implements Command {
            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String path() {
                return buildPath("timers", timerName, "participants");
            }

            @Override
            public Optional<String> body() {
                return Optional.of("""
                        {
                            "participants": [%s]
                        }
                        """.formatted(participants
                        .stream()
                        .map("\"%s\""::formatted)
                        .collect(Collectors.joining(", "))));
            }
        }

        record FetchTimer(String timerName) implements Command {

            @Override
            public String method() {
                return "GET";
            }

            @Override
            public String path() {
                return buildPath("timers", timerName);
            }

            @Override
            public Optional<String> body() {
                return Optional.empty();
            }
        }

        record NextTurn(String timerName) implements Command {

            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String path() {
                return buildPath("timers", timerName, "next_turn");
            }

            @Override
            public Optional<String> body() {
                return Optional.empty();
            }
        }
    }

    public static String buildPath(String... segments) {
        try {
            URI uri = new URI("http", "example.com", "/" + String.join("/", segments), "");
            return uri.getRawPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public record Timer(
            String name,
            String status,
            int remainingSeconds,
            List<String> participants
    ) {
    }

}
