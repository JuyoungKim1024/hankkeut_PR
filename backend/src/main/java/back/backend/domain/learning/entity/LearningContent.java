package back.backend.domain.learning.entity;

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

@Entity
@Table(name = "learning_content", uniqueConstraints =
        @UniqueConstraint(name = "uk_learning_user_posting_skill", columnNames = {"user_id", "job_posting_id", "skill_name"}))
public class LearningContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    protected LearningContent() {
    }
}
