-- Row Level Security for leaderboard table

-- Enable RLS
ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;

-- Policy: allow anyone to SELECT (public read of leaderboard)
CREATE POLICY "allow_select_leaderboard" ON public.leaderboard
  FOR SELECT USING (true);

-- Policy: allow authenticated users to INSERT only; user_id will be set by trigger from JWT
CREATE POLICY "allow_insert_own" ON public.leaderboard
  FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- Policy: allow authenticated users to UPDATE only their own row
CREATE POLICY "allow_update_own" ON public.leaderboard
  FOR UPDATE USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());

-- Policy: allow authenticated users to DELETE only their own row
CREATE POLICY "allow_delete_own" ON public.leaderboard
  FOR DELETE USING (user_id = auth.uid());

-- Note: We rely on the trigger to set user_id from JWT; the client only needs to send user_name and total_score.
