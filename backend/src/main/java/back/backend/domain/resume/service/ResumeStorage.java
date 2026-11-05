package back.backend.domain.resume.service;

import back.backend.domain.resume.entity.ResumeFileType;

public interface ResumeStorage {

    String store(byte[] content, ResumeFileType fileType);

    void delete(String storageKey);
}
