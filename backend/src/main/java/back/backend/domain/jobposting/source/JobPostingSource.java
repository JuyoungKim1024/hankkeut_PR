package back.backend.domain.jobposting.source;

import back.backend.domain.jobposting.entity.JobSource;

import java.util.List;

public interface JobPostingSource {

    JobSource source();

    List<CollectedJobPosting> fetch();
}
