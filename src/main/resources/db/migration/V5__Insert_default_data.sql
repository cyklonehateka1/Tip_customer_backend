INSERT INTO tipsters (id, is_ai, is_verified, is_active, bio, rating, success_rate)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    true,
    true,
    true,
    'AI-powered prediction system',
    0,
    0
) ON CONFLICT DO NOTHING;

INSERT INTO platform_settings (key, value, value_type, description, category, is_public) VALUES
('platform_fee_percentage', '10', 'number', 'Platform fee percentage for tipster payouts', 'fees', true),
('minimum_tip_price', '5.00', 'number', 'Minimum price for a tip in GHS', 'limits', true),
('minimum_stake_personal_prediction', '5.00', 'number', 'Minimum stake for personal predictions in GHS', 'limits', true),
('tip_publish_12hr_rule', 'true', 'boolean', 'Tips must be published 12 hours before match', 'rules', true),
('ai_predictions_enabled', 'true', 'boolean', 'Enable AI predictions feature', 'features', true),
('ai_prediction_frequency', 'daily', 'string', 'How often to generate AI predictions', 'features', false),
('tipster_eligibility_min_wins', '5', 'number', 'Minimum wins in last 15 predictions', 'rules', true),
('tipster_eligibility_min_predictions', '15', 'number', 'Minimum predictions for eligibility check', 'rules', true)
ON CONFLICT (key) DO NOTHING;

