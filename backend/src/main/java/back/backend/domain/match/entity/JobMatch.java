package back.backend.domain.match.entity;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.user.entity.User;
import back.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_match", uniqueConstraints =
        @UniqueConstraint(name = "uk_job_match_user_posting", columnNames = {"user_id", "job_posting_id"}))
public class JobMatch extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_skills", nullable = false, columnDefinition = "json")
    private List<String> matchedSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills", nullable = false, columnDefinition = "json")
    private List<String> missingSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "input_fingerprint", length = 64)
    private String inputFingerprint;

    protected JobMatch() {
    }

    public static JobMatch create(User user, JobPosting posting) {
        JobMatch match = new JobMatch();
        match.user = user;
        match.jobPosting = posting;
        return match;
    }

    public void update(BigDecimal score, List<String> matched, List<String> missing,
                       String explanation, String version, String fingerprint, LocalDateTime analyzedAt) {
        this.matchScore = score;
        this.matchedSkills = List.copyOf(matched);
        this.missingSkills = List.copyOf(missing);
        this.explanation = explanation;
        this.promptVersion = version;
        this.inputFingerprint = fingerprint;
        this.analyzedAt = analyzedAt;
    }

    public boolean matchesInput(String version, String fingerprint) {
        return version.equals(promptVersion) && fingerprint.equals(inputFingerprint);
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}
