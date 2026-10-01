package back.backend.domain.resume.service;

import back.backend.domain.resume.entity.ResumeFileType;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class TikaResumeDocumentProcessor implements ResumeDocumentProcessor {

    private static final int MAX_EXTRACTED_CHARACTERS = 1_000_000;
    private static final String PDF_MIME = "application/pdf";
    private static final String DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final Tika tika = new Tika();

    @Override
    public ProcessedResumeDocument process(byte[] content, String originalFileName) {
        try {
            String detectedMime = tika.detect(new ByteArrayInputStream(content), originalFileName);
            ResumeFileType fileType = toFileType(detectedMime);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName);
            String extractedText = tika.parseToString(
                    new ByteArrayInputStream(content), metadata, MAX_EXTRACTED_CHARACTERS).trim();
            if (extractedText.isEmpty()) {
                throw new ResumeProcessingException("이력서에서 텍스트를 추출할 수 없습니다.");
            }
            return new ProcessedResumeDocument(fileType, extractedText);
        } catch (IOException | TikaException exception) {
            throw new ResumeProcessingException("이력서 텍스트 추출에 실패했습니다.", exception);
        }
    }

    private ResumeFileType toFileType(String detectedMime) {
        return switch (detectedMime) {
            case PDF_MIME -> ResumeFileType.PDF;
            case DOCX_MIME -> ResumeFileType.DOCX;
            default -> throw new ResumeProcessingException("지원하지 않는 실제 파일 형식입니다.");
        };
    }
}
