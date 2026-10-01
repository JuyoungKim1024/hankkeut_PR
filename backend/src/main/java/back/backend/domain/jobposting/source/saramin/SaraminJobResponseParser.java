package back.backend.domain.jobposting.source.saramin;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.jobposting.source.JobPostingSourceFailure;
import back.backend.domain.jobposting.source.JobPostingSourceFailureCode;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class SaraminJobResponseParser {

    private final ObjectMapper objectMapper;

    public SaraminJobResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SaraminJobPage parse(String responseBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (RuntimeException exception) {
            throw new SaraminApiResponseException("INVALID_JSON");
        }

        if (!root.path("code").isMissingNode()) {
            String code = optionalText(root, "code");
            throw new SaraminApiResponseException(code == null ? "UNKNOWN" : code);
        }

        JsonNode jobs = root.path("jobs");
        JsonNode items = jobs.path("job");
        if (jobs.isMissingNode() || !items.isArray()) {
            throw new SaraminApiResponseException("INVALID_STRUCTURE");
        }

        List<CollectedJobPosting> postings = new ArrayList<>();
        List<JobPostingSourceFailure> failures = new ArrayList<>();
        int index = 0;
        for (JsonNode item : items) {
            String reference = optionalText(item, "id");
            if (reference == null) {
                reference = "index-" + index;
            }
            try {
                postings.add(toPosting(item));
            } catch (IllegalArgumentException exception) {
                failures.add(new JobPostingSourceFailure(
                        reference, JobPostingSourceFailureCode.MISSING_REQUIRED_FIELD));
            } catch (RuntimeException exception) {
                failures.add(new JobPostingSourceFailure(
                        reference, JobPostingSourceFailureCode.PARSE_ERROR));
            }
            index++;
        }

        return new SaraminJobPage(
                jobs.path("start").asInt(0),
                parseInteger(jobs.path("total"), items.size()),
                items.size(),
                postings,
                failures
        );
    }

    private static CollectedJobPosting toPosting(JsonNode item) {
        JsonNode position = item.path("position");
        String title = optionalText(position, "title");
        String keyword = optionalText(item, "keyword");
        String description = keyword == null ? title : keyword;
        return new CollectedJobPosting(
                optionalText(item, "id"),
                nestedText(item, "company", "detail", "name"),
                title,
                firstText(
                        nestedText(position, "job-mid-code", "name"),
                        nestedText(position, "job-code", "name"),
                        nestedText(position, "industry", "name")),
                nestedText(position, "experience-level", "name"),
                nestedText(position, "location", "name"),
                description,
                nestedText(position, "required-education-level", "name"),
                null,
                optionalText(item, "url"),
                epochTime(item.path("posting-timestamp")),
                epochTime(item.path("expiration-timestamp")),
                item.path("active").asInt(0) == 1 ? JobPostingStatus.ACTIVE : JobPostingStatus.CLOSED
        );
    }

    private static String nestedText(JsonNode node, String... path) {
        JsonNode current = node;
        for (String segment : path) {
            current = current.path(segment);
        }
        return textOrNull(current);
    }

    private static String optionalText(JsonNode node, String field) {
        return textOrNull(node.path(field));
    }

    private static String textOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static LocalDateTime epochTime(JsonNode node) {
        String value = textOrNull(node);
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(value)), ZoneOffset.UTC);
    }

    private static int parseInteger(JsonNode node, int fallback) {
        String value = textOrNull(node);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
