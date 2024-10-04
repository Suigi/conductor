package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.adapter.OutputListener;
import ninja.ranner.conductor.adapter.OutputTracker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConductorApiClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final OutputListener<Command> commandListener = new OutputListener<>();

    ConductorApiClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public static ConductorApiClient createNull() {
        return new ConductorApiClient(
                HttpClient.createNull(),
                "https://conductor-api.example.com");
    }

    public void createTimer(String timerName, int durationSeconds) throws IOException, InterruptedException {
        send(new Command.CreateTimer(
                timerName,
                durationSeconds));
    }

    public void startTimer(String timerName) throws IOException, InterruptedException {
        send(new Command.StartTimer(timerName));
    }

    public void pauseTimer(String timerName) throws IOException, InterruptedException {
        send(new Command.PauseTimer(timerName));
    }

    public void updateParticipants(String timerName, List<String> participants) throws IOException, InterruptedException {
        send(new Command.UpdateParticipants(timerName, participants));
    }

    private void send(Command command) throws IOException, InterruptedException {
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
        httpClient.sendRequest(request);
    }

    public OutputTracker<Command> trackCommands() {
        return commandListener.track();
    }

    public sealed interface Command permits
            Command.CreateTimer,
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
                return "/timers/" + timerName + "/start";
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
                return "/timers/" + timerName + "/pause";
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
                return "/timers/" + timerName + "/participants";
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
    }

}
