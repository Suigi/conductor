package ninja.ranner.conductor.adapter.out.http;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class MyHttpClient {
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
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> inputStreamBodyHandler) {
            return new HttpResponse<>() {
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
                    //noinspection unchecked
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
