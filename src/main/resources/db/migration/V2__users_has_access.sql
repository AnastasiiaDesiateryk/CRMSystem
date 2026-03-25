ALTER TABLE users
    ADD COLUMN IF NOT EXISTS has_access boolean NOT NULL DEFAULT false;
