CREATE INDEX idx_admins_email ON admins(email);
CREATE INDEX idx_admins_role ON admins(role);
CREATE INDEX idx_admins_is_active ON admins(is_active);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_display_name ON users(display_name);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

CREATE INDEX idx_tipsters_user_id ON tipsters(user_id);
CREATE INDEX idx_tipsters_is_ai ON tipsters(is_ai);
CREATE INDEX idx_tipsters_is_verified ON tipsters(is_verified);
CREATE INDEX idx_tipsters_kyc_status ON tipsters(kyc_status);
CREATE INDEX idx_tipsters_rating ON tipsters(rating DESC);
CREATE INDEX idx_tipsters_success_rate ON tipsters(success_rate DESC);

CREATE INDEX idx_tipster_applications_user_id ON tipster_applications(user_id);
CREATE INDEX idx_tipster_applications_status ON tipster_applications(status);
CREATE INDEX idx_tipster_applications_submitted_at ON tipster_applications(submitted_at DESC);

CREATE INDEX idx_leagues_name ON leagues(name);
CREATE INDEX idx_leagues_country ON leagues(country);
CREATE INDEX idx_leagues_is_active ON leagues(is_active);

CREATE INDEX idx_teams_name ON teams(name);
CREATE INDEX idx_teams_country ON teams(country);

CREATE INDEX idx_matches_league_id ON matches(league_id);
CREATE INDEX idx_matches_home_team_id ON matches(home_team_id);
CREATE INDEX idx_matches_away_team_id ON matches(away_team_id);
CREATE INDEX idx_matches_match_date ON matches(match_date);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_external_id ON matches(external_id);
CREATE INDEX idx_matches_date_status ON matches(match_date, status);

CREATE INDEX idx_tips_tipster_id ON tips(tipster_id);
CREATE INDEX idx_tips_is_ai ON tips(is_ai);
CREATE INDEX idx_tips_status ON tips(status);
CREATE INDEX idx_tips_is_published ON tips(is_published);
CREATE INDEX idx_tips_published_at ON tips(published_at DESC);
CREATE INDEX idx_tips_earliest_match_date ON tips(earliest_match_date);
CREATE INDEX idx_tips_status_published ON tips(status, is_published);
CREATE INDEX idx_tips_created_at ON tips(created_at DESC);

CREATE INDEX idx_tip_selections_tip_id ON tip_selections(tip_id);
CREATE INDEX idx_tip_selections_match_id ON tip_selections(match_id);
CREATE INDEX idx_tip_selections_prediction_type ON tip_selections(prediction_type);

CREATE INDEX idx_personal_predictions_user_id ON personal_predictions(user_id);
CREATE INDEX idx_personal_predictions_status ON personal_predictions(status);
CREATE INDEX idx_personal_predictions_earliest_match_date ON personal_predictions(earliest_match_date);
CREATE INDEX idx_personal_predictions_user_status ON personal_predictions(user_id, status);

CREATE INDEX idx_personal_prediction_selections_prediction_id ON personal_prediction_selections(personal_prediction_id);
CREATE INDEX idx_personal_prediction_selections_match_id ON personal_prediction_selections(match_id);

CREATE INDEX idx_purchases_tip_id ON purchases(tip_id);
CREATE INDEX idx_purchases_buyer_id ON purchases(buyer_id);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_purchases_purchased_at ON purchases(purchased_at DESC);
CREATE INDEX idx_purchases_tip_outcome ON purchases(tip_outcome);

CREATE INDEX idx_escrow_purchase_id ON escrow(purchase_id);
CREATE INDEX idx_escrow_status ON escrow(status);
CREATE INDEX idx_escrow_is_ai_tip ON escrow(is_ai_tip);
CREATE INDEX idx_escrow_held_at ON escrow(held_at);
CREATE INDEX idx_escrow_released_at ON escrow(released_at);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_tipster_id ON transactions(tipster_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_purchase_id ON transactions(purchase_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_user_type ON transactions(user_id, type);

CREATE INDEX idx_user_wallets_user_id ON user_wallets(user_id);

CREATE INDEX idx_ratings_tipster_id ON ratings(tipster_id);
CREATE INDEX idx_ratings_user_id ON ratings(user_id);
CREATE INDEX idx_ratings_purchase_id ON ratings(purchase_id);
CREATE INDEX idx_ratings_rating ON ratings(rating);
CREATE INDEX idx_ratings_created_at ON ratings(created_at DESC);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = false;

CREATE INDEX idx_reports_reported_by ON reports(reported_by);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_type ON reports(type);
CREATE INDEX idx_reports_created_at ON reports(created_at DESC);

CREATE INDEX idx_platform_settings_key ON platform_settings(key);
CREATE INDEX idx_platform_settings_category ON platform_settings(category);
CREATE INDEX idx_platform_settings_is_public ON platform_settings(is_public);

