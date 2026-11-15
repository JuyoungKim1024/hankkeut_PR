package back.backend.domain.jobposting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeJobRecommendationsTest {
    @Test
    @DisplayName("t1 이력서 기술과 겹치는 공개 공고만 점수순으로 표시한다")
    void t1_ranksMatchingPublicPostings() {
        var board = JsonMapper.builder().build().readTree("""
                {"jobs":[
                  {"id":1,"title":"Backend","internal_job_id":1,"content":"&lt;p&gt;Java and Redis&lt;/p&gt;","absolute_url":"https://job-boards.greenhouse.io/example/jobs/1"},
                  {"id":2,"title":"Platform","internal_job_id":2,"content":"Java and Spring Boot","absolute_url":"https://job-boards.greenhouse.io/example/jobs/2"},
                  {"id":3,"title":"Frontend","internal_job_id":3,"content":"JavaScript","absolute_url":"https://job-boards.greenhouse.io/example/jobs/3"}
                ]}
                """);
        var result = new ResumeJobRecommendations(new RuleBasedSkillExtractor())
                .rank(board, "Java와 Spring Boot를 사용했습니다.", "");

        assertThat(result).extracting(ResumeJobRecommendations.Recommendation::title)
                .containsExactly("Platform", "Backend");
        assertThat(result.getFirst().matchedSkills()).containsExactly("Java", "Spring Boot");
        assertThat(result.getLast().score()).isEqualTo(50);
    }
}
