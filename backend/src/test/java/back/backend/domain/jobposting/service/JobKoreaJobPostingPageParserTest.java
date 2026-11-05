package back.backend.domain.jobposting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobKoreaJobPostingPageParserTest {

    private final TikaJobPostingPageParser fallbackParser = new TikaJobPostingPageParser();
    private final JobKoreaJobPostingPageParser parser = new JobKoreaJobPostingPageParser(fallbackParser);

    @Test
    @DisplayName("t1 잡코리아 메타데이터에서 회사명 공고명 경력과 마감일을 추출한다")
    void t1_jobKoreaMetadataIsExtracted() {
        String html = """
                <html><head>
                  <title>한끗랩 채용 - 백엔드 개발자 | 잡코리아</title>
                  <meta name="writer" content="한끗랩">
                  <meta property="og:title" content="한끗랩 채용 - 백엔드 개발자 | 잡코리아">
                  <meta name="description" content="경력 : 경력 2년이상, 학력 : 학력무관, 마감일 : 2026.11.30">
                </head><body><main>Java와 Spring Boot 기반 서비스 개발</main></body></html>
                """;

        ParsedJobPostingPage result = parser.parse(page(html));

        assertThat(result.companyName()).isEqualTo("한끗랩");
        assertThat(result.title()).isEqualTo("백엔드 개발자");
        assertThat(result.career()).isEqualTo("경력 2년이상");
        assertThat(result.expiredAt()).isEqualTo(LocalDateTime.of(2026, 11, 30, 23, 59, 59));
        assertThat(result.description()).contains("Java와 Spring Boot 기반 서비스 개발");
    }

    @Test
    @DisplayName("t2 잡코리아 공고 상세 경로만 지원한다")
    void t2_onlyJobKoreaDetailPathIsSupported() {
        assertThat(parser.supports(URI.create("https://www.jobkorea.co.kr/Recruit/GI_Read/123"))).isTrue();
        assertThat(parser.supports(URI.create("https://www.jobkorea.co.kr/Search/?stext=java"))).isFalse();
        assertThat(parser.supports(URI.create("https://example.com/Recruit/GI_Read/123"))).isFalse();
    }

    private FetchedJobPostingPage page(String html) {
        return new FetchedJobPostingPage(
                URI.create("https://www.jobkorea.co.kr/Recruit/GI_Read/123"),
                "text/html; charset=UTF-8",
                html.getBytes(StandardCharsets.UTF_8)
        );
    }
}
