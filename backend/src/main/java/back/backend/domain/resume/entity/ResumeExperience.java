package back.backend.domain.resume.entity;

import back.backend.domain.resume.dto.ExperienceInput;
import jakarta.persistence.*;

@Entity
@Table(name = "resume_experience")
public class ResumeExperience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id")
    private Resume resume;
    @Column(nullable = false, length = 150)
    private String project;
    @Column(name = "experience_role", nullable = false, length = 150)
    private String role;
    @Column(nullable = false, length = 300)
    private String skills;
    @Column(nullable = false, length = 500)
    private String outcome;
    @Column(nullable = false, length = 1000)
    private String evidence;

    protected ResumeExperience() {}

    public ResumeExperience(Resume resume, ExperienceInput input) {
        this.resume = resume;
        update(input);
    }

    public void update(ExperienceInput input) {
        project = input.project().strip();
        role = input.role().strip();
        skills = input.skills() == null ? "" : input.skills().strip();
        outcome = input.outcome() == null ? "" : input.outcome().strip();
        evidence = input.evidence().strip();
    }

    public Long id() { return id; }

    public ExperienceInput content() {
        return new ExperienceInput(project, role, skills, outcome, evidence);
    }
}
