package back.backend.domain.jobposting.service;

public interface JobPostingPageParser {

    ParsedJobPostingPage parse(FetchedJobPostingPage page);
}
