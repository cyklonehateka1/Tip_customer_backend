-- Create providers table
CREATE TABLE providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    base_url TEXT NOT NULL,
    api_key TEXT,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    rate_limit_per_minute INTEGER,
    rate_limit_per_day INTEGER,
    configuration JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for providers
CREATE INDEX idx_providers_name ON providers(name);
CREATE INDEX idx_providers_code ON providers(code);
CREATE INDEX idx_providers_is_active ON providers(is_active);

-- Create sports table
CREATE TABLE sports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sport_key VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sport_group VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    has_outrights BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for sports
CREATE INDEX idx_sports_group ON sports(sport_group);
CREATE INDEX idx_sports_key ON sports(sport_key);
CREATE INDEX idx_sports_is_active ON sports(is_active);

-- Add description column to leagues table if it doesn't exist
ALTER TABLE leagues 
    ADD COLUMN IF NOT EXISTS description TEXT;

-- Add provider_id and sport_id to leagues table (nullable initially for existing data)
ALTER TABLE leagues 
    ADD COLUMN IF NOT EXISTS provider_id UUID REFERENCES providers(id) ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS sport_id UUID REFERENCES sports(id) ON DELETE RESTRICT;

-- Drop old unique constraint on external_id if it exists
ALTER TABLE leagues DROP CONSTRAINT IF EXISTS uk_leagues_external_id;

-- Create a partial unique index for provider_id and external_id
-- This ensures uniqueness only when both values are present
-- NULL values are ignored, allowing existing leagues without provider/sport to coexist
CREATE UNIQUE INDEX uk_leagues_provider_external_id 
    ON leagues(provider_id, external_id) 
    WHERE provider_id IS NOT NULL AND external_id IS NOT NULL;

-- Create indexes for leagues
CREATE INDEX IF NOT EXISTS idx_leagues_provider_id ON leagues(provider_id);
CREATE INDEX IF NOT EXISTS idx_leagues_sport_id ON leagues(sport_id);

-- Insert default provider (The Odds API)
INSERT INTO providers (code, name, base_url, description, is_active, created_at, updated_at)
VALUES (
    'THE_ODDS_API',
    'The Odds API',
    'https://api.the-odds-api.com/v4',
    'The Odds API provides sports odds data from multiple bookmakers',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
