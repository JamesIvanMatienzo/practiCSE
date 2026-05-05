import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "GET") {
    return json({ error: "Method not allowed" }, 405);
  }

  const configuredApiKey = (Deno.env.get("SCORES_API_KEY") ?? "").trim();
  if (configuredApiKey.length > 0) {
    const authHeader = req.headers.get("Authorization") ?? "";
    const providedKey = authHeader.replace(/^Bearer\s+/i, "").trim();
    if (providedKey != configuredApiKey) {
      return json({ error: "Unauthorized" }, 401);
    }
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY");

  if (!supabaseUrl || !supabaseAnonKey) {
    return json({ error: "Missing Supabase env vars in Edge runtime" }, 500);
  }

  const limitRaw = new URL(req.url).searchParams.get("limit") ?? "100";
  const limit = Math.min(Math.max(Number.parseInt(limitRaw, 10) || 100, 1), 500);

  const supabase = createClient(supabaseUrl, supabaseAnonKey);

  const { data, error } = await supabase
    .from("leaderboard")
    .select("user_name,total_score,updated_at")
    .order("total_score", { ascending: false })
    .limit(limit);

  if (error) {
    return json({ error: error.message }, 500);
  }

  const payload = (data ?? []).map((row) => ({
    userName: row.user_name,
    totalScore: row.total_score,
    lastUpdatedMillis: Date.parse(row.updated_at),
  }));

  return json(payload, 200);
});

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}
