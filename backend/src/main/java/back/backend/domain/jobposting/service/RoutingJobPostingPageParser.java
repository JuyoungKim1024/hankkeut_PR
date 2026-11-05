package back.backend.domain.jobposting.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class RoutingJobPostingPageParser implements JobPostingPageParser {

    private final List<JobPostingSiteParser> siteParsers;
    private final TikaJobPostingPageParser fallbackParser;

    public RoutingJobPostingPageParser(List<JobPostingSiteParser> siteParsers,
                                       TikaJobPostingPageParser fallbackParser) {
        this.siteParsers = List.copyOf(siteParsers);
        this.fallbackParser = fallbackParser;
    }

    @Override
    public ParsedJobPostingPage parse(FetchedJobPostingPage page) {
        return siteParsers.stream()
                .filter(parser -> parser.supports(page.uri()))
                .findFirst()
                .map(parser -> parser.parse(page))
                .orElseGet(() -> fallbackParser.parse(page));
    }
}
