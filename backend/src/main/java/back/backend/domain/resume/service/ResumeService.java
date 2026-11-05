package back.backend.domain.resume.service;

import back.backend.domain.resume.dto.ResumeResponse;
import back.backend.domain.resume.entity.Resume;
import back.backend.domain.resume.entity.ResumeFileType;
import back.backend.domain.resume.repository.ResumeRepository;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.service.LocalUserService;
import back.backend.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Service
public class ResumeService {

    private static final String PDF_MIME = "application/pdf";
    private static final String DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final LocalUserService localUserService;
    private final ResumeRepository resumeRepository;
    private final ResumeDocumentProcessor documentProcessor;
    private final ResumeStorage resumeStorage;
    private final long maxFileSizeBytes;

    public ResumeService(LocalUserService localUserService,
                         ResumeRepository resumeRepository,
                         ResumeDocumentProcessor documentProcessor,
                         ResumeStorage resumeStorage,
                         @Value("${app.resumes.max-file-size-bytes}") long maxFileSizeBytes) {
        this.localUserService = localUserService;
        this.resumeRepository = resumeRepository;
        this.documentProcessor = documentProcessor;
        this.resumeStorage = resumeStorage;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Transactional
    public ResumeResponse upload(MultipartFile file) {
        UploadMetadata metadata = validate(file);
        byte[] content = read(file);
        ProcessedResumeDocument processed = process(content, metadata.originalFileName());
        if (processed.fileType() != metadata.expectedFileType()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESUME_TYPE_MISMATCH",
                    "파일 확장자와 실제 형식이 일치하지 않습니다.");
        }

        String storageKey = store(content, processed.fileType());
        try {
            User user = localUserService.getOrCreate();
            Resume resume = Resume.create(
                    user,
                    storageKey,
                    processed.fileType(),
                    metadata.originalFileName(),
                    processed.extractedText()
            );
            return ResumeResponse.from(resumeRepository.saveAndFlush(resume));
        } catch (RuntimeException exception) {
            try {
                resumeStorage.delete(storageKey);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    @Transactional
    public List<ResumeResponse> findAll() {
        Long userId = localUserService.getOrCreate().getId();
        return resumeRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ResumeResponse::from)
                .toList();
    }

    @Transactional
    public void delete(long resumeId) {
        Long userId = localUserService.getOrCreate().getId();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."));
        resumeRepository.delete(resume);
        resumeRepository.flush();
        try {
            resumeStorage.delete(resume.getFileUrl());
        } catch (ResumeStorageException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESUME_DELETE_FAILED",
                    "이력서 파일을 삭제하지 못했습니다.");
        }
    }

    private UploadMetadata validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_RESUME", "이력서 파일을 선택해 주세요.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new ApiException(HttpStatus.CONTENT_TOO_LARGE, "RESUME_TOO_LARGE",
                    "이력서 파일은 10MB 이하여야 합니다.");
        }

        String originalName = safeOriginalName(file.getOriginalFilename());
        ResumeFileType expectedType = extensionType(originalName);
        String contentType = file.getContentType();
        String expectedMime = expectedType == ResumeFileType.PDF ? PDF_MIME : DOCX_MIME;
        if (!expectedMime.equalsIgnoreCase(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESUME_MIME_MISMATCH",
                    "파일 MIME 형식이 확장자와 일치하지 않습니다.");
        }
        return new UploadMetadata(originalName, expectedType);
    }

    private String safeOriginalName(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RESUME_NAME", "파일 이름을 확인해 주세요.");
        }
        String fileName = Path.of(value).getFileName().toString().trim();
        if (fileName.isEmpty() || fileName.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RESUME_NAME", "파일 이름을 확인해 주세요.");
        }
        return fileName;
    }

    private ResumeFileType extensionType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return ResumeFileType.PDF;
        if (lower.endsWith(".docx")) return ResumeFileType.DOCX;
        throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_RESUME_TYPE",
                "PDF 또는 DOCX 파일만 등록할 수 있습니다.");
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESUME_READ_FAILED", "이력서 파일을 읽지 못했습니다.");
        }
    }

    private ProcessedResumeDocument process(byte[] content, String originalFileName) {
        try {
            return documentProcessor.process(content, originalFileName);
        } catch (ResumeProcessingException exception) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "RESUME_PARSE_FAILED",
                    "이력서 텍스트를 추출하지 못했습니다.");
        }
    }

    private String store(byte[] content, ResumeFileType fileType) {
        try {
            return resumeStorage.store(content, fileType);
        } catch (ResumeStorageException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESUME_STORE_FAILED",
                    "이력서 파일을 저장하지 못했습니다.");
        }
    }

    private record UploadMetadata(String originalFileName, ResumeFileType expectedFileType) {
    }
}
