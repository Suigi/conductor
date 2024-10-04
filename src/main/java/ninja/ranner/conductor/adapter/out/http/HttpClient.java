package ninja.ranner.conductor.adapter.out.http;

import ninja.ranner.conductor.adapter.OutputListener;
import ninja.ranner.conductor.adapter.OutputTracker;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Function;

public class HttpClient {
    private final AnHttpClient httpClient;
    private final OutputListener<Request> requestListener = new OutputListener<>();

    private HttpClient(AnHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static HttpClient create() {
        return new HttpClient(new WrappedHttpClient(java.net.http.HttpClient.newHttpClient()));
    }

    public Response<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        StringSubscriber requestBodySubscriber = new StringSubscriber(request);

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        requestListener.emit(new Request(
                request.method(),
                request.uri(),
                requestBodySubscriber.body()));
        return new Response<>(
                httpResponse.statusCode(),
                httpResponse.body()
        );
    }

    public OutputTracker<Request> trackRequests() {
        return requestListener.track();
    }

    public record Request(String method, URI uri, String body) {
    }

    public record Response<T>(int statusCode, T body) {

        public static <T> Response<T> ok(T body) {
            return new Response<>(200, body);
        }

        public static <T> Response<T> status(int statusCode, Class<T> bodyType) {
            return new Response<>(statusCode, null);
        }

    }

    private static class StringSubscriber implements Flow.Subscriber<ByteBuffer> {
        private final StringBuilder requestBody = new StringBuilder();

        public StringSubscriber(HttpRequest request) {
            request.bodyPublisher().ifPresent(p -> p.subscribe(this));
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            requestBody.append(StandardCharsets.UTF_8.decode(item));
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }

        public String body() {
            return requestBody.toString();
        }
    }

    /// ~~~ Embedded Stub below ~~~

    public static HttpClient createNull() {
        return createNull(Function.identity());
    }

    public static HttpClient createNull(Function<Config, Config> configure) {
        Config config = configure.apply(new Config());
        return new HttpClient(new StubHttpClient(config.configuredResponse));
    }

    public static class Config {
        private Response<String> configuredResponse = Response.ok("DEFAULT RESPONSE BODY");

        public Config respondingWith(Response<String> configuredResponse) {
            this.configuredResponse = configuredResponse;
            return this;
        }
    }

    interface AnHttpClient {
        <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> inputStreamBodyHandler) throws IOException, InterruptedException;
    }

    record WrappedHttpClient(java.net.http.HttpClient httpClient) implements AnHttpClient {
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
                    return configuredResponse.statusCode();
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
                public java.net.http.HttpClient.Version version() {
                    return java.net.http.HttpClient.Version.HTTP_1_1;
                }
            };
        }
    }

}
