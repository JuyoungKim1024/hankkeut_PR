package back.backend.domain.resume.service;

import back.backend.domain.resume.entity.ResumeFileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalResumeStorage implements ResumeStorage {

    private final Path rootDirectory;

    public LocalResumeStorage(@Value("${app.storage.resume-directory}") String rootDirectory) {
        this.rootDirectory = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(byte[] content, ResumeFileType fileType) {
        String storageKey = UUID.randomUUID() + "." + fileType.name().toLowerCase(Locale.ROOT);
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(rootDirectory);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return storageKey;
        } catch (IOException exception) {
            throw new ResumeStorageException("이력서 파일 저장에 실패했습니다.", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new ResumeStorageException("이력서 파일 삭제에 실패했습니다.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = rootDirectory.resolve(storageKey).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new ResumeStorageException("잘못된 이력서 저장 키입니다.");
        }
        return resolved;
    }
}
