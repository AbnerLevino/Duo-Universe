ALTER TABLE users ADD COLUMN google_sub VARCHAR(255) NOT NULL;
CREATE UNIQUE INDEX users_google_sub_unique ON users(google_sub);