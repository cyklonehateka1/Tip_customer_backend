CREATE OR REPLACE VIEW tipster_leaderboard AS
SELECT 
    t.id,
    t.user_id,
    t.is_ai,
    CASE 
        WHEN t.is_ai THEN 'AI System'
        ELSE u.display_name
    END AS display_name,
    t.total_tips,
    t.successful_tips,
    t.success_rate,
    t.rating,
    t.total_earnings,
    COUNT(DISTINCT r.id) AS total_reviews,
    t.created_at
FROM tipsters t
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN ratings r ON t.id = r.tipster_id AND r.is_visible = true
WHERE t.is_active = true AND t.is_verified = true
GROUP BY t.id, t.user_id, t.is_ai, u.display_name, t.total_tips, 
         t.successful_tips, t.success_rate, t.rating, t.total_earnings, t.created_at
ORDER BY t.success_rate DESC, t.rating DESC, t.total_tips DESC;

CREATE OR REPLACE VIEW user_leaderboard AS
SELECT 
    u.id,
    u.display_name,
    COUNT(DISTINCT pp.id) AS total_predictions,
    COUNT(DISTINCT CASE WHEN pp.status = 'won' THEN pp.id END) AS wins,
    COUNT(DISTINCT CASE WHEN pp.status = 'lost' THEN pp.id END) AS losses,
    CASE 
        WHEN COUNT(DISTINCT pp.id) > 0 
        THEN ROUND((COUNT(DISTINCT CASE WHEN pp.status = 'won' THEN pp.id END)::NUMERIC / 
                   COUNT(DISTINCT pp.id)::NUMERIC) * 100, 2)
        ELSE 0
    END AS win_rate,
    SUM(CASE WHEN pp.status = 'won' THEN pp.total_stake ELSE 0 END) AS total_winnings
FROM users u
LEFT JOIN personal_predictions pp ON u.id = pp.user_id
WHERE u.is_active = true
GROUP BY u.id, u.display_name
HAVING COUNT(DISTINCT pp.id) > 0
ORDER BY win_rate DESC, wins DESC, total_predictions DESC;

CREATE OR REPLACE FUNCTION update_tipster_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IN ('won', 'lost', 'void') AND 
       (OLD.status IS NULL OR OLD.status = 'pending') THEN
        
        UPDATE tipsters
        SET 
            total_tips = (
                SELECT COUNT(*) 
                FROM tips 
                WHERE tipster_id = NEW.tipster_id 
                AND status IN ('won', 'lost', 'void')
            ),
            successful_tips = (
                SELECT COUNT(*) 
                FROM tips 
                WHERE tipster_id = NEW.tipster_id 
                AND status = 'won'
            ),
            success_rate = CASE 
                WHEN (
                    SELECT COUNT(*) 
                    FROM tips 
                    WHERE tipster_id = NEW.tipster_id 
                    AND status IN ('won', 'lost', 'void')
                ) > 0
                THEN ROUND((
                    SELECT COUNT(*) 
                    FROM tips 
                    WHERE tipster_id = NEW.tipster_id 
                    AND status = 'won'
                )::NUMERIC / (
                    SELECT COUNT(*) 
                    FROM tips 
                    WHERE tipster_id = NEW.tipster_id 
                    AND status IN ('won', 'lost', 'void')
                )::NUMERIC * 100, 2)
                ELSE 0
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.tipster_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_tipster_stats
AFTER UPDATE OF status ON tips
FOR EACH ROW
EXECUTE FUNCTION update_tipster_stats();

CREATE OR REPLACE FUNCTION check_tipster_eligibility(p_user_id UUID)
RETURNS TABLE (
    is_eligible BOOLEAN,
    total_predictions BIGINT,
    wins BIGINT,
    meets_min_wins BOOLEAN,
    meets_min_stake BOOLEAN,
    meets_12hr_rule BOOLEAN
) AS $$
DECLARE
    v_min_wins INTEGER := 5;
    v_min_predictions INTEGER := 15;
    v_min_stake NUMERIC := 5.00;
    v_total_predictions BIGINT;
    v_wins BIGINT;
    v_all_meet_stake BOOLEAN;
    v_all_meet_12hr BOOLEAN;
BEGIN
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'won')
    INTO v_total_predictions, v_wins
    FROM personal_predictions
    WHERE user_id = p_user_id
    AND status IN ('won', 'lost', 'void')
    ORDER BY created_at DESC
    LIMIT v_min_predictions;
    
    SELECT 
        COUNT(*) = 0 OR BOOL_AND(total_stake >= v_min_stake)
    INTO v_all_meet_stake
    FROM (
        SELECT total_stake
        FROM personal_predictions
        WHERE user_id = p_user_id
        AND status IN ('won', 'lost', 'void')
        ORDER BY created_at DESC
        LIMIT v_min_predictions
    ) last_predictions;
    
    SELECT 
        COUNT(*) = 0 OR BOOL_AND(created_at <= (earliest_match_date - INTERVAL '12 hours'))
    INTO v_all_meet_12hr
    FROM (
        SELECT created_at, earliest_match_date
        FROM personal_predictions
        WHERE user_id = p_user_id
        AND status IN ('won', 'lost', 'void')
        ORDER BY created_at DESC
        LIMIT v_min_predictions
    ) last_predictions;
    
    RETURN QUERY SELECT 
        (v_total_predictions >= v_min_predictions AND 
         v_wins >= v_min_wins AND 
         v_all_meet_stake AND 
         v_all_meet_12hr) AS is_eligible,
        v_total_predictions AS total_predictions,
        v_wins AS wins,
        (v_wins >= v_min_wins) AS meets_min_wins,
        COALESCE(v_all_meet_stake, false) AS meets_min_stake,
        COALESCE(v_all_meet_12hr, false) AS meets_12hr_rule;
END;
$$ LANGUAGE plpgsql;

