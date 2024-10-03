package ninja.ranner.conductor.adapter.out.http;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            MyHttpClient httpClient = MyHttpClient.create();

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
            MyHttpClient httpClient = MyHttpClient.create();

            HttpResponse<String> response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("my response body");
        }

    }

    @Nested
    class NulledHttpClient {

        @Test
        void doesNotSendHttpRequest() throws Exception {
            MyHttpClient httpClient = MyHttpClient.createNull();

            httpClient.sendRequest(HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build());

            assertThat(server.getRequestCount())
                    .isZero();
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

    public static class MyHttpClient {
        private final AnHttpClient httpClient;

        private MyHttpClient(AnHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public static MyHttpClient create() {
            return new MyHttpClient(new WrappedHttpClient(HttpClient.newHttpClient()));
        }

        public static MyHttpClient createNull() {
            return new MyHttpClient(new StubHttpClient());
        }

        private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        interface AnHttpClient {
            <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> inputStreamBodyHandler) throws IOException, InterruptedException;
        }

        record WrappedHttpClient(HttpClient httpClient) implements AnHttpClient {
            @Override
            public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
                return httpClient.send(request, bodyHandler);
            }
        }

        record StubHttpClient() implements AnHttpClient {
            @Override
            public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> inputStreamBodyHandler) {
                return null;
            }
        }


    }


}
