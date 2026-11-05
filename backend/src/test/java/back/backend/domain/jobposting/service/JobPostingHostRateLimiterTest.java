package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobPostingHostRateLimiterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-03T10:00:00Z"), ZoneOffset.UTC);
    private final JobPostingHostRateLimiter limiter = new JobPostingHostRateLimiter(Duration.ofSeconds(2), clock);

    @Test
    @DisplayName("t1 동일 호스트의 연속 요청은 호출 제한으로 거부한다")
    void t1_sameHostConsecutiveRequestIsRejected() {
        URI first = URI.create("https://www.jobkorea.co.kr/Recruit/GI_Read/1");
        URI second = URI.create("https://www.jobkorea.co.kr/Recruit/GI_Read/2");

        limiter.check(first);

        assertThatThrownBy(() -> limiter.check(second))
                .isInstanceOf(ApiException.class)
                .hasMessage("같은 사이트의 공고를 너무 빠르게 요청했습니다. 잠시 후 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("t2 서로 다른 호스트의 요청은 독립적으로 허용한다")
    void t2_differentHostsAreIndependent() {
        limiter.check(URI.create("https://www.jobkorea.co.kr/Recruit/GI_Read/1"));

        assertThatCode(() -> limiter.check(URI.create("https://careers.example.com/jobs/1")))
                .doesNotThrowAnyException();
    }
}
