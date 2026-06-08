ALTER TABLE contacts
    ADD COLUMN is_primary_email boolean NOT NULL DEFAULT false;