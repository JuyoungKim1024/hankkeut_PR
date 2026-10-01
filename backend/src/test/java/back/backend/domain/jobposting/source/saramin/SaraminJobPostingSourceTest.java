package back.backend.domain.jobposting.source.saramin;

import back.backend.domain.jobposting.source.JobPostingFetchResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaraminJobPostingSourceTest {

    private final SaraminJobResponseParser parser = new SaraminJobResponseParser(new ObjectMapper());

    @Test
    @DisplayName("t1 모든 페이지를 수집하면 완전한 snapshot을 반환한다")
    void t1_allPagesCreateCompleteSnapshot() {
        List<Integer> requestedPages = new ArrayList<>();
        SaraminApiClient client = (start, count) -> {
            requestedPages.add(start);
            return page(start, 2, String.valueOf(start + 1001));
        };
        SaraminJobPostingSource source = new SaraminJobPostingSource(client, parser, 1, 3);

        JobPostingFetchResult result = source.fetch();

        assertThat(requestedPages).containsExactly(0, 1);
        assertThat(result.postings()).hasSize(2);
        assertThat(result.completeSnapshot()).isTrue();
    }

    @Test
    @DisplayName("t2 최대 페이지에 도달하면 불완전한 snapshot을 반환한다")
    void t2_pageLimitCreatesIncompleteSnapshot() {
        SaraminApiClient client = (start, count) -> page(start, 2, "1001");
        SaraminJobPostingSource source = new SaraminJobPostingSource(client, parser, 1, 1);

        JobPostingFetchResult result = source.fetch();

        assertThat(result.postings()).hasSize(1);
        assertThat(result.completeSnapshot()).isFalse();
    }

    private static String page(int start, int total, String id) {
        return """
                {
                  "jobs": {
                    "count": 1,
                    "start": %d,
                    "total": "%d",
                    "job": [{
                      "url": "https://www.saramin.co.kr/jobs/%s",
                      "active": 1,
                      "company": {"detail": {"name": "테스트 컴퍼니"}},
                      "position": {
                        "title": "백엔드 개발자",
                        "job-mid-code": {"name": "IT개발·데이터"},
                        "location": {"name": "서울"},
                        "experience-level": {"name": "신입"},
                        "required-education-level": {"name": "학력무관"}
                      },
                      "keyword": "Java",
                      "id": "%s",
                      "posting-timestamp": "1788192000",
                      "expiration-timestamp": "1790783999"
                    }]
                  }
                }
                """.formatted(start, total, id, id);
    }
}
