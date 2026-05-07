# practiCSE

practiCSE is an offline-first Android reviewer for the Philippine Civil Service Exam. The app is designed to help users study in short, focused sessions without depending on constant connectivity, while still supporting optional cloud features for leaderboard sync and AI-assisted deep dives.

The experience centers on momentum, clarity, and repeatable practice:

- start quickly from login or onboarding,
- choose a study track,
- continue an exam session exactly where you left off,
- review mistakes with deeper explanations when needed,
- and compare progress through a local or online ranking view.

## Highlights

- Offline-first study flow with bundled question banks and local persistence.
- Track-based learning for Professional and Sub-Professional exam paths.
- Splash, login, onboarding, and dashboard screens that use the practiCSE branding assets.
- Resume support for active exam sessions.
- Dashboard tracking for study progress and recent performance.
- Deep-dive explanations for incorrect answers when the Groq API is configured.
- Ranking screen with persisted offline/online mode and local sample entries when offline.
- Profile, settings, and study library screens for navigation and personalization.

## Screens

- Splash screen: brief branded entry point with the practiCSE logo.
- Login: entry screen for existing users.
- Sign Up: account creation flow.
- Onboarding: track selection and initial setup.
- Dashboard: overview of progress, study shortcuts, and exam actions.
- Exam: the main practice experience.
- Result: session summary after completing an exam.
- Ranking: leaderboard view with offline sample data and toggleable sync mode.
- Study Library: practice by subject or category.
- Deep Dive: explanation screen for detailed answer review.
- Profile: stored user information and account actions.
- Settings: app preferences and configuration.
- About: project and app information.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- Room for local persistence
- SharedPreferences-based preference storage
- WorkManager for background sync tasks
- Firebase Remote Config and Crashlytics
- Supabase for leaderboard-related backend support
- Groq API for explanation generation

## Architecture

The project follows a practical offline-first architecture:

- UI layer: Jetpack Compose screens and navigation.
- State layer: ViewModels and StateFlow for reactive screen state.
- Data layer: Room entities, repositories, and preference stores.
- Sync layer: background tasks for cloud updates when configured.
- AI layer: on-demand deep-dive requests only when the user opens an explanation view.

This keeps the normal study loop fast and usable even when the network is unavailable.

## Project Structure

- `app/src/main/java/com/jigen/practicse/` - Android source code.
- `app/src/main/res/` - launcher icons, logos, and app UI resources.
- `app/src/main/assets/question_bank/` - bundled seed question data.
- `app/src/main/assets/` - other static assets used at runtime.
- `supabase/` - schema, policies, and deployment notes for leaderboard support.
- `PractiCSE_Datasource - *.csv` - source reference files for question content.
- `questions.json` - question data used by the app and seed tooling.

## Requirements

- Android Studio Hedgehog or newer.
- JDK 17.
- Android SDK 35.
- A connected Android device or emulator for installation and testing.

## Setup

1. Open the project in Android Studio.
2. Confirm that `local.properties` points to your Android SDK installation.
3. Configure the root `.env.local` file if you want AI or leaderboard features.
4. If you are using Supabase, review `supabase/supabase.properties` and the SQL files in `supabase/`.
5. Sync Gradle, then build and run the app on a device or emulator.

## Environment Variables

Create or edit `.env.local` in the project root with values similar to the following:

```properties
GROQ_API_KEY=your_groq_api_key
GROQ_BASE_URL=https://api.groq.com/openai/v1
GROQ_MODEL=openai/gpt-oss-120b
SCORES_LIST_ENDPOINT=your_supabase_or_api_endpoint
SCORES_API_KEY=your_scores_api_key
```

Notes:

- `GROQ_API_KEY` enables AI-generated explanations in Deep Dive.
- If the key is missing, the app still runs and shows a fallback explanation.
- The app reads the first non-empty value for each key in `.env.local`.
- Leaderboard support can be left offline; the app will continue to work with local ranking data.

## Branding Assets

The UI uses the practiCSE logo and icon assets directly from the app resources:

- `practicse_logo.png` is used in the splash, login, and onboarding flows.
- `p_only.png` is used as the launcher icon foreground and dashboard branding mark.
- Splash uses a light off-white background to match the app’s eye-care visual style.

## Build and Run

To install the debug build on a connected device or emulator:

```bash
./gradlew installDebug
```

To compile the project without installing:

```bash
./gradlew assembleDebug
```

If you want a full clean build:

```bash
./gradlew clean assembleDebug
```

## Data Flow

- Question content is seeded locally from bundled JSON and reference files.
- User profile data, active track selection, and offline ranking preferences are stored locally.
- Exam progress and resume state are kept on-device so the user can continue later.
- Ranking data can operate in offline mode using sample entries or online mode when available.
- Deep dive explanations are requested only when the user opens a specific question.

## Offline Ranking Behavior

The ranking screen supports a persisted offline mode.

- Offline mode uses local sample leaderboard entries.
- Names are displayed in capitalized form for readability.
- Avatar thumbnails are shown for the sample users when available.
- The offline/online toggle is saved locally, so the screen opens in the last selected state.

## Contributing and Maintenance

- Keep Android resource filenames lowercase and underscore-separated.
- Update database versions and migration paths whenever Room entities change.
- Keep the README in sync with major UI or flow changes, especially navigation and data persistence behavior.
- Prefer offline-safe behavior first, then add cloud or AI features as optional enhancements.

## Notes

- The app is intentionally usable without a network connection.
- Cloud and AI features should enhance the experience, not block the core study flow.
- If you change the question schema or ranking model, verify any seed or persistence code that depends on it.
