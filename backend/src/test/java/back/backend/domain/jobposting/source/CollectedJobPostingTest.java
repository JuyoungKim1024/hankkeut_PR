package back.backend.domain.jobposting.source;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectedJobPostingTest {

    @Test
    @DisplayName("t1 수집 공고를 생성하면 문자열을 정규화한다")
    void t1_normalizesCollectedJobPosting() {
        CollectedJobPosting posting = posting(" external-1 ", " https://example.com/jobs/1 ", "  ");

        assertThat(posting.externalId()).isEqualTo("external-1");
        assertThat(posting.companyName()).isEqualTo("한끗 컴퍼니");
        assertThat(posting.originalUrl()).isEqualTo("https://example.com/jobs/1");
        assertThat(posting.career()).isNull();
    }

    @Test
    @DisplayName("t2 필수 필드가 비어 있으면 생성을 거부한다")
    void t2_rejectsBlankRequiredField() {
        assertThatThrownBy(() -> posting(" ", "https://example.com/jobs/1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("externalId");
    }

    @Test
    @DisplayName("t3 원문 주소가 HTTP 형식이 아니면 생성을 거부한다")
    void t3_rejectsUnsupportedOriginalUrl() {
        assertThatThrownBy(() -> posting("external-1", "file:///jobs/1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalUrl");
    }

    private static CollectedJobPosting posting(String externalId, String originalUrl, String career) {
        return new CollectedJobPosting(
                externalId,
                " 한끗 컴퍼니 ",
                " 백엔드 개발자 ",
                " 개발 ",
                career,
                " 서울 ",
                " 채용 설명 ",
                " Java ",
                null,
                originalUrl,
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 10, 1, 0, 0),
                JobPostingStatus.ACTIVE
        );
    }
}
