package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSkillType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RuleBasedSkillExtractor {

    private static final List<SkillRule> RULES = List.of(
            rule("AWS", "aws|amazon\\s+web\\s+services"),
            rule("Azure", "azure"),
            rule("Docker", "docker"),
            rule("Elasticsearch", "elasticsearch|elastic\\s+search"),
            rule("FastAPI", "fastapi"),
            rule("GCP", "gcp|google\\s+cloud(?:\\s+platform)?"),
            rule("GitHub Actions", "github\\s+actions"),
            rule("Hibernate", "hibernate"),
            rule("Java", "java"),
            rule("JavaScript", "javascript"),
            rule("JPA", "jpa"),
            rule("Kafka", "kafka"),
            rule("Kotlin", "kotlin"),
            rule("Kubernetes", "kubernetes|k8s"),
            rule("MongoDB", "mongodb|mongo\\s*db"),
            rule("MySQL", "mysql"),
            rule("NestJS", "nestjs|nest\\.js"),
            rule("Next.js", "next\\.?js"),
            rule("Node.js", "node\\.?js"),
            rule("PostgreSQL", "postgresql|postgres"),
            rule("Python", "python"),
            rule("RabbitMQ", "rabbitmq|rabbit\\s*mq"),
            rule("React", "react(?:\\.js)?"),
            rule("Redis", "redis"),
            rule("Spring Boot", "spring\\s*boot"),
            rule("TypeScript", "typescript")
    );

    public List<ExtractedJobSkill> extract(String description, String qualification, String preference) {
        String requiredText = join(description, qualification);
        String preferredText = preference == null ? "" : preference;

        return RULES.stream()
                .map(rule -> extract(rule, requiredText, preferredText))
                .filter(result -> result != null)
                .sorted(Comparator.comparing(ExtractedJobSkill::type)
                        .thenComparing(ExtractedJobSkill::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static String canonicalName(String name) {
        String trimmed = name.strip();
        return RULES.stream()
                .filter(rule -> rule.pattern().matcher(trimmed).matches())
                .map(SkillRule::name)
                .findFirst()
                .orElse(trimmed);
    }

    private ExtractedJobSkill extract(SkillRule rule, String requiredText, String preferredText) {
        if (rule.pattern().matcher(requiredText).find()) {
            return new ExtractedJobSkill(rule.name(), JobSkillType.REQUIRED);
        }
        if (rule.pattern().matcher(preferredText).find()) {
            return new ExtractedJobSkill(rule.name(), JobSkillType.PREFERRED);
        }
        return null;
    }

    private String join(String first, String second) {
        return (first == null ? "" : first) + "\n" + (second == null ? "" : second);
    }

    private static SkillRule rule(String name, String expression) {
        Pattern pattern = Pattern.compile(
                "(?iu)(?<![A-Za-z0-9])(?:" + expression + ")(?![A-Za-z0-9])");
        return new SkillRule(name, pattern);
    }

    private record SkillRule(String name, Pattern pattern) {
    }
}
