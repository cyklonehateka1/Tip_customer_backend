CREATE TYPE user_role_type AS ENUM ('tipster', 'customer');
CREATE TYPE admin_role_type AS ENUM ('admin', 'manager', 'superadmin');
CREATE TYPE match_status_type AS ENUM ('scheduled', 'live', 'finished', 'cancelled', 'postponed');
CREATE TYPE tip_status_type AS ENUM ('pending', 'won', 'lost', 'void', 'cancelled');
CREATE TYPE prediction_type AS ENUM (
    'match_result',
    'over_under',
    'both_teams_to_score',
    'double_chance',
    'handicap',
    'correct_score',
    'first_goal_scorer',
    'any_other'
);
CREATE TYPE purchase_status_type AS ENUM ('pending', 'completed', 'failed', 'cancelled', 'refunded');
CREATE TYPE escrow_status_type AS ENUM ('pending', 'held', 'released', 'refunded');
CREATE TYPE transaction_type AS ENUM (
    'purchase',
    'tipster_payout',
    'customer_refund',
    'platform_revenue',
    'withdrawal',
    'deposit',
    'fee'
);
CREATE TYPE transaction_status_type AS ENUM ('pending', 'completed', 'failed', 'cancelled');
CREATE TYPE notification_type AS ENUM (
    'tip_published',
    'tip_purchased',
    'tip_outcome',
    'refund_received',
    'payout_received',
    'application_status',
    'new_tip_available',
    'system_alert'
);
CREATE TYPE report_status_type AS ENUM ('pending', 'under_review', 'resolved', 'dismissed');
CREATE TYPE report_type AS ENUM (
    'tipster_misconduct',
    'fraud',
    'inappropriate_content',
    'payment_issue',
    'technical_issue',
    'other'
);

