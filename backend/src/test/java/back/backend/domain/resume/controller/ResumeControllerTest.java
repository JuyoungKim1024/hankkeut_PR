package back.backend.domain.resume.controller;

import back.backend.domain.resume.entity.ResumeFileType;
import back.backend.domain.resume.repository.ResumeRepository;
import back.backend.domain.resume.service.ProcessedResumeDocument;
import back.backend.domain.resume.service.ResumeDocumentProcessor;
import back.backend.domain.resume.service.ResumeStorage;
import back.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ResumeRepository resumeRepository;
    @Autowired UserRepository userRepository;

    @MockitoBean ResumeDocumentProcessor documentProcessor;
    @MockitoBean ResumeStorage resumeStorage;

    @BeforeEach
    void setUp() {
        resumeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 PDF 이력서를 업로드하면 텍스트를 추출하고 로컬에 저장한다")
    void t1_uploadsPdfAndExtractsText() throws Exception {
        MockMultipartFile file = pdfFile("resume.pdf");
        when(documentProcessor.process(any(), anyString()))
                .thenReturn(new ProcessedResumeDocument(ResumeFileType.PDF, "백엔드 프로젝트 경험"));
        when(resumeStorage.store(any(), any())).thenReturn("stored-resume.pdf");

        mockMvc.perform(multipart("/api/resumes").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalFileName").value("resume.pdf"))
                .andExpect(jsonPath("$.data.fileType").value("PDF"))
                .andExpect(jsonPath("$.data.extractedTextLength").value(11));

        assertThat(resumeRepository.count()).isEqualTo(1);
        verify(resumeStorage).store(any(), any());
    }

    @Test
    @DisplayName("t2 실제 파일 형식이 확장자와 다르면 저장하지 않고 400을 반환한다")
    void t2_rejectsDetectedTypeMismatch() throws Exception {
        MockMultipartFile file = pdfFile("resume.pdf");
        when(documentProcessor.process(any(), anyString()))
                .thenReturn(new ProcessedResumeDocument(ResumeFileType.DOCX, "내용"));

        mockMvc.perform(multipart("/api/resumes").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("RESUME_TYPE_MISMATCH"));

        verify(resumeStorage, never()).store(any(), any());
        assertThat(resumeRepository.count()).isZero();
    }

    @Test
    @DisplayName("t3 지원하지 않는 확장자는 처리하지 않고 400을 반환한다")
    void t3_rejectsUnsupportedExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", MediaType.TEXT_PLAIN_VALUE, "resume".getBytes());

        mockMvc.perform(multipart("/api/resumes").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_RESUME_TYPE"));

        verify(documentProcessor, never()).process(any(), anyString());
    }

    @Test
    @DisplayName("t4 저장한 이력서는 최신순 목록으로 조회한다")
    void t4_listsSavedResumes() throws Exception {
        when(documentProcessor.process(any(), anyString()))
                .thenReturn(new ProcessedResumeDocument(ResumeFileType.PDF, "추출 내용"));
        when(resumeStorage.store(any(), any())).thenReturn("stored-resume.pdf");
        mockMvc.perform(multipart("/api/resumes").file(pdfFile("resume.pdf")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].originalFileName").value("resume.pdf"));
    }

    @Test
    @DisplayName("t5 이력서를 삭제하면 로컬 파일과 메타데이터를 함께 제거한다")
    void t5_deletesResumeAndStoredFile() throws Exception {
        when(documentProcessor.process(any(), anyString()))
                .thenReturn(new ProcessedResumeDocument(ResumeFileType.PDF, "추출 내용"));
        when(resumeStorage.store(any(), any())).thenReturn("stored-resume.pdf");
        String response = mockMvc.perform(multipart("/api/resumes").file(pdfFile("resume.pdf")))
                .andReturn().getResponse().getContentAsString();
        long resumeId = new tools.jackson.databind.ObjectMapper().readTree(response).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/resumes/{resumeId}", resumeId))
                .andExpect(status().isNoContent());

        assertThat(resumeRepository.count()).isZero();
        verify(resumeStorage).delete("stored-resume.pdf");
    }

    @Test
    @DisplayName("t6 최대 크기를 초과한 파일은 처리하지 않고 413을 반환한다")
    void t6_rejectsOversizedFile() throws Exception {
        byte[] content = new byte[(10 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", MediaType.APPLICATION_PDF_VALUE, content);

        mockMvc.perform(multipart("/api/resumes").file(file))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.error.code").value("RESUME_TOO_LARGE"));

        verify(documentProcessor, never()).process(any(), anyString());
    }

    private MockMultipartFile pdfFile(String name) {
        return new MockMultipartFile("file", name, MediaType.APPLICATION_PDF_VALUE, "%PDF-test".getBytes());
    }
}
