package back.backend.domain.jobposting.source.saramin;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.source.JobPostingSourceFailureCode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaraminJobResponseParserTest {

    private final SaraminJobResponseParser parser = new SaraminJobResponseParser(new ObjectMapper());

    @Test
    @DisplayName("t1 정상 공고는 변환하고 필수 필드 누락 공고는 부분 실패로 격리한다")
    void t1_parsesValidPostingAndIsolatesInvalidPosting() throws IOException {
        SaraminJobPage page = parser.parse(fixture("job-search-response.json"));

        assertThat(page.start()).isZero();
        assertThat(page.total()).isEqualTo(2);
        assertThat(page.rawCount()).isEqualTo(2);
        assertThat(page.postings()).hasSize(1);
        assertThat(page.failures()).hasSize(1);
        assertThat(page.failures().getFirst().code())
                .isEqualTo(JobPostingSourceFailureCode.MISSING_REQUIRED_FIELD);
        assertThat(page.postings().getFirst())
                .satisfies(posting -> {
                    assertThat(posting.externalId()).isEqualTo("1001");
                    assertThat(posting.companyName()).isEqualTo("테스트 컴퍼니");
                    assertThat(posting.jobCategory()).isEqualTo("IT개발·데이터");
                    assertThat(posting.description()).isEqualTo("Java,Spring Boot,MySQL");
                    assertThat(posting.qualification()).isEqualTo("학력무관");
                    assertThat(posting.status()).isEqualTo(JobPostingStatus.ACTIVE);
                });
    }

    @Test
    @DisplayName("t2 API 오류 응답은 서버 message를 노출하지 않고 코드만 보존한다")
    void t2_apiErrorDoesNotExposeServerMessage() {
        String response = "{\"code\":4,\"message\":\"access-key secret details\"}";

        assertThatThrownBy(() -> parser.parse(response))
                .isInstanceOf(SaraminApiResponseException.class)
                .hasMessage("사람인 API 응답 오류: code=4")
                .message().doesNotContain("secret details");
    }

    private static String fixture(String name) throws IOException {
        try (InputStream input = SaraminJobResponseParserTest.class
                .getResourceAsStream("/fixtures/saramin/" + name)) {
            if (input == null) {
                throw new IllegalStateException("fixture not found: " + name);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
