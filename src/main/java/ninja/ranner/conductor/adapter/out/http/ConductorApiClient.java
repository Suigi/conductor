package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.adapter.OutputListener;
import ninja.ranner.conductor.adapter.OutputTracker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Optional;

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
        Command command = new Command.CreateTimer(
                timerName,
                durationSeconds);
        commandListener.emit(command);
        httpClient.sendRequest(requestFor(command));
    }

    private HttpRequest requestFor(Command command) {
        return HttpRequest.newBuilder()
                .method(
                        command.method(),
                        command.body()
                                .map(HttpRequest.BodyPublishers::ofString)
                                .orElse(HttpRequest.BodyPublishers.noBody())
                )
                .uri(uriTo(command.path()))
                .build();
    }

    private URI uriTo(String path) {
        return URI.create("%s%s".formatted(baseUrl, path));
    }

    public OutputTracker<Command> trackCommands() {
        return commandListener.track();
    }

    public sealed interface Command permits Command.CreateTimer {
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
    }

}
