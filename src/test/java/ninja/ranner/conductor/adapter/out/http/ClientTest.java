package ninja.ranner.conductor.adapter.out.http;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

            MyHttpClient.Response<String> response = httpClient.sendRequest(request);

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

        @Test
        void returnsConfiguredResponse() throws Exception {
            MyHttpClient httpClient = MyHttpClient.createNull(c -> c
                    .respondingWith(new MyHttpClient.Response<>("my configured response body")));

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build();
            var response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("my configured response body");
        }

        @Test
        void whenNoResponseConfigured_returnsDefaultResponse() throws Exception {
            MyHttpClient httpClient = MyHttpClient.createNull();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(server.url("/").uri())
                    .build();
            var response = httpClient.sendRequest(request);

            assertThat(response.body())
                    .isEqualTo("DEFAULT RESPONSE BODY");
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

    public static class MyHttpClient {
        private final AnHttpClient httpClient;

        private MyHttpClient(AnHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public static MyHttpClient create() {
            return new MyHttpClient(new WrappedHttpClient(HttpClient.newHttpClient()));
        }

        public static MyHttpClient createNull() {
            return createNull(Function.identity());
        }

        public static MyHttpClient createNull(Function<Config, Config> configure) {
            Config config = configure.apply(new Config());
            return new MyHttpClient(new StubHttpClient(config.configuredResponse));
        }

        public Response<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Response<>(httpResponse.body());
        }

        public static class Config {
            private Response<String> configuredResponse = new Response<>("DEFAULT RESPONSE BODY");

            public Config respondingWith(Response<String> configuredResponse) {
                this.configuredResponse = configuredResponse;
                return this;
            }
        }

        public static class Response<T> {
            private final T body;

            public Response(T body) {
                this.body = body;
            }

            public T body() {
                return body;
            }
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

        record StubHttpClient(Response<?> configuredResponse) implements AnHttpClient {
            @Override
            public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> inputStreamBodyHandler) throws IOException, InterruptedException {
                return new HttpResponse<T>() {
                    @Override
                    public int statusCode() {
                        return 200;
                    }

                    @Override
                    public HttpRequest request() {
                        return request;
                    }

                    @Override
                    public Optional<HttpResponse<T>> previousResponse() {
                        return Optional.empty();
                    }

                    @Override
                    public HttpHeaders headers() {
                        return HttpHeaders.of(Collections.emptyMap(), (key, value) -> true);
                    }

                    @Override
                    public T body() {
                        return (T) configuredResponse.body();
                    }

                    @Override
                    public Optional<SSLSession> sslSession() {
                        return Optional.empty();
                    }

                    @Override
                    public URI uri() {
                        return request.uri();
                    }

                    @Override
                    public HttpClient.Version version() {
                        return HttpClient.Version.HTTP_1_1;
                    }
                };
            }
        }

    }

}
