package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobPostingQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JobPostingRepository jobPostingRepository;
    @Autowired JobPostingIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
        ingestionService.ingest(JobSource.MANUAL, List.of(
                posting("job-1", "한끗랩", "주니어 백엔드 개발자", "서울", JobPostingStatus.ACTIVE,
                        LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 10, 30, 0, 0)),
                posting("job-2", "로컬웍스", "플랫폼 엔지니어", "부산", JobPostingStatus.ACTIVE,
                        LocalDateTime.of(2026, 9, 2, 0, 0), LocalDateTime.of(2026, 10, 20, 0, 0)),
                posting("job-3", "완료컴퍼니", "백엔드 엔지니어", "서울", JobPostingStatus.CLOSED,
                        LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 20, 0, 0))
        ));
    }

    @Test
    @DisplayName("t1 기본 목록은 진행 중 공고를 최신순으로 페이지 조회한다")
    void t1_defaultListReturnsActiveJobsByLatestPostingDate() throws Exception {
        mockMvc.perform(get("/api/jobs").queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("플랫폼 엔지니어"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    @DisplayName("t2 검색어와 지역 및 상태 조건으로 공고를 필터링한다")
    void t2_filtersJobsByQueryLocationAndStatus() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .queryParam("query", "백엔드")
                        .queryParam("location", "서울")
                        .queryParam("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].companyName").value("완료컴퍼니"));
    }

    @Test
    @DisplayName("t3 공고 식별자로 전체 상세 내용을 조회한다")
    void t3_readsJobPostingDetail() throws Exception {
        long id = jobPostingRepository.findBySourceAndExternalId(JobSource.MANUAL, "job-1")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/jobs/{jobId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.description").value("Java와 Spring Boot를 사용하는 서비스 개발"))
                .andExpect(jsonPath("$.data.qualification").value("Java 기본 지식"))
                .andExpect(jsonPath("$.data.originalUrl").value("https://example.com/jobs/job-1"));
    }

    @Test
    @DisplayName("t4 존재하지 않는 공고를 조회하면 404를 반환한다")
    void t4_missingJobPostingReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JOB_NOT_FOUND"));
    }

    private CollectedJobPosting posting(String externalId,
                                        String companyName,
                                        String title,
                                        String location,
                                        JobPostingStatus status,
                                        LocalDateTime postedAt,
                                        LocalDateTime expiredAt) {
        return new CollectedJobPosting(
                externalId,
                companyName,
                title,
                "백엔드",
                "신입",
                location,
                "Java와 Spring Boot를 사용하는 서비스 개발",
                "Java 기본 지식",
                "개인 프로젝트 경험",
                "https://example.com/jobs/" + externalId,
                postedAt,
                expiredAt,
                status
        );
    }
}
