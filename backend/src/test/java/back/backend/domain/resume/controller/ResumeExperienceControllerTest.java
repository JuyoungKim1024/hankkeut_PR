package back.backend.domain.resume.controller;

import back.backend.domain.resume.entity.Resume;
import back.backend.domain.resume.entity.ResumeFileType;
import back.backend.domain.resume.repository.ResumeRepository;
import back.backend.domain.user.service.LocalUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResumeExperienceControllerTest {
    private static final String ROUTE = "/api/resumes/{id}/experiences";
    @Autowired MockMvc mvc;
    @Autowired ResumeRepository resumes;
    @Autowired LocalUserService users;

    @Test
    @DisplayName("t1 확인한 경험을 이력서에 저장하고 조회한다")
    void t1_savesExperienceWithSourceExcerpt() throws Exception {
        long id = saveResume();
        mvc.perform(post(ROUTE, id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"project":"도서 검색","role":"개발","skills":"Java",
                                 "outcome":"검색 API 구현","evidence":"Java로 검색 API를 구현했습니다."}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(get(ROUTE, id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.experiences[0].content.project").value("도서 검색"));
    }

    @Test
    @DisplayName("t2 원문에 없는 근거는 저장하지 않는다")
    void t2_rejectsEvidenceOutsideResume() throws Exception {
        mvc.perform(post(ROUTE, saveResume()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"project":"도서 검색","role":"개발","evidence":"매출을 두 배 높였습니다."}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("EXPERIENCE_EVIDENCE_INVALID"));
    }

    private long saveResume() {
        return resumes.save(Resume.create(users.getOrCreate(), "fixture.docx", ResumeFileType.DOCX,
                "fixture.docx", "Java로 검색 API를 구현했습니다.")).getId();
    }
}
