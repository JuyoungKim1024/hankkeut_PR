DROP TABLE refresh_token;

ALTER TABLE users DROP INDEX uk_users_email;
ALTER TABLE users DROP INDEX uk_users_provider_identity;
ALTER TABLE users DROP COLUMN email;
ALTER TABLE users DROP COLUMN password;
ALTER TABLE users DROP COLUMN provider;
ALTER TABLE users DROP COLUMN provider_user_id;
