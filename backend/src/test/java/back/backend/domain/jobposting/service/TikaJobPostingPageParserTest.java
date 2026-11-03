package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TikaJobPostingPageParserTest {

    private final TikaJobPostingPageParser parser = new TikaJobPostingPageParser();

    @Test
    @DisplayName("t1 HTML 문서에서 제목과 본문을 추출한다")
    void t1_htmlTitleAndBodyAreExtracted() {
        String html = """
                <html><head><title>백엔드 개발자 채용</title></head>
                <body><main><h1>백엔드 개발자</h1><p>Java와 Spring Boot 경험이 필요합니다.</p></main></body></html>
                """;

        ParsedJobPostingPage result = parser.parse(page(html));

        assertThat(result.title()).isEqualTo("백엔드 개발자 채용");
        assertThat(result.description()).contains("Java와 Spring Boot 경험이 필요합니다.");
    }

    @Test
    @DisplayName("t2 제목이 없는 HTML 문서는 파싱 실패로 처리한다")
    void t2_htmlWithoutTitleIsRejected() {
        assertThatThrownBy(() -> parser.parse(page("<html><body>본문만 있습니다.</body></html>")))
                .isInstanceOf(ApiException.class)
                .hasMessage("채용공고 제목을 찾을 수 없습니다.");
    }

    private FetchedJobPostingPage page(String html) {
        return new FetchedJobPostingPage(
                URI.create("https://jobs.example.com/jobs/123"),
                "text/html; charset=UTF-8",
                html.getBytes(StandardCharsets.UTF_8)
        );
    }
}
