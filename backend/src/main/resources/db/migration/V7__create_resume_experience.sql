CREATE TABLE resume_experience (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    project VARCHAR(150) NOT NULL,
    experience_role VARCHAR(150) NOT NULL,
    skills VARCHAR(300) NOT NULL,
    outcome VARCHAR(500) NOT NULL,
    evidence VARCHAR(1000) NOT NULL,
    CONSTRAINT fk_experience_resume FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE
);
CREATE INDEX idx_resume_experience_resume ON resume_experience(resume_id);
