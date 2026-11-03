package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class TikaJobPostingPageParser implements JobPostingPageParser {

    private static final int MAX_TEXT_LENGTH = 1_000_000;
    private final Tika tika = new Tika();

    @Override
    public ParsedJobPostingPage parse(FetchedJobPostingPage page) {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "posting.html");

        try (ByteArrayInputStream input = new ByteArrayInputStream(page.content())) {
            String description = normalize(tika.parseToString(input, metadata, MAX_TEXT_LENGTH));
            String title = normalize(metadata.get(TikaCoreProperties.TITLE));
            if (title == null) {
                throw parseFailed("채용공고 제목을 찾을 수 없습니다.");
            }
            if (description == null) {
                throw parseFailed("채용공고 본문을 찾을 수 없습니다.");
            }
            return new ParsedJobPostingPage(title, description);
        } catch (IOException | TikaException exception) {
            throw parseFailed("채용공고 내용을 분석할 수 없습니다.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
        return normalized.isBlank() ? null : normalized;
    }

    private ApiException parseFailed(String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "JOB_PARSE_FAILED", message);
    }
}
