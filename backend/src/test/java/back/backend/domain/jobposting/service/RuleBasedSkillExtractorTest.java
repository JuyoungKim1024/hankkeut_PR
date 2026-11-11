package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSkillType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedSkillExtractorTest {

    private final RuleBasedSkillExtractor extractor = new RuleBasedSkillExtractor();

    @Test
    @DisplayName("t1 자격요건과 우대사항에서 기술을 유형별로 추출한다")
    void t1_skillsAreExtractedByRequirementType() {
        var result = extractor.extract(
                "Docker 기반 백엔드 서비스",
                "Java와 Spring Boot, MySQL 경험",
                "Redis 또는 Docker 운영 경험"
        );

        assertThat(result).containsExactly(
                new ExtractedJobSkill("Docker", JobSkillType.REQUIRED),
                new ExtractedJobSkill("Java", JobSkillType.REQUIRED),
                new ExtractedJobSkill("MySQL", JobSkillType.REQUIRED),
                new ExtractedJobSkill("Spring Boot", JobSkillType.REQUIRED),
                new ExtractedJobSkill("Redis", JobSkillType.PREFERRED)
        );
    }

    @Test
    @DisplayName("t2 JavaScript 문자열을 Java 기술로 잘못 추출하지 않는다")
    void t2_javascriptDoesNotMatchJava() {
        var result = extractor.extract("JavaScript와 TypeScript", null, null);

        assertThat(result).extracting(ExtractedJobSkill::name)
                .containsExactly("JavaScript", "TypeScript");
    }
}
