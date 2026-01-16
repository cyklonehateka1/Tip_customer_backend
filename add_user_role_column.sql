-- Add role column to users table
-- Roles: 'user' or 'tipster'
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'user';

-- Add check constraint to ensure only valid roles
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('user', 'tipster'));

-- Update existing users to have 'user' role if they don't have one
UPDATE users SET role = 'user' WHERE role IS NULL OR role = '';
