package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

@Component
public class HttpJobPostingPageFetcher implements JobPostingPageFetcher {

    private static final int MAX_CONTENT_BYTES = 2 * 1024 * 1024;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final JobPostingUrlPolicy urlPolicy;
    private final JobPostingHostRateLimiter rateLimiter;
    private final HttpClient httpClient;

    public HttpJobPostingPageFetcher(JobPostingUrlPolicy urlPolicy,
                                     JobPostingHostRateLimiter rateLimiter) {
        this.urlPolicy = urlPolicy;
        this.rateLimiter = rateLimiter;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public FetchedJobPostingPage fetch(String originalUrl) {
        URI uri = urlPolicy.validate(originalUrl);
        rateLimiter.check(uri);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("User-Agent", "HankkeutLocal/1.0")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw fetchFailed("채용공고 페이지가 HTTP " + response.statusCode() + "을 반환했습니다.");
                }
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                if (!isHtml(contentType)) {
                    throw fetchFailed("HTML 채용공고 페이지만 가져올 수 있습니다.");
                }
                byte[] content = body.readNBytes(MAX_CONTENT_BYTES + 1);
                if (content.length > MAX_CONTENT_BYTES) {
                    throw fetchFailed("채용공고 페이지 크기가 2MB를 초과합니다.");
                }
                return new FetchedJobPostingPage(uri, contentType, content);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw fetchFailed("채용공고 가져오기가 중단되었습니다.");
        } catch (IOException exception) {
            throw fetchFailed("채용공고 페이지에 연결할 수 없습니다.");
        }
    }

    private boolean isHtml(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return normalized.startsWith("text/html") || normalized.startsWith("application/xhtml+xml");
    }

    private ApiException fetchFailed(String message) {
        return new ApiException(HttpStatus.BAD_GATEWAY, "JOB_FETCH_FAILED", message);
    }
}
