package back.backend.domain.jobposting.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JobKoreaJobPostingPageParser implements JobPostingSiteParser {

    private static final Pattern CAREER_PATTERN = Pattern.compile("경력\\s*:\\s*([^,]+)");
    private static final Pattern DEADLINE_PATTERN = Pattern.compile("마감일\\s*:\\s*(\\d{4}\\.\\d{2}\\.\\d{2})");
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final TikaJobPostingPageParser fallbackParser;

    public JobKoreaJobPostingPageParser(TikaJobPostingPageParser fallbackParser) {
        this.fallbackParser = fallbackParser;
    }

    @Override
    public boolean supports(URI uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        return ("www.jobkorea.co.kr".equals(host) || "jobkorea.co.kr".equals(host))
                && path.startsWith("/recruit/gi_read/");
    }

    @Override
    public ParsedJobPostingPage parse(FetchedJobPostingPage page) {
        ParsedJobPostingPage fallback = fallbackParser.parse(page);
        Document document = parseDocument(page);
        String companyName = content(document, "meta[name=writer]");
        String metadataTitle = content(document, "meta[property=og:title]");
        String metadataDescription = content(document, "meta[name=description]");

        return new ParsedJobPostingPage(
                companyName,
                extractTitle(metadataTitle, companyName, fallback.title()),
                extract(CAREER_PATTERN, metadataDescription),
                fallback.description(),
                extractDeadline(metadataDescription)
        );
    }

    private Document parseDocument(FetchedJobPostingPage page) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(page.content())) {
            return Jsoup.parse(input, null, page.uri().toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Fetched HTML could not be parsed", exception);
        }
    }

    private String content(Document document, String selector) {
        Element element = document.selectFirst(selector);
        if (element == null) {
            return null;
        }
        String value = element.attr("content").strip();
        return value.isEmpty() ? null : value;
    }

    private String extractTitle(String metadataTitle, String companyName, String fallbackTitle) {
        if (metadataTitle == null) {
            return fallbackTitle;
        }
        String prefix = companyName == null ? "" : Pattern.quote(companyName) + "\\s*";
        Pattern titlePattern = Pattern.compile("^" + prefix + "채용\\s*-\\s*(.+?)\\s*\\|\\s*잡코리아$");
        Matcher matcher = titlePattern.matcher(metadataTitle);
        return matcher.matches() ? matcher.group(1).strip() : fallbackTitle;
    }

    private String extract(Pattern pattern, String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1).strip() : null;
    }

    private LocalDateTime extractDeadline(String value) {
        String deadline = extract(DEADLINE_PATTERN, value);
        if (deadline == null) {
            return null;
        }
        try {
            return LocalDate.parse(deadline, DEADLINE_FORMAT).atTime(LocalTime.of(23, 59, 59));
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
