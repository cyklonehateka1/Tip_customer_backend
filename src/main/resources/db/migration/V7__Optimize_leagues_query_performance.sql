-- Optimize leagues query performance
-- Add composite index for faster filtering by sport_id and is_active
-- This index supports the optimized query in LeagueRepository.findActiveLeaguesBySportGroupWithSport

CREATE INDEX IF NOT EXISTS idx_leagues_sport_active 
    ON leagues(sport_id, is_active) 
    WHERE is_active = true;

-- Add index on sport_group in sports table if not already exists (should be there from V6, but ensuring it)
CREATE INDEX IF NOT EXISTS idx_sports_group_active 
    ON sports(sport_group, is_active) 
    WHERE is_active = true;
