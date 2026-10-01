package back.backend.domain.resume.service;

import back.backend.domain.resume.entity.ResumeFileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TikaResumeDocumentProcessorTest {

    private final TikaResumeDocumentProcessor processor = new TikaResumeDocumentProcessor();

    @Test
    @DisplayName("t1 PDF 문서에서 실제 텍스트와 파일 형식을 추출한다")
    void t1_extractsPdfTextAndType() throws IOException {
        ProcessedResumeDocument result = processor.process(pdf("Backend resume"), "resume.pdf");

        assertThat(result.fileType()).isEqualTo(ResumeFileType.PDF);
        assertThat(result.extractedText()).contains("Backend resume");
    }

    @Test
    @DisplayName("t2 DOCX 문서에서 실제 텍스트와 파일 형식을 추출한다")
    void t2_extractsDocxTextAndType() throws IOException {
        ProcessedResumeDocument result = processor.process(docx("Spring project"), "resume.docx");

        assertThat(result.fileType()).isEqualTo(ResumeFileType.DOCX);
        assertThat(result.extractedText()).contains("Spring project");
    }

    @Test
    @DisplayName("t3 지원하지 않는 실제 파일은 추출을 거부한다")
    void t3_rejectsUnsupportedContent() {
        assertThatThrownBy(() -> processor.process("plain text".getBytes(), "resume.pdf"))
                .isInstanceOf(ResumeProcessingException.class);
    }

    private byte[] pdf(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 720);
                content.showText(text);
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] docx(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(output);
            return output.toByteArray();
        }
    }
}
