package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSource;

public class JobPostingCollectionException extends RuntimeException {

    private final JobSource source;
    private final String failureType;

    public JobPostingCollectionException(JobSource source, RuntimeException cause) {
        super(source + " 공고 수집에 실패했습니다.");
        this.source = source;
        this.failureType = cause.getClass().getSimpleName();
    }

    public JobSource getSource() {
        return source;
    }

    public String getFailureType() {
        return failureType;
    }
}
