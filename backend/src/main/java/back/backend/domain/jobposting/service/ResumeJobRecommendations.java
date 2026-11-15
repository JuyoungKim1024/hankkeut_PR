package back.backend.domain.jobposting.service;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

final class ResumeJobRecommendations {
    private final RuleBasedSkillExtractor extractor;

    ResumeJobRecommendations(RuleBasedSkillExtractor extractor) {
        this.extractor = extractor;
    }

    List<Recommendation> rank(JsonNode board, String resume, String confirmedSkills) {
        var owned = skills(resume, confirmedSkills);
        var matches = new ArrayList<Recommendation>();
        for (JsonNode entry : board.path("jobs")) {
            String title = entry.path("title").asString("");
            String url = entry.path("absolute_url").asString("");
            if (title.isBlank() || !entry.path("internal_job_id").isIntegralNumber() || !isWebUrl(url)) continue;
            String html = entry.path("content").asString("");
            String text = Jsoup.parse(Parser.unescapeEntities(html, false)).text();
            var required = skills(text, title);
            var matched = required.stream().filter(owned::contains).toList();
            if (!matched.isEmpty()) {
                var missing = required.stream().filter(skill -> !owned.contains(skill)).toList();
                int score = Math.round(100f * matched.size() / required.size());
                matches.add(new Recommendation(title, entry.path("location").path("name").asString(""), url,
                        score, matched, missing));
            }
        }
        return matches.stream().sorted(Comparator.comparingInt(Recommendation::score).reversed()
                        .thenComparing(Comparator.comparingInt((Recommendation item) -> item.matchedSkills().size()).reversed())
                        .thenComparing(Recommendation::title).thenComparing(Recommendation::url))
                .limit(20).toList();
    }

    List<String> detectedSkills(String text, String confirmedSkills) {
        return List.copyOf(skills(text, confirmedSkills));
    }

    private LinkedHashSet<String> skills(String text, String additional) {
        var names = new LinkedHashSet<String>();
        extractor.extract(text, additional, null).forEach(skill -> names.add(skill.name()));
        return names;
    }

    private static boolean isWebUrl(String url) {
        try {
            URI parsed = URI.create(url);
            return "https".equalsIgnoreCase(parsed.getScheme()) && parsed.getHost() != null
                    && parsed.getRawUserInfo() == null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    record Recommendation(String title, String location, String url, int score,
                          List<String> matchedSkills, List<String> missingSkills) {}
}
