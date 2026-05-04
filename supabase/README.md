Supabase leaderboard setup

Files in this folder:
- `leaderboard_schema.sql` - table + trigger to set user_id from JWT
- `rls_policies.sql` - RLS policies to allow public select and restrict write/update/delete to authenticated users on their own row

Deployment notes:
- Run the SQL files in your Supabase SQL editor (or psql) against the project's database.
- Ensure you enable Row Level Security and add the policies in `rls_policies.sql`.
- The trigger relies on `request.jwt.claims.sub` being set (Supabase PostgREST sets this from the Authorization JWT).

Client integration:
- The client must authenticate users via Supabase Auth and include the user JWT in Authorization header for upsert operations.
- The client will send only `user_name` and `total_score`; the trigger will set `user_id` from the JWT.
- The server will enforce that only the user can update their row via RLS policies.

Security:
- Do not send PII. `user_name` should be a display name or pseudonym.
- Store Supabase keys in CI or device secure storage. Use authenticated user's JWT (not service role) for updates.
