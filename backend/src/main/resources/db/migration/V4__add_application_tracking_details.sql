ALTER TABLE application ADD COLUMN memo LONGTEXT NULL;
ALTER TABLE application ADD COLUMN next_action_at DATETIME(6) NULL;

CREATE INDEX idx_application_user_next_action ON application (user_id, next_action_at);
