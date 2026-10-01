package back.backend.domain.jobposting.source;

import back.backend.domain.jobposting.entity.JobSource;

public interface JobPostingSource {

    JobSource source();

    JobPostingFetchResult fetch();
}
