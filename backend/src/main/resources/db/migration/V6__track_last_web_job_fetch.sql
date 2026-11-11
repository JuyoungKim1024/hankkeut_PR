ALTER TABLE job_posting ADD COLUMN last_fetched_at DATETIME(6);
UPDATE job_posting SET last_fetched_at = updated_at WHERE source = 'WEB';
