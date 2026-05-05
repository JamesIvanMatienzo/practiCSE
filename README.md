# practiCSE

practiCSE is an offline-first Android reviewer for the Philippine Civil Service Exam. It is built with Kotlin, Jetpack Compose, Room, and WorkManager, with optional cloud features for leaderboard sync and AI-generated deep-dive explanations.

## What it does

- Offline exam practice with locally stored question banks.
- Track-based study flow for Professional and Sub-Professional paths.
- Dashboard progress tracking by subject and recent performance.
- Resume support for active exam sessions.
- Deep-dive explanations powered by Groq when a valid API key is available.
- Background leaderboard syncing through Supabase.
- Local profile storage for user details and study preferences.

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- WorkManager
- Firebase Remote Config / Crashlytics
- Supabase
- Groq API

## Project Structure

- `app/src/main/java/com/jigen/practicse/` - Android source code
- `app/src/main/assets/question_bank/` - Seed question JSON
- `supabase/` - Supabase schema and policies
- `PractiCSE_Datasource - *.csv` - Source question references

## Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

## Setup

1. Open the project in Android Studio.
2. Make sure `local.properties` points to your Android SDK.
3. Configure `.env.local` in the project root.
4. If you use Supabase, configure `supabase/supabase.properties`.
5. Build and run the app on an emulator or device.

## Environment Variables

Create or edit `.env.local` in the project root with these values:

```properties
GROQ_API_KEY=your_groq_api_key
GROQ_BASE_URL=https://api.groq.com/openai/v1
GROQ_MODEL=openai/gpt-oss-120b
SCORES_LIST_ENDPOINT=your_supabase_or_api_endpoint
SCORES_API_KEY=your_scores_api_key
```

Notes:

- `GROQ_API_KEY` is required for AI explanations in the deep-dive screen.
- If `GROQ_API_KEY` is missing, the app still works, but it shows a fallback explanation message.
- The app reads the first non-empty value for each key in `.env.local`.

## Build and Run

```bash
./gradlew installDebug
```

Or build only:

```bash
./gradlew clean build -x test
```

## Main Screens

- Login
- Sign Up
- Onboarding
- Dashboard
- Exam
- Result
- Profile
- Ranking
- Study Library
- Deep Dive

## Data Flow

- Questions are seeded locally from the bundled JSON question bank.
- Exam progress, sessions, and profile details are stored in Room and shared preferences.
- Leaderboard data can be synced to Supabase when configured.
- AI explanations are requested only when the user opens a deep dive question.

## Notes

- The app is designed to stay usable without constant internet access.
- If you update the question schema or Room entities, make sure the database version is updated consistently.
