package ninja.ranner.conductor.adapter.out.http;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    private MockWebServer server;

    @Nested
    class RealHttpClient {

        @Test
        void clientMakesGetRequest() throws Exception {
            server.enqueue(new MockResponse());

            HttpRequest request = HttpRequest.newBuilder()
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri((server.url("/hello").uri()))
                    .build();
            HttpClient httpClient = HttpClient.create();

            httpClient.sendRequest(request);

            RecordedRequest recordedRequest = server.takeRequest(1, TimeUnit.SECONDS);
            assertThat(recordedRequest)
                    .isNotNull();
        }

        @Test
        void clientReadsResponseBody() throws Exception {
            server.enqueue(new MockResponse().setBody("my response body"));

            HttpRequest request = HttpRequest.newBuilder()
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri((server.url("/hello").uri()))
                    .build();
            HttpClient httpClient = HttpClient.create();

            HttpClient.Response<String> response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("my response body");
        }

    }

    @Nested
    class NulledHttpClient {

        @Test
        void doesNotSendHttpRequest() throws Exception {
            HttpClient httpClient = HttpClient.createNull();

            httpClient.sendRequest(HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build());

            assertThat(server.getRequestCount())
                    .isZero();
        }

        @Test
        void whenNoResponseConfigured_returnsDefaultResponse() throws Exception {
            HttpClient httpClient = HttpClient.createNull();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build();
            var response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("DEFAULT RESPONSE BODY");
        }

        @Test
        void returnsConfiguredResponseBody() throws Exception {
            HttpClient httpClient = HttpClient.createNull(c -> c
                    .respondingWith(HttpClient.Response.ok("my configured response body")));

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build();
            var response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("my configured response body");
        }

        @Test
        void returnsConfiguredResponseStatus() throws Exception {
            HttpClient httpClient = HttpClient.createNull(c -> c
                    .respondingWith(HttpClient.Response.status(404, String.class)));

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build();
            var response = httpClient.sendRequest(request);

            assertThat(response.statusCode())
                    .isEqualTo(404);
        }

        @Test
        @Disabled("test list")
        void returnsConfiguredResponseHeaders() {
        }

        @Test
        @Disabled("test list")
        void tracksSentRequests() {
        }

    }


    @BeforeEach
    void setUp() {
        server = new MockWebServer();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
    }

}
