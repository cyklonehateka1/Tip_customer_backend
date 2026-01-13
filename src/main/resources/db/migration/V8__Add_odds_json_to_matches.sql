-- Add JSON column to store odds data from external APIs
ALTER TABLE matches ADD COLUMN IF NOT EXISTS odds_json JSONB;

-- Add index for JSON queries (if needed in the future)
CREATE INDEX IF NOT EXISTS idx_matches_odds_json ON matches USING GIN (odds_json);

-- Add comment
COMMENT ON COLUMN matches.odds_json IS 'Stores complete odds data from external APIs (The Odds API) as JSON, including all markets and bookmakers';
