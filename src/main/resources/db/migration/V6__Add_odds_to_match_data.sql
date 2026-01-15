
ALTER TABLE match_data 
ADD COLUMN IF NOT EXISTS odds TEXT;

COMMENT ON COLUMN match_data.odds IS 'Stores betting odds from various markets (match_result, over_under, btts, etc.) as stringified JSON';
