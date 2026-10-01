package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManualJobPostingImportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JobPostingRepository jobPostingRepository;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 공고 URL과 본문을 등록하면 공고를 생성한다")
    void t1_manualPostingCreatesJobPosting() throws Exception {
        mockMvc.perform(post("/api/jobs/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("백엔드 개발자")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobPostingId").isNumber())
                .andExpect(jsonPath("$.data.created").value(1))
                .andExpect(jsonPath("$.data.updated").value(0))
                .andExpect(jsonPath("$.data.unchanged").value(0));

        assertThat(jobPostingRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("t2 같은 URL의 변경된 공고를 다시 등록하면 기존 공고를 갱신한다")
    void t2_sameUrlUpdatesExistingPosting() throws Exception {
        mockMvc.perform(post("/api/jobs/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("백엔드 개발자")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jobs/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("플랫폼 백엔드 개발자")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobPostingId").isNumber())
                .andExpect(jsonPath("$.data.created").value(0))
                .andExpect(jsonPath("$.data.updated").value(1));

        assertThat(jobPostingRepository.count()).isEqualTo(1);
        assertThat(jobPostingRepository.findAll().getFirst().getTitle()).isEqualTo("플랫폼 백엔드 개발자");
    }

    @Test
    @DisplayName("t3 HTTP URL이 아니면 400을 반환한다")
    void t3_nonHttpUrlReturnsBadRequest() throws Exception {
        String request = validRequest("백엔드 개발자")
                .replace("https://example.com/jobs/123", "file:///jobs/123");

        mockMvc.perform(post("/api/jobs/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    private String validRequest(String title) {
        return """
                {
                  "originalUrl": "https://example.com/jobs/123",
                  "companyName": "한끗랩",
                  "title": "%s",
                  "jobCategory": "백엔드",
                  "career": "신입",
                  "location": "서울",
                  "description": "Java와 Spring Boot를 사용하는 채용공고입니다.",
                  "qualification": "Java 기본 지식",
                  "preference": "개인 프로젝트 경험"
                }
                """.formatted(title);
    }
}
