package ninja.ranner.conductor.adapter.out.http;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    @Test
    void clientMakesGetRequest() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse());

        HttpRequest request = HttpRequest.newBuilder()
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .uri((server.url("/hello").uri()))
                .build();
        HttpClient httpClient = HttpClient.newBuilder().build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        RecordedRequest recordedRequest = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest)
                .isNotNull();
    }

}
