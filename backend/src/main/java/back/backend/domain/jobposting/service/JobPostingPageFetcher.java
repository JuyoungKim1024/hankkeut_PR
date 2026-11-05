package back.backend.domain.jobposting.service;

public interface JobPostingPageFetcher {

    FetchedJobPostingPage fetch(String originalUrl);
}
