-- Supabase/Postgres schema for leaderboard
-- Stores a user's display name and score. A trigger will set user_id from JWT.

CREATE TABLE public.leaderboard (
  user_id uuid PRIMARY KEY,
  user_name text NOT NULL,
  total_score integer NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);

-- Index for fast top queries
CREATE INDEX IF NOT EXISTS idx_leaderboard_total_score ON public.leaderboard (total_score DESC);

-- Trigger function to populate user_id from JWT claims if not explicitly provided
CREATE OR REPLACE FUNCTION public.set_user_id_from_jwt()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
  IF NEW.user_id IS NULL THEN
    BEGIN
      NEW.user_id := (current_setting('request.jwt.claims.sub', true))::uuid;
    EXCEPTION WHEN others THEN
      -- in case request.jwt.claims.sub is not set, leave user_id NULL
      NEW.user_id := NULL;
    END;
  END IF;
  NEW.updated_at := now();
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_set_user_id
BEFORE INSERT OR UPDATE ON public.leaderboard
FOR EACH ROW EXECUTE FUNCTION public.set_user_id_from_jwt();
