-- Add role column back to user_roles table
ALTER TABLE user_roles 
    ADD COLUMN IF NOT EXISTS role user_role_type NOT NULL DEFAULT 'USER'::user_role_type;

-- Recreate the index on role column
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- Recreate the unique constraint
ALTER TABLE user_roles 
    DROP CONSTRAINT IF EXISTS uk_user_roles_user_role;
    
ALTER TABLE user_roles 
    ADD CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role);
