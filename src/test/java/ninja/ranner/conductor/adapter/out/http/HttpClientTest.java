package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.adapter.OutputTracker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientTest {

    private MockWebServer server;

    @Nested
    class RealHttpClient {

        @Test
        void clientMakesGetRequest() throws Exception {
            server.enqueue(new MockResponse());

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri((server.url("/get-path").uri()))
                    .build();
            HttpClient httpClient = HttpClient.create();

            httpClient.sendRequest(request);

            RecordedRequest recordedRequest = server.takeRequest(1, TimeUnit.SECONDS);
            assertThat(recordedRequest)
                    .isNotNull();
            assertThat(recordedRequest.getMethod())
                    .isEqualTo("GET");
            assertThat(recordedRequest.getBody().readUtf8())
                    .isEmpty();
            assertThat(recordedRequest.getPath())
                    .isEqualTo("/get-path");
        }

        @Test
        void clientMakesPostRequest() throws Exception {
            server.enqueue(new MockResponse());

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("my request body"))
                    .uri((server.url("/post-request").uri()))
                    .build();
            HttpClient httpClient = HttpClient.create();

            httpClient.sendRequest(request);

            RecordedRequest recordedRequest = server.takeRequest(1, TimeUnit.SECONDS);
            assertThat(recordedRequest)
                    .isNotNull();
            assertThat(recordedRequest.getMethod())
                    .isEqualTo("POST");
            assertThat(recordedRequest.getBody().readUtf8())
                    .isEqualTo("my request body");
            assertThat(recordedRequest.getPath())
                    .isEqualTo("/post-request");
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
        void tracksSentRequests() throws Exception {
            HttpClient httpClient = HttpClient.createNull();
            OutputTracker<HttpClient.Request> trackedRequests = httpClient.trackRequests();

            httpClient.sendRequest(HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/get-path").uri())
                    .build());
            httpClient.sendRequest(HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("my request body"))
                    .uri(server.url("/post-path").uri())
                    .build());

            assertThat(trackedRequests.all())
                    .containsExactly(
                            new HttpClient.Request(
                                    "GET",
                                    ""
                            ),
                            new HttpClient.Request(
                                    "POST",
                                    "my request body"
                            ));
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
