-- Add logo_url column to leagues table
ALTER TABLE leagues ADD COLUMN IF NOT EXISTS logo_url TEXT;
