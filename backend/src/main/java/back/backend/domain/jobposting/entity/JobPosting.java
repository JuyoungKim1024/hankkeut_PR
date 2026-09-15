package back.backend.domain.jobposting.entity;

import back.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting", uniqueConstraints =
        @UniqueConstraint(name = "uk_job_posting_source_external", columnNames = {"source", "external_id"}))
public class JobPosting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobSource source;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "job_category", nullable = false, length = 100)
    private String jobCategory;

    @Column(length = 100)
    private String career;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String qualification;

    @Column(columnDefinition = "LONGTEXT")
    private String preference;

    @Column(name = "original_url", nullable = false, length = 1000)
    private String originalUrl;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobPostingStatus status;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    protected JobPosting() {
    }
}
