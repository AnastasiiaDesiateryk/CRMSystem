-- Users table
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- user roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role TEXT NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
ALTER TABLE user_roles
ADD CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash varchar(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    rotated_from_id UUID,
    user_agent TEXT,
    ip TEXT,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_rotated_from FOREIGN KEY (rotated_from_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);
CREATE INDEX idx_refresh_user_id ON refresh_tokens(user_id);
CREATE UNIQUE INDEX idx_refresh_token_hash_unique ON refresh_tokens(token_hash);

-- =========================
-- organizations
-- =========================
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version           bigint NOT NULL DEFAULT 0,

    name              varchar(200) NOT NULL,
    website           varchar(500),
    website_status    varchar(50),
    linkedin_url      varchar(500),
    country_region    varchar(200),
    email             varchar(320),
    category          varchar(120),
    status            varchar(50),
    notes             text,
    preferred_language varchar(5),

    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_org_name ON organizations (name);
CREATE INDEX IF NOT EXISTS idx_org_category ON organizations (category);
CREATE INDEX IF NOT EXISTS idx_org_status ON organizations (status);
CREATE INDEX IF NOT EXISTS idx_org_country_region ON organizations (country_region);

-- optional: fast search by email
CREATE INDEX IF NOT EXISTS idx_org_email ON organizations (email);

-- =========================
-- contacts
-- =========================
CREATE TABLE IF NOT EXISTS contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version           bigint NOT NULL DEFAULT 0,

    organization_id   uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,

    name              varchar(200) NOT NULL,
    role_position     varchar(200),
    email             varchar(320),
    preferred_language varchar(5),
    notes             text,

    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_contact_org_id ON contacts (organization_id);
CREATE INDEX IF NOT EXISTS idx_contact_email ON contacts (email);

-- =========================
-- updated_at trigger (DB-side)
-- =========================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_organizations_updated_at ON organizations;
CREATE TRIGGER trg_organizations_updated_at
BEFORE UPDATE ON organizations
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_contacts_updated_at ON contacts;
CREATE TRIGGER trg_contacts_updated_at
BEFORE UPDATE ON contacts
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
