package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.repository.JobSkillRepository;
import back.backend.domain.jobposting.service.JobPostingIngestionService;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobSkillControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JobPostingIngestionService ingestionService;
    @Autowired JobPostingRepository jobPostingRepository;
    @Autowired JobSkillRepository jobSkillRepository;

    @BeforeEach
    void setUp() {
        jobSkillRepository.deleteAll();
        jobPostingRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 저장된 공고를 분석하면 기술 스택을 저장하고 반환한다")
    void t1_analyzedSkillsAreStoredAndReturned() throws Exception {
        long jobId = savePosting();

        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Java"))
                .andExpect(jsonPath("$.data[0].type").value("REQUIRED"))
                .andExpect(jsonPath("$.data[2].name").value("Redis"))
                .andExpect(jsonPath("$.data[2].type").value("PREFERRED"));

        assertThat(jobSkillRepository.count()).isEqualTo(3);
        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
        assertThat(jobSkillRepository.count()).isEqualTo(3);

        mockMvc.perform(get("/api/jobs/{jobId}/skills", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("t2 존재하지 않는 공고 분석은 404를 반환한다")
    void t2_missingPostingReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JOB_NOT_FOUND"));
    }

    @Test
    @DisplayName("t3 공고 기술 원문이 바뀌면 저장된 분석을 비우고 새 본문을 분석한다")
    void t3_changedPostingClearsPreviouslyAnalyzedSkills() throws Exception {
        long jobId = savePosting();
        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", jobId))
                .andExpect(status().isOk());

        savePosting("Python 기반 서비스", null, null);
        mockMvc.perform(get("/api/jobs/{jobId}/skills", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Python"));
    }

    @Test
    @DisplayName("t4 분석할 기술이 없는 공고는 빈 배열을 반환한다")
    void t4_postingWithoutSkillsReturnsEmptyList() throws Exception {
        long jobId = savePosting("업무 내용은 면접에서 안내합니다.", null, null);
        mockMvc.perform(post("/api/jobs/{jobId}/skills/analysis", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        assertThat(jobSkillRepository.count()).isZero();
    }

    private long savePosting() {
        return savePosting("Java 기반 서비스", "Spring Boot 경험", "Redis 경험");
    }

    private long savePosting(String description, String qualification, String preference) {
        CollectedJobPosting posting = new CollectedJobPosting(
                "skill-test-1", "한끗랩", "백엔드 개발자", "백엔드", null, null,
                description, qualification, preference,
                "https://example.com/jobs/skill-test-1", null, null, JobPostingStatus.ACTIVE);
        ingestionService.ingest(JobSource.MANUAL, List.of(posting));
        return jobPostingRepository.findBySourceAndExternalId(JobSource.MANUAL, "skill-test-1")
                .orElseThrow().getId();
    }
}
