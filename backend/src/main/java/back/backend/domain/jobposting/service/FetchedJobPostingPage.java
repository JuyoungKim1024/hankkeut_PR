package back.backend.domain.jobposting.service;

import java.net.URI;

public record FetchedJobPostingPage(URI uri, String contentType, byte[] content) {
}
