package back.backend.domain.application.controller;

import back.backend.domain.application.repository.ApplicationRepository;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.service.JobPostingIngestionService;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired JobPostingRepository jobPostingRepository;
    @Autowired JobPostingIngestionService ingestionService;
    @Autowired UserRepository userRepository;

    private long jobPostingId;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        jobPostingRepository.deleteAll();
        userRepository.deleteAll();
        ingestionService.ingest(JobSource.MANUAL, List.of(posting()));
        jobPostingId = jobPostingRepository.findBySourceAndExternalId(JobSource.MANUAL, "application-job")
                .orElseThrow().getId();
    }

    @Test
    @DisplayName("t1 저장한 공고에 관심 상태의 지원 기록을 생성한다")
    void t1_createsApplicationForSavedJob() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("INTERESTED")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobPostingId").value(jobPostingId))
                .andExpect(jsonPath("$.data.status").value("INTERESTED"))
                .andExpect(jsonPath("$.data.memo").value("지원 전 기술 확인"));

        assertThat(applicationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("t2 같은 공고에 지원 기록을 중복 생성하면 409를 반환한다")
    void t2_duplicateApplicationReturnsConflict() throws Exception {
        createApplication("INTERESTED");

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("PREPARING")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("APPLICATION_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("t3 관심 상태를 준비 상태로 변경하고 메모와 일정을 갱신한다")
    void t3_updatesAllowedStatusAndDetails() throws Exception {
        long applicationId = createApplication("INTERESTED");

        mockMvc.perform(patch("/api/applications/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PREPARING",
                                  "memo": "포트폴리오 정리",
                                  "nextActionAt": "2026-10-10T10:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARING"))
                .andExpect(jsonPath("$.data.memo").value("포트폴리오 정리"))
                .andExpect(jsonPath("$.data.nextActionAt").value("2026-10-10T10:00:00"));
    }

    @Test
    @DisplayName("t4 허용되지 않은 상태 전이는 409를 반환한다")
    void t4_rejectsInvalidStatusTransition() throws Exception {
        long applicationId = createApplication("INTERESTED");

        mockMvc.perform(patch("/api/applications/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\",\"memo\":null,\"nextActionAt\":null}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INVALID_APPLICATION_TRANSITION"));
    }

    @Test
    @DisplayName("t5 상태 조건으로 지원 기록 목록을 조회한다")
    void t5_filtersApplicationsByStatus() throws Exception {
        createApplication("INTERESTED");

        mockMvc.perform(get("/api/applications").queryParam("status", "INTERESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].companyName").value("한끗랩"));

        mockMvc.perform(get("/api/applications").queryParam("status", "APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("t6 지원 기록을 삭제하면 목록에서 제거한다")
    void t6_deletesApplication() throws Exception {
        long applicationId = createApplication("INTERESTED");

        mockMvc.perform(delete("/api/applications/{applicationId}", applicationId))
                .andExpect(status().isNoContent());

        assertThat(applicationRepository.count()).isZero();
    }

    private long createApplication(String status) throws Exception {
        String response = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest(status)))
                .andReturn().getResponse().getContentAsString();
        return new tools.jackson.databind.ObjectMapper().readTree(response).path("data").path("id").asLong();
    }

    private String createRequest(String status) {
        return """
                {
                  "jobPostingId": %d,
                  "status": "%s",
                  "memo": "지원 전 기술 확인",
                  "nextActionAt": "2026-10-05T18:00:00"
                }
                """.formatted(jobPostingId, status);
    }

    private CollectedJobPosting posting() {
        return new CollectedJobPosting(
                "application-job", "한끗랩", "백엔드 개발자", "백엔드", "신입", "서울",
                "Java 서비스 개발", "Java 기본 지식", null,
                "https://example.com/jobs/application-job",
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 10, 30, 0, 0),
                JobPostingStatus.ACTIVE
        );
    }
}
