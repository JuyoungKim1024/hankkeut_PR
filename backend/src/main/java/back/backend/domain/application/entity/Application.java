package back.backend.domain.application.entity;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.user.entity.User;
import back.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "application", uniqueConstraints =
        @UniqueConstraint(name = "uk_application_user_posting", columnNames = {"user_id", "job_posting_id"}))
public class Application extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(columnDefinition = "LONGTEXT")
    private String memo;

    @Column(name = "next_action_at")
    private LocalDateTime nextActionAt;

    protected Application() {
    }

    public static Application create(User user,
                                     JobPosting jobPosting,
                                     ApplicationStatus status,
                                     String memo,
                                     LocalDateTime nextActionAt) {
        Application application = new Application();
        application.user = user;
        application.jobPosting = jobPosting;
        application.status = status;
        application.memo = normalizeMemo(memo);
        application.nextActionAt = nextActionAt;
        if (status == ApplicationStatus.APPLIED) {
            application.appliedAt = LocalDateTime.now();
        }
        return application;
    }

    public boolean canTransitionTo(ApplicationStatus next) {
        return status.canTransitionTo(next);
    }

    public void update(ApplicationStatus nextStatus, String memo, LocalDateTime nextActionAt) {
        if (status != ApplicationStatus.APPLIED && nextStatus == ApplicationStatus.APPLIED) {
            appliedAt = LocalDateTime.now();
        }
        status = nextStatus;
        this.memo = normalizeMemo(memo);
        this.nextActionAt = nextActionAt;
    }

    private static String normalizeMemo(String memo) {
        if (memo == null) return null;
        String normalized = memo.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public Long getId() {
        return id;
    }

    public JobPosting getJobPosting() {
        return jobPosting;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getNextActionAt() {
        return nextActionAt;
    }
}
