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

Scores List Endpoint (Edge Function)

This repo includes a ready-to-deploy function at `supabase/functions/scores-list/index.ts`.
It returns exactly what the Android app expects:

```json
[
	{
		"userName": "sample_user",
		"totalScore": 99,
		"lastUpdatedMillis": 1778012345000
	}
]
```

Quick setup (copy/paste)

1. Login and link your project:

```bash
supabase login
supabase link --project-ref <your-project-ref>
```

2. Deploy the function:

```bash
supabase functions deploy scores-list
```

3. (Optional but recommended) Set a bearer key used by the app:

```bash
supabase secrets set SCORES_API_KEY=<your-strong-random-token>
```

4. Test it:

Without key:

```bash
curl "https://<your-project-ref>.functions.supabase.co/scores-list?limit=50"
```

With key:

```bash
curl -H "Authorization: Bearer <your-strong-random-token>" "https://<your-project-ref>.functions.supabase.co/scores-list?limit=50"
```

Android `.env.local` values

Set these in project root `.env.local`:

```env
SCORES_LIST_ENDPOINT=https://<your-project-ref>.functions.supabase.co/scores-list?limit=100
SCORES_API_KEY=<your-strong-random-token>
```

Then rebuild app so `BuildConfig` is regenerated.
