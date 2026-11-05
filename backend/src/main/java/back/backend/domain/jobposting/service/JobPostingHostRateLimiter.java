package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class JobPostingHostRateLimiter {

    private final Duration minimumInterval;
    private final Clock clock;
    private final Map<String, Instant> lastRequests = new HashMap<>();

    @Autowired
    public JobPostingHostRateLimiter(
            @Value("${app.jobs.crawling.minimum-host-interval:PT2S}") Duration minimumInterval
    ) {
        this(minimumInterval, Clock.systemUTC());
    }

    JobPostingHostRateLimiter(Duration minimumInterval, Clock clock) {
        this.minimumInterval = minimumInterval;
        this.clock = clock;
    }

    public synchronized void check(URI uri) {
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        Instant now = clock.instant();
        Instant nextAllowedAt = lastRequests.getOrDefault(host, Instant.MIN).plus(minimumInterval);
        if (now.isBefore(nextAllowedAt)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "JOB_FETCH_RATE_LIMITED",
                    "같은 사이트의 공고를 너무 빠르게 요청했습니다. 잠시 후 다시 시도해 주세요.");
        }
        lastRequests.put(host, now);
    }
}
