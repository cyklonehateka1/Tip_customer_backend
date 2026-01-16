-- Create user_role_type enum if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role_type') THEN
        CREATE TYPE user_role_type AS ENUM ('USER', 'TIPSTER');
    ELSE
        -- If enum exists, drop and recreate to ensure correct values
        DROP TYPE IF EXISTS user_role_type CASCADE;
        CREATE TYPE user_role_type AS ENUM ('USER', 'TIPSTER');
    END IF;
END $$;

-- Drop the old check constraint if it exists
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;

-- Drop the default value first
ALTER TABLE users ALTER COLUMN role DROP DEFAULT;

-- Change the role column to use the enum type
-- Convert existing VARCHAR values to enum
ALTER TABLE users 
    ALTER COLUMN role TYPE user_role_type 
    USING CASE 
        WHEN role = 'user' THEN 'USER'::user_role_type
        WHEN role = 'tipster' THEN 'TIPSTER'::user_role_type
        ELSE 'USER'::user_role_type
    END;

-- Set default value
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER'::user_role_type;
