CREATE TABLE admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(100),
    phone_number VARCHAR(20),
    role admin_role_type NOT NULL DEFAULT 'admin',
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    email_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_by UUID REFERENCES admins(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(100),
    phone_number VARCHAR(20),
    avatar_url TEXT,
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    email_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role user_role_type NOT NULL DEFAULT 'customer',
    granted_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID REFERENCES users(id),
    
    UNIQUE(user_id, role)
);

CREATE TABLE tipsters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_ai BOOLEAN DEFAULT false,
    bio TEXT,
    avatar_url TEXT,
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    
    total_tips INTEGER DEFAULT 0,
    successful_tips INTEGER DEFAULT 0,
    total_earnings NUMERIC(10, 2) DEFAULT 0,
    success_rate NUMERIC(5, 2) DEFAULT 0,
    rating NUMERIC(5, 2) DEFAULT 0,
    
    kyc_status VARCHAR(20) DEFAULT 'not_applied',
    kyc_submitted_at TIMESTAMPTZ,
    kyc_approved_at TIMESTAMPTZ,
    kyc_rejected_at TIMESTAMPTZ,
    kyc_rejection_reason TEXT,
    
    payout_method VARCHAR(50),
    payout_details JSONB,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT tipsters_user_or_ai CHECK (
        (is_ai = true AND user_id IS NULL) OR 
        (is_ai = false AND user_id IS NOT NULL)
    ),
    CONSTRAINT tipsters_kyc_status CHECK (
        kyc_status IN ('not_applied', 'pending', 'approved', 'rejected')
    ),
    CONSTRAINT tipsters_success_rate_range CHECK (
        success_rate >= 0 AND success_rate <= 100
    ),
    CONSTRAINT tipsters_rating_range CHECK (
        rating >= 0 AND rating <= 5
    )
);

CREATE TABLE tipster_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    status VARCHAR(20) DEFAULT 'pending',
    
    identity_document_url TEXT,
    identity_document_type VARCHAR(50),
    proof_of_address_url TEXT,
    
    payout_method VARCHAR(50) NOT NULL,
    payout_details JSONB NOT NULL,
    
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    notes TEXT,
    
    submitted_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT tipster_applications_status CHECK (
        status IN ('pending', 'approved', 'rejected')
    )
);

CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    logo_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(external_id)
);

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(50),
    logo_url TEXT,
    country VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(external_id)
);

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(100),
    league_id UUID REFERENCES leagues(id),
    
    home_team_id UUID NOT NULL REFERENCES teams(id),
    away_team_id UUID NOT NULL REFERENCES teams(id),
    
    match_date TIMESTAMPTZ NOT NULL,
    status match_status_type DEFAULT 'scheduled',
    
    home_score INTEGER,
    away_score INTEGER,
    home_score_penalty INTEGER,
    away_score_penalty INTEGER,
    
    venue VARCHAR(255),
    referee VARCHAR(255),
    round VARCHAR(100),
    season VARCHAR(50),
    
    last_synced_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT matches_teams_different CHECK (home_team_id != away_team_id),
    CONSTRAINT matches_scores_valid CHECK (
        (status = 'finished' AND home_score IS NOT NULL AND away_score IS NOT NULL) OR
        (status != 'finished')
    ),
    UNIQUE(external_id)
);

CREATE TABLE tips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipster_id UUID NOT NULL REFERENCES tipsters(id) ON DELETE CASCADE,
    is_ai BOOLEAN DEFAULT false,
    
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    total_odds NUMERIC(8, 2),
    
    status tip_status_type DEFAULT 'pending',
    is_published BOOLEAN DEFAULT false,
    
    purchases_count INTEGER DEFAULT 0,
    total_revenue NUMERIC(10, 2) DEFAULT 0,
    
    published_at TIMESTAMPTZ,
    earliest_match_date TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT tips_price_positive CHECK (price > 0),
    CONSTRAINT tips_price_minimum CHECK (price >= 5.00),
    CONSTRAINT tips_odds_positive CHECK (total_odds IS NULL OR total_odds > 0),
    CONSTRAINT tips_purchases_non_negative CHECK (purchases_count >= 0)
);

CREATE TABLE tip_selections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tip_id UUID NOT NULL REFERENCES tips(id) ON DELETE CASCADE,
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    
    prediction_type prediction_type NOT NULL,
    prediction_value VARCHAR(100) NOT NULL,
    odds NUMERIC(8, 2),
    
    is_correct BOOLEAN,
    is_void BOOLEAN DEFAULT false,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(tip_id, match_id, prediction_type, prediction_value)
);

CREATE TABLE personal_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    title VARCHAR(255),
    total_stake NUMERIC(10, 2) NOT NULL,
    total_odds NUMERIC(8, 2),
    
    status tip_status_type DEFAULT 'pending',
    earliest_match_date TIMESTAMPTZ NOT NULL,
    
    created_at_12hr_before_match BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT personal_predictions_stake_minimum CHECK (total_stake >= 5.00),
    CONSTRAINT personal_predictions_12hr_rule CHECK (
        created_at <= (earliest_match_date - INTERVAL '12 hours')
    )
);

CREATE TABLE personal_prediction_selections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personal_prediction_id UUID NOT NULL REFERENCES personal_predictions(id) ON DELETE CASCADE,
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    
    prediction_type prediction_type NOT NULL,
    prediction_value VARCHAR(100) NOT NULL,
    odds NUMERIC(8, 2),
    
    is_correct BOOLEAN,
    is_void BOOLEAN DEFAULT false,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tip_id UUID NOT NULL REFERENCES tips(id) ON DELETE RESTRICT,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    amount NUMERIC(10, 2) NOT NULL,
    status purchase_status_type DEFAULT 'pending',
    
    payment_reference VARCHAR(255),
    payment_method VARCHAR(50),
    payment_gateway VARCHAR(50),
    
    tip_outcome tip_status_type,
    refunded_at TIMESTAMPTZ,
    refund_reason TEXT,
    
    purchased_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT purchases_amount_positive CHECK (amount > 0),
    CONSTRAINT purchases_no_duplicate UNIQUE(tip_id, buyer_id)
);

CREATE TABLE escrow (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_id UUID NOT NULL UNIQUE REFERENCES purchases(id) ON DELETE CASCADE,
    
    amount NUMERIC(10, 2) NOT NULL,
    status escrow_status_type DEFAULT 'pending',
    is_ai_tip BOOLEAN DEFAULT false,
    
    held_at TIMESTAMPTZ,
    released_at TIMESTAMPTZ,
    
    released_to UUID REFERENCES users(id),
    release_type VARCHAR(20),
    
    platform_fee NUMERIC(10, 2) DEFAULT 0,
    platform_fee_percentage NUMERIC(5, 2) DEFAULT 0,
    tipster_earnings NUMERIC(10, 2) DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT escrow_amount_positive CHECK (amount > 0)
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    tipster_id UUID REFERENCES tipsters(id) ON DELETE SET NULL,
    
    type transaction_type NOT NULL,
    status transaction_status_type DEFAULT 'pending',
    
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GHS',
    
    purchase_id UUID REFERENCES purchases(id),
    escrow_id UUID REFERENCES escrow(id),
    
    payment_reference VARCHAR(255),
    payment_method VARCHAR(50),
    
    description TEXT,
    metadata JSONB,
    
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT transactions_amount_positive CHECK (amount > 0)
);

CREATE TABLE user_wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    
    balance NUMERIC(10, 2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'GHS',
    
    version INTEGER DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT user_wallets_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipster_id UUID NOT NULL REFERENCES tipsters(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purchase_id UUID REFERENCES purchases(id) ON DELETE SET NULL,
    
    rating INTEGER NOT NULL,
    review_text TEXT,
    
    is_verified BOOLEAN DEFAULT false,
    is_visible BOOLEAN DEFAULT true,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ratings_rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT ratings_one_per_purchase UNIQUE(user_id, tipster_id, purchase_id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMPTZ,
    
    reference_id UUID,
    reference_type VARCHAR(50),
    
    metadata JSONB,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reported_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    type report_type NOT NULL,
    status report_status_type DEFAULT 'pending',
    
    reported_user_id UUID REFERENCES users(id),
    reported_tip_id UUID REFERENCES tips(id),
    reported_purchase_id UUID REFERENCES purchases(id),
    
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    value_type VARCHAR(20) DEFAULT 'string',
    description TEXT,
    category VARCHAR(50),
    is_public BOOLEAN DEFAULT false,
    
    updated_by UUID REFERENCES users(id),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT platform_settings_value_type CHECK (
        value_type IN ('string', 'number', 'boolean', 'json')
    )
);

