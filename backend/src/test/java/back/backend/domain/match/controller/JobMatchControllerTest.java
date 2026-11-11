package back.backend.domain.match.controller;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.service.JobPostingIngestionService;
import back.backend.domain.jobposting.service.JobSkillAnalysisService;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobMatchControllerTest {

    private static final String MATCH = "/api/jobs/{jobId}/match";

    @Autowired MockMvc mockMvc;
    @Autowired JobPostingIngestionService ingestionService;
    @Autowired JobPostingRepository postingRepository;
    @Autowired JobSkillAnalysisService skillAnalysis;
    @Autowired ProfileRepository profileRepository;

    @BeforeEach
    void resetProfilesForTest() {
        profileRepository.deleteAll();
        profileRepository.flush();
    }

    @Test
    @DisplayName("t1 필수 기술 두 개 보유 시 우대 기술 부족을 표시하고 80점을 저장한다")
    void t1_savesAndReadsWeightedMatch() throws Exception {
        long jobId = saveJob("Java, Spring Boot", "Redis");
        saveProfile("""
                [{"name":"java","level":"BEGINNER"},{"name":"SpringBoot","level":"ADVANCED"},
                 {"name":"SPRING BOOT","level":"BEGINNER"}]
                """);
        skillAnalysis.analyze(jobId);

        mockMvc.perform(get(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_CALCULATED"));
        mockMvc.perform(post(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.result.score").value(80.0))
                .andExpect(jsonPath("$.data.result.matchedWeight").value(4))
                .andExpect(jsonPath("$.data.result.totalWeight").value(5))
                .andExpect(jsonPath("$.data.result.skills[2].name").value("Redis"))
                .andExpect(jsonPath("$.data.result.skills[2].type").value("PREFERRED"))
                .andExpect(jsonPath("$.data.result.skills[2].matched").value(false));
        mockMvc.perform(get(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.analyzedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.result.score").value(80.0));
    }

    @Test
    @DisplayName("t2 보유 기술이 바뀌면 이전 점수를 숨기고 다시 계산할 수 있다")
    void t2_staleResultCanBeRecalculated() throws Exception {
        long jobId = saveJob("Java", "Redis");
        saveProfile("[{\"name\":\"Java\",\"level\":\"BEGINNER\"}]");
        skillAnalysis.analyze(jobId);
        mockMvc.perform(post(MATCH, jobId)).andExpect(status().isOk());

        saveProfile("[]");
        mockMvc.perform(get(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("STALE"))
                .andExpect(jsonPath("$.data.result").isEmpty());
        mockMvc.perform(post(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.score").value(0.0));
        saveProfile("[{\"name\":\"Java\",\"level\":\"BEGINNER\"},{\"name\":\"Redis\",\"level\":\"BEGINNER\"}]");
        mockMvc.perform(post(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.score").value(100.0));
    }

    @Test
    @DisplayName("t3 프로필이나 공고 기술 정보가 없으면 점수를 생성하지 않는다")
    void t3_requiresProfileAndJobSkills() throws Exception {
        long jobId = saveJob("Java", null);
        mockMvc.perform(get(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROFILE_REQUIRED"));

        saveProfile("[]");
        mockMvc.perform(post(MATCH, jobId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("JOB_SKILLS_REQUIRED"))
                .andExpect(jsonPath("$.data.result").isEmpty());
    }

    @Test
    @DisplayName("t4 없는 공고의 적합도는 404를 반환한다")
    void t4_missingJobReturnsNotFound() throws Exception {
        mockMvc.perform(get(MATCH, Long.MAX_VALUE)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JOB_NOT_FOUND"));
    }

    private long saveJob(String description, String preferred) {
        ingestionService.ingest(JobSource.MANUAL, List.of(new CollectedJobPosting(
                "match-test", "합성 회사", "합성 공고", "개발", null, null,
                description, null, preferred, "https://example.com/match-test", null, null,
                JobPostingStatus.ACTIVE)));
        return postingRepository.findBySourceAndExternalId(JobSource.MANUAL, "match-test")
                .orElseThrow().getId();
    }

    private void saveProfile(String skills) throws Exception {
        mockMvc.perform(put("/api/profile").contentType(MediaType.APPLICATION_JSON).content("""
                {"desiredJob":"개발","desiredLocation":"서울","careerLevel":"ENTRY","skills":%s}
                """.formatted(skills))).andExpect(status().isOk());
    }
}
