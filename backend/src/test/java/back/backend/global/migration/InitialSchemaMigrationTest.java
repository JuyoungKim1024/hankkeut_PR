package back.backend.global.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InitialSchemaMigrationTest {

    private static final Set<String> DOMAIN_TABLES = Set.of(
            "users",
            "profile",
            "resume",
            "user_skill",
            "job_posting",
            "job_skill",
            "job_match",
            "application",
            "learning_content",
            "interview",
            "cover_letter"
    );

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @DisplayName("t1 Flyway V1이 모든 초기 도메인 테이블을 생성한다")
    void t1_flywayCreatesAllInitialDomainTables() {
        Set<String> tables = jdbcClient.sql("""
                        SELECT LOWER(table_name)
                        FROM information_schema.tables
                        WHERE UPPER(table_schema) = 'PUBLIC'
                        """)
                .query(String.class)
                .set();

        assertThat(tables).containsAll(DOMAIN_TABLES);
    }

    @Test
    @DisplayName("t2 Flyway V1 마이그레이션이 성공 상태로 기록된다")
    void t2_flywayRecordsSuccessfulInitialMigration() {
        Boolean success = jdbcClient.sql("""
                        SELECT success
                        FROM flyway_schema_history
                        WHERE version = '1'
                        """)
                .query(Boolean.class)
                .single();

        assertThat(success).isTrue();
    }
}
