package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
            return new URI(scheme, null, host, port, uri.getPath(), canonicalQuery(host, uri), null).normalize();
        } catch (URISyntaxException exception) {
            throw invalidUrl();
        }
    }

    void requirePublicAddress(URI uri) {
        try {
            if (Arrays.stream(InetAddress.getAllByName(uri.getHost())).allMatch(JobPostingUrlPolicy::isPublicAddress)) {
                return;
            }
        } catch (UnknownHostException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "JOB_FETCH_FAILED", "채용공고 사이트 주소를 확인할 수 없습니다.");
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "JOB_SOURCE_NOT_ALLOWED", "공개 인터넷의 채용공고만 가져올 수 있습니다.");
    }

    private static boolean isPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        int first = Byte.toUnsignedInt(bytes[0]);
        int second = Byte.toUnsignedInt(bytes[1]);
        if (bytes.length == 16) {
            return (first & 0xe0) == 0x20 && !(first == 0x20 && second == 0x02);
        }
        int third = Byte.toUnsignedInt(bytes[2]);
        return first != 0 && first < 224
                && !(first == 100 && second >= 64 && second <= 127)
                && !(first == 192 && second == 0 && third == 0)
                && !(first == 198 && (second == 18 || second == 19));
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

    private String canonicalQuery(String host, URI uri) {
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        if (("www.jobkorea.co.kr".equals(host) || "jobkorea.co.kr".equals(host))
                && path.startsWith("/recruit/gi_read/")) {
            return null;
        }
        return uri.getQuery();
    }

    private ApiException invalidUrl() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_JOB_URL", "유효한 채용공고 URL을 입력해 주세요.");
    }
}
