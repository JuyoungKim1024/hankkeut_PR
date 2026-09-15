CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_provider_identity UNIQUE (provider, provider_user_id)
);

CREATE TABLE profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    desired_job VARCHAR(100) NOT NULL,
    career_level VARCHAR(30) NOT NULL,
    desired_location VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_profile PRIMARY KEY (id),
    CONSTRAINT uk_profile_user UNIQUE (user_id),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE resume (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_type VARCHAR(30) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    extracted_text LONGTEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_resume PRIMARY KEY (id),
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_resume_user_created ON resume (user_id, created_at);

CREATE TABLE user_skill (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_level VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_skill PRIMARY KEY (id),
    CONSTRAINT uk_user_skill_name UNIQUE (user_id, skill_name),
    CONSTRAINT fk_user_skill_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE job_posting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(255) NOT NULL,
    source VARCHAR(30) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    job_category VARCHAR(100) NOT NULL,
    career VARCHAR(100),
    location VARCHAR(255),
    description LONGTEXT NOT NULL,
    qualification LONGTEXT,
    preference LONGTEXT,
    original_url VARCHAR(1000) NOT NULL,
    posted_at DATETIME(6),
    expired_at DATETIME(6),
    status VARCHAR(30) NOT NULL,
    content_hash VARCHAR(64),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_job_posting PRIMARY KEY (id),
    CONSTRAINT uk_job_posting_source_external UNIQUE (source, external_id)
);
CREATE INDEX idx_job_posting_status_expired ON job_posting (status, expired_at);
CREATE INDEX idx_job_posting_category_location ON job_posting (job_category, location);

CREATE TABLE job_skill (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_posting_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_type VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_job_skill PRIMARY KEY (id),
    CONSTRAINT uk_job_skill_type UNIQUE (job_posting_id, skill_name, skill_type),
    CONSTRAINT fk_job_skill_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE
);
CREATE INDEX idx_job_skill_name ON job_skill (skill_name);

CREATE TABLE job_match (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    match_score DECIMAL(5,2) NOT NULL,
    matched_skills JSON NOT NULL,
    missing_skills JSON NOT NULL,
    explanation LONGTEXT,
    prompt_version VARCHAR(50) NOT NULL,
    analyzed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_job_match PRIMARY KEY (id),
    CONSTRAINT uk_job_match_user_posting UNIQUE (user_id, job_posting_id),
    CONSTRAINT fk_job_match_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_match_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE,
    CONSTRAINT ck_job_match_score CHECK (match_score >= 0 AND match_score <= 100)
);

CREATE TABLE application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    applied_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_application PRIMARY KEY (id),
    CONSTRAINT uk_application_user_posting UNIQUE (user_id, job_posting_id),
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE
);
CREATE INDEX idx_application_user_status ON application (user_id, status);

CREATE TABLE learning_content (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    content LONGTEXT NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_learning_content PRIMARY KEY (id),
    CONSTRAINT uk_learning_user_posting_skill UNIQUE (user_id, job_posting_id, skill_name),
    CONSTRAINT fk_learning_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE
);

CREATE TABLE interview (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    questions JSON NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_interview PRIMARY KEY (id),
    CONSTRAINT uk_interview_user_posting UNIQUE (user_id, job_posting_id),
    CONSTRAINT fk_interview_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE
);

CREATE TABLE cover_letter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    question VARCHAR(1000) NOT NULL,
    content LONGTEXT NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_cover_letter PRIMARY KEY (id),
    CONSTRAINT fk_cover_letter_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_cover_letter_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting (id) ON DELETE CASCADE
);
CREATE INDEX idx_cover_letter_user_posting ON cover_letter (user_id, job_posting_id);
