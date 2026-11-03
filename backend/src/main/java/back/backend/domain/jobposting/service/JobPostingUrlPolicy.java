package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JobPostingUrlPolicy {

    private final Set<String> allowedHosts;

    public JobPostingUrlPolicy(@Value("${app.jobs.crawling.allowed-hosts:}") String allowedHosts) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public URI validate(String value) {
        URI uri = parse(value);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        int port = uri.getPort();

        if (!("http".equals(scheme) || "https".equals(scheme)) || host.isEmpty()
                || uri.getRawUserInfo() != null || hasCustomPort(scheme, port)) {
            throw invalidUrl();
        }
        if (!allowedHosts.contains(host)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "JOB_SOURCE_NOT_ALLOWED",
                    "허용된 채용공고 사이트가 아닙니다.");
        }

        try {
            return new URI(scheme, null, host, port, uri.getPath(), uri.getQuery(), null).normalize();
        } catch (URISyntaxException exception) {
            throw invalidUrl();
        }
    }

    private URI parse(String value) {
        try {
            return URI.create(value == null ? "" : value.trim());
        } catch (IllegalArgumentException exception) {
            throw invalidUrl();
        }
    }

    private boolean hasCustomPort(String scheme, int port) {
        return port != -1 && !("http".equals(scheme) && port == 80) && !("https".equals(scheme) && port == 443);
    }

    private ApiException invalidUrl() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_JOB_URL", "유효한 채용공고 URL을 입력해 주세요.");
    }
}
