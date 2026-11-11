package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class HttpJobPostingPageFetcher implements JobPostingPageFetcher {

    private static final int MAX_CONTENT_BYTES = 2 * 1024 * 1024;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final JobPostingUrlPolicy urlPolicy;
    private final JobPostingHostRateLimiter rateLimiter;
    private final HttpClient httpClient;
    private final Duration requestTimeout;

    @Autowired
    public HttpJobPostingPageFetcher(JobPostingUrlPolicy urlPolicy,
                                     JobPostingHostRateLimiter rateLimiter) {
        this(urlPolicy, rateLimiter, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build(), REQUEST_TIMEOUT);
    }

    HttpJobPostingPageFetcher(JobPostingUrlPolicy urlPolicy, JobPostingHostRateLimiter rateLimiter,
                              HttpClient httpClient, Duration requestTimeout) {
        this.urlPolicy = urlPolicy;
        this.rateLimiter = rateLimiter;
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public FetchedJobPostingPage fetch(String originalUrl) {
        URI uri = urlPolicy.validate(originalUrl);
        rateLimiter.check(uri);
        urlPolicy.requirePublicAddress(uri);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("User-Agent", "HankkeutLocal/1.0")
                .GET()
                .build();

        var pending = httpClient.sendAsync(request, info -> {
            if (info.statusCode() < 200 || info.statusCode() >= 300) {
                throw fetchFailed("채용공고 페이지가 HTTP " + info.statusCode() + "을 반환했습니다.");
            }
            if (!isHtml(info.headers().firstValue("Content-Type").orElse(""))) {
                throw fetchFailed("HTML 채용공고 페이지만 가져올 수 있습니다.");
            }
            if (info.headers().firstValueAsLong("Content-Length").orElse(0) > MAX_CONTENT_BYTES) {
                throw bodyTooLarge();
            }
            return new LimitedBodySubscriber();
        });
        try {
            HttpResponse<byte[]> response = pending.get(requestTimeout.toNanos(), TimeUnit.NANOSECONDS);
            String contentType = response.headers().firstValue("Content-Type").orElseThrow();
            return new FetchedJobPostingPage(uri, contentType, response.body());
        } catch (TimeoutException exception) {
            pending.cancel(true);
            throw requestTimedOut();
        } catch (InterruptedException exception) {
            pending.cancel(true);
            Thread.currentThread().interrupt();
            throw fetchFailed("채용공고 가져오기가 중단되었습니다.");
        } catch (ExecutionException exception) {
            for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof ApiException rejected) {
                    throw rejected;
                }
                if (cause instanceof HttpTimeoutException) {
                    throw requestTimedOut();
                }
            }
            throw fetchFailed("채용공고 페이지에 연결할 수 없습니다.");
        }
    }

    private boolean isHtml(String contentType) {
        String mimeType = contentType.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        return mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml");
    }

    private static ApiException requestTimedOut() {
        return fetchFailed("채용공고 페이지 응답 시간이 초과되었습니다.");
    }

    private static ApiException bodyTooLarge() {
        return fetchFailed("채용공고 페이지 크기가 2MB를 초과합니다.");
    }

    private static ApiException fetchFailed(String message) {
        return new ApiException(HttpStatus.BAD_GATEWAY, "JOB_FETCH_FAILED", message);
    }

    private static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private Flow.Subscription subscription;

        @Override
        public CompletionStage<byte[]> getBody() {
            return result;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            for (ByteBuffer buffer : buffers) {
                int size = buffer.remaining();
                if (size > MAX_CONTENT_BYTES - body.size()) {
                    result.completeExceptionally(bodyTooLarge());
                    subscription.cancel();
                    return;
                }
                byte[] chunk = new byte[size];
                buffer.get(chunk);
                body.writeBytes(chunk);
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable error) {
            result.completeExceptionally(error);
        }

        @Override
        public void onComplete() {
            result.complete(body.toByteArray());
        }
    }
}
