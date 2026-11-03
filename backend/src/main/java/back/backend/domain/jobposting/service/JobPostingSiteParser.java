package back.backend.domain.jobposting.service;

import java.net.URI;

public interface JobPostingSiteParser {

    boolean supports(URI uri);

    ParsedJobPostingPage parse(FetchedJobPostingPage page);
}
