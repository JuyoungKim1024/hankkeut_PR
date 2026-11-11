package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpJobPostingPageFetcherTest {
    private HttpServer server;
    private URI uri;

    @BeforeEach
    void startFixture() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/job");
    }

    @AfterEach
    void stopFixture() {
        server.stop(0);
    }

    @Test
    @DisplayName("t1 HTML 공고 본문을 가져온다")
    void t1_readsHtmlBody() {
        serve(exchange -> send(exchange, 200, "text/html; charset=utf-8",
                "<h1>합성 공고</h1>".getBytes(StandardCharsets.UTF_8)));

        var page = fetcher(Duration.ofSeconds(2)).fetch(uri.toString());

        assertThat(new String(page.content(), StandardCharsets.UTF_8)).contains("합성 공고");
    }

    @Test
    @DisplayName("t2 헤더를 받은 뒤 본문이 멈추면 제한 시간에 종료한다")
    void t2_stalledBodyHasAnOverallDeadline() {
        CountDownLatch bodyStarted = new CountDownLatch(1);
        CountDownLatch releaseBody = new CountDownLatch(1);
        serve(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write('<');
            exchange.getResponseBody().flush();
            bodyStarted.countDown();
            try {
                releaseBody.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            assertThatThrownBy(() -> fetcher(Duration.ofSeconds(1)).fetch(uri.toString()))
                    .isInstanceOf(ApiException.class).hasMessageContaining("시간");
            assertThat(bodyStarted.getCount()).isZero();
        } finally {
            releaseBody.countDown();
        }
    }

    @Test
    @DisplayName("t3 길이를 알 수 없는 HTML 본문도 2MB를 넘으면 거부한다")
    void t3_rejectsOversizedChunkedBody() {
        serve(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);
            byte[] chunk = new byte[16 * 1024];
            for (int i = 0; i < 129; i++) {
                exchange.getResponseBody().write(chunk);
            }
        });

        assertThatThrownBy(() -> fetcher(Duration.ofSeconds(3)).fetch(uri.toString()))
                .isInstanceOf(ApiException.class).hasMessageContaining("2MB");
    }

    @Test
    @DisplayName("t4 HTML과 비슷한 잘못된 MIME 타입을 거부한다")
    void t4_rejectsInvalidMediaType() {
        serve(exchange -> send(exchange, 200, "text/html-other", new byte[]{1}));

        assertThatThrownBy(() -> fetcher(Duration.ofSeconds(2)).fetch(uri.toString()))
                .isInstanceOf(ApiException.class).hasMessageContaining("HTML");
    }

    @Test
    @DisplayName("t5 리다이렉트를 따라가지 않는다")
    void t5_doesNotFollowRedirect() {
        AtomicInteger redirected = new AtomicInteger();
        server.createContext("/destination", exchange -> {
            redirected.incrementAndGet();
            exchange.close();
        });
        serve(exchange -> {
            exchange.getResponseHeaders().set("Location", "/destination");
            exchange.sendResponseHeaders(302, -1);
        });

        assertThatThrownBy(() -> fetcher(Duration.ofSeconds(2)).fetch(uri.toString()))
                .isInstanceOf(ApiException.class).hasMessageContaining("302");
        assertThat(redirected.get()).isZero();
    }

    @Test
    @DisplayName("t6 사이트의 429 응답을 그대로 재시도하지 않는다")
    void t6_reportsRemoteRateLimitWithoutRetry() {
        AtomicInteger requests = new AtomicInteger();
        serve(exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(429, -1);
        });
        assertThatThrownBy(() -> fetcher(Duration.ofSeconds(2)).fetch(uri.toString()))
                .isInstanceOf(ApiException.class).hasMessageContaining("429");
        assertThat(requests.get()).isEqualTo(1);
    }

    private void serve(HttpHandler handler) {
        server.createContext("/job", exchange -> {
            try (exchange) {
                handler.handle(exchange);
            }
        });
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    private HttpJobPostingPageFetcher fetcher(Duration timeout) {
        JobPostingUrlPolicy policy = mock(JobPostingUrlPolicy.class);
        when(policy.validate(uri.toString())).thenReturn(uri);
        return new HttpJobPostingPageFetcher(policy, mock(JobPostingHostRateLimiter.class),
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build(), timeout);
    }
}
