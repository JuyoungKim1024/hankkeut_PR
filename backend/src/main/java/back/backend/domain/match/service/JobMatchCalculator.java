package back.backend.domain.match.service;

import back.backend.domain.jobposting.entity.JobSkill;
import back.backend.domain.jobposting.entity.JobSkillType;
import back.backend.domain.jobposting.service.RuleBasedSkillExtractor;
import back.backend.domain.match.dto.JobMatchResponse.Skill;
import back.backend.domain.skill.entity.UserSkill;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;

final class JobMatchCalculator {
    private JobMatchCalculator() {
    }

    static Comparison compare(List<JobSkill> requirements, List<UserSkill> userSkills) {
        var owned = userSkills.stream().map(UserSkill::getSkillName)
                .map(RuleBasedSkillExtractor::canonicalName).map(JobMatchCalculator::key)
                .collect(Collectors.toSet());
        var byName = new TreeMap<String, Skill>();
        for (JobSkill requirement : requirements) {
            String name = RuleBasedSkillExtractor.canonicalName(requirement.getSkillName());
            String key = key(name);
            var skill = new Skill(name, requirement.getSkillType(), owned.contains(key));
            byName.merge(key, skill, (first, second) -> first.type() == JobSkillType.REQUIRED ? first : second);
        }
        List<Skill> skills = byName.values().stream()
                .sorted(Comparator.comparing(Skill::type).thenComparing(skill -> key(skill.name())))
                .toList();
        int totalWeight = skills.stream().mapToInt(JobMatchCalculator::weight).sum();
        int matchedWeight = skills.stream().filter(Skill::matched).mapToInt(JobMatchCalculator::weight).sum();
        return new Comparison(skills, matchedWeight, totalWeight);
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static int weight(Skill skill) {
        return skill.type() == JobSkillType.REQUIRED ? 2 : 1;
    }

    record Comparison(List<Skill> skills, int matchedWeight, int totalWeight) {
        BigDecimal score() {
            return BigDecimal.valueOf(matchedWeight * 100L)
                    .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
        }

        String explanation() {
            return "필수 기술 2점, 우대 기술 1점 기준으로 %d점 중 %d점을 충족했습니다."
                    .formatted(totalWeight, matchedWeight);
        }

        List<String> names(boolean matched) {
            return skills.stream().filter(skill -> skill.matched() == matched).map(Skill::name).toList();
        }

        String fingerprint() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (Skill skill : skills) {
                    String name = skill.name();
                    String input = "%s:%s:%d:%s".formatted(skill.type(), skill.matched(), name.length(), name);
                    digest.update(input.getBytes(StandardCharsets.UTF_8));
                }
                return HexFormat.of().formatHex(digest.digest());
            } catch (NoSuchAlgorithmException error) {
                throw new IllegalStateException("SHA-256 is unavailable", error);
            }
        }
    }
}
