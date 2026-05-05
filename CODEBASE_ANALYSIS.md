# PractiCSE Android App - Codebase Analysis

## Overview
PractiCSE is a Civil Service Exam preparation Android app built with Kotlin/Jetpack Compose. It features exam sessions, Grok AI integration for explanations, study library, and leaderboard rankings.

---

## 1. EXAM QUESTION DISPLAY MECHANISM

### Location: [app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt)

### Answer Evaluation System
**How answers are evaluated:**
- Answers are evaluated immediately upon selection in the `selectAnswer()` function
- Comparison is **case-insensitive**: `selectedText.equals(question.correctAnswer, ignoreCase = true)`
- Score increments only if the answer is correct
- Progress is saved to database via `progressDao.upsert(UserProgressEntity)`

**File:** [ExamViewModelNew.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamViewModelNew.kt) - Lines 268-298

```kotlin
fun selectAnswer(selectedText: String) {
    val isCorrect = selectedText.equals(question.correctAnswer, ignoreCase = true)
    val newScore = if (isCorrect) currentState.currentScore + 1 else currentState.currentScore
    progressDao.upsert(UserProgressEntity(
        questionId = question.id,
        selectedIndex = question.shuffledOptions.indexOf(selectedText).coerceAtLeast(-1),
        isCorrect = isCorrect,
        answeredAtMillis = System.currentTimeMillis(),
        track = "Professional",
        category = question.category
    ))
}
```

### Color Application Logic for Answer Cards

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 400-450

**Answer Card Background Colors:**
```
- Correct answer (evaluated): #E7F5EA (light green)
- Selected but incorrect (evaluated): #FDECEC (light red)
- Selected (before evaluation): #EAF2FF (light blue)
- Not selected: #F1F3F4 (light gray)
```

**Answer Card Border Colors:**
```
- Correct answer (evaluated): #188038 (SuccessGreen)
- Selected but incorrect (evaluated): #D93025 (ErrorRed)
- Selected (before evaluation): #1A73E8 (PrimaryBlue)
- Not selected: Transparent
```

**Feedback Text:**
- Correct: "Correct answer. Great job." (Green text)
- Incorrect: "That answer is incorrect. Correct answer: {answer}" (Red text)

---

## 2. GROK AI INTEGRATION

### Location: [app/src/main/java/com/jigen/practicse/ui/screens/deepdive/DeepDiveViewModel.kt](app/src/main/java/com/jigen/practicse/ui/screens/deepdive/DeepDiveViewModel.kt)

### Configuration
- **API Key:** `BuildConfig.GROK_API_KEY` (from gradle build config)
- **Base URL:** `BuildConfig.GROK_BASE_URL` (default: `https://api.x.ai/v1`)
- **Model:** `BuildConfig.GROK_MODEL` (default: `grok-3-mini`)
- **Source:** [build.gradle.kts](app/build.gradle.kts) - Lines 93-106

### Trigger Point
**"Ask AI" Button:** Located in the QuestionPage composable
- File: [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Line ~461
- Label: "Ask AI for Deep Explanation"
- Color: PrimaryBlue (#1A73E8)
- Handler: `onDeepDive(question.id.toString())`

### Grok API Call Implementation

**File:** [DeepDiveViewModel.kt](app/src/main/java/com/jigen/practicse/ui/screens/deepdive/DeepDiveViewModel.kt) - Lines 59-133

```kotlin
private suspend fun generateExplanation(question: QuestionEntity): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GROK_API_KEY.trim()
    val baseUrl = BuildConfig.GROK_BASE_URL.trim().ifBlank { "https://api.x.ai/v1" }
    val model = BuildConfig.GROK_MODEL.trim().ifBlank { "grok-3-mini" }
    
    // Request body construction
    val requestBody = JSONObject().apply {
        put("model", model)
        put("messages", listOf(
            mapOf("role" to "system", "content" to "You are a helpful exam tutor."),
            mapOf("role" to "user", "content" to prompt)
        ))
        put("temperature", 0.2)
    }
    
    // HTTP Connection setup
    val connection = (URL("${baseUrl.trimEnd('/')}/chat/completions").openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 15_000
        readTimeout = 30_000
        doOutput = true
        setRequestProperty("Authorization", "Bearer $apiKey")
        setRequestProperty("Content-Type", "application/json")
    }
}
```

### JSON Response Parsing

**File:** [DeepDiveViewModel.kt](app/src/main/java/com/jigen/practicse/ui/screens/deepdive/DeepDiveViewModel.kt) - Lines 110-135

```kotlin
val root = JSONObject(responseText)
val choices = root.optJSONArray("choices")
val message = choices?.optJSONObject(0)?.optJSONObject("message")
val content = message?.optString("content").orEmpty().trim()

if (content.isNotBlank()) {
    content  // Return the explanation
} else {
    "Grok returned an empty explanation. Correct answer: ${question.correctAnswer}"
}
```

### Error Handling for API Calls

1. **Missing API Key:**
   ```
   "Add GROK_API_KEY in .env.local to generate an AI explanation for this question.\n\nCorrect answer: {answer}"
   ```

2. **Network Errors:** Silently caught and return fallback message
   - No explanation generated, falls back to: `"No explanation was returned from Grok. Correct answer: {answer}"`

3. **Empty Response:**
   - Fallback: `"Grok returned an empty explanation. Correct answer: {answer}"`

4. **HTTP Error Responses (non-200):**
   - Reads error stream and logs, but still returns fallback message

### Known Issues
- **JSON Parsing:** Uses `optJSONArray()`, `optJSONObject()`, `optString()` which silently return null/default values (not throwing exceptions)
- No explicit validation of response format before accessing nested properties
- No timeout handling for long-running API calls (30-second read timeout only)

---

## 3. SCROLL/PAGER BEHAVIOR

### HorizontalPager Configuration

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 270-275

```kotlin
HorizontalPager(
    state = pagerState,
    modifier = Modifier.weight(1f),
    contentPadding = PaddingValues(horizontal = 16.dp),
    pageSpacing = 16.dp,
    userScrollEnabled = questions.isNotEmpty()
) { page ->
    // QuestionPage composable
}
```

### Pager Configuration Details
| Property | Value | Purpose |
|----------|-------|---------|
| **state** | `rememberPagerState(initialPage = safeInitialPage, pageCount = { questions.size })` | Maintains current page and total pages |
| **contentPadding** | 16dp horizontal | Padding around visible page |
| **pageSpacing** | 16dp | Gap between pages |
| **userScrollEnabled** | `questions.isNotEmpty()` | Disables swipe if no questions |

### Page Synchronization

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 103-113

```kotlin
// Sync when exam state changes
LaunchedEffect(state.currentIndex) {
    if (state.questions.isNotEmpty() && pagerState.currentPage != state.currentIndex) {
        pagerState.animateScrollToPage(state.currentIndex)
    }
}

// Sync when user swipes
LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
        viewModel.goToQuestion(page)
    }
}
```

### Question Navigator Bar
- Located below pager, horizontal lazy row with question numbers
- Colors: 
  - Current: PrimaryBlue (#1A73E8)
  - Flagged: ErrorRed (#D93025)
  - Answered: SuccessGreen (#188038)
  - Unanswered: Gray (#E8EAED)

---

## 4. BUTTON IMPLEMENTATIONS

### Exam Screen Buttons

#### 4.1 Flag Button (FloatingActionButton)
**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 138-146

```kotlin
FloatingActionButton(
    onClick = { viewModel.toggleFlagCurrentQuestion() },
    containerColor = if (state.currentQuestion?.id in state.flaggedQuestionIds) ErrorRed else PrimaryBlue,
    contentColor = Color.White,
    shape = RoundedCornerShape(16.dp)
) {
    Text(text = if (state.currentQuestion?.id in state.flaggedQuestionIds) "Unflag" else "Flag")
}
```
- **Toggle Logic:** Switches between flagged/unflagged state
- **Handler:** `ExamViewModelNew.toggleFlagCurrentQuestion()` - Lines 307-318

#### 4.2 Previous/Next Navigation Buttons
**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 324-336

```kotlin
Button(
    onClick = onPrevious,
    modifier = Modifier.weight(1f),
    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9AA0A6)),
    enabled = currentQuestionIndex > 0
) {
    Text("Back")
}

Button(
    onClick = onNext,
    modifier = Modifier.weight(1f),
    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
) {
    Text(if (currentQuestionIndex == questions.lastIndex) "Finish" else "Next")
}
```
- **Previous Handler:** `goToQuestion(currentIndex - 1)`
- **Next Handler:** `goToQuestion(currentIndex + 1)` or `completeExam()` if last question
- **Disabled State:** Back button disabled on first question

#### 4.3 Answer Card Buttons (RadioButton + Card)
**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines ~423-438

```kotlin
Card(
    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = background),
    border = BorderStroke(1.dp, borderColor),
    onClick = { onAnswerSelected(option) }
) {
    RowWithRadio(selected = isSelected, text = option, textColor = TextColor)
}
```
- **Handler:** `ExamViewModelNew.selectAnswer(optionText)`
- **No Disabled State:** Can select new answer even after already answering

#### 4.4 Ask AI & Report Buttons
**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 461-474

```kotlin
Button(
    onClick = { onDeepDive(question.id.toString()) },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
    shape = RoundedCornerShape(18.dp)
) {
    Text(text = "Ask AI for Deep Explanation")
}

Button(
    onClick = onReportError,
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F0FE)),
    shape = RoundedCornerShape(18.dp)
) {
    Text(text = "Report a Problem in this Question", color = PrimaryBlue)
}
```

### Dashboard Screen Buttons

**File:** [DashboardScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/dashboard/DashboardScreen.kt)

#### 4.5 Profile Header Button
- Clickable Row with Icon + "Profile" label
- Handler: `onProfileClick()`

#### 4.6 Action Cards (Start Exam / Continue Session)
- **Start New Exam:** Triggers `onStartNewExam()` - Lines ~186-195
- **Continue Session:** Triggers `onContinueSession()` - Enabled only if `state.hasSessionToResume`

#### 4.7 Ranking & Study Library Buttons
- **Ranking Button:** OutlinedActionButton with Star icon - Handler: `onRanking()`
- **Study Library Button:** OutlinedActionButton with Info icon - Handler: `onStudyLibrary()`

### Ranking Screen Buttons

**File:** [RankingScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/ranking/RankingScreen.kt)

#### 4.8 Back Navigation Button
```kotlin
IconButton(onClick = onBack) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
}
```

### Study Library Screen Buttons

**File:** [StudyLibraryScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/study_library/StudyLibraryScreen.kt)

#### 4.9 Category Selection Cards
```kotlin
Card(
    modifier = modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    // Category info
}
```
- **Handler:** `onStartPractice(category.categoryKey)`
- **2-Column Layout:** Categories arranged in pairs with equal weight

#### 4.10 Back Navigation Button
```kotlin
IconButton(onClick = onBack) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
}
```

### Summary of All Button Clicks

| Screen | Button | Handler | State |
|--------|--------|---------|-------|
| Exam | Flag (FAB) | `toggleFlagCurrentQuestion()` | Active |
| Exam | Back | `previousQuestion()` | Disabled if first |
| Exam | Next/Finish | `nextQuestion()`/`completeExam()` | Active |
| Exam | Answer Option | `selectAnswer()` | Active |
| Exam | Ask AI | Navigation to DeepDive | Active |
| Exam | Report | `reportCurrentQuestion()` | Active |
| Dashboard | Profile | `onProfileClick()` | Active |
| Dashboard | Start Exam | `onStartNewExam()` | Active |
| Dashboard | Continue | `onContinueSession()` | Conditional |
| Dashboard | Ranking | `onRanking()` | Active |
| Dashboard | Study Library | `onStudyLibrary()` | Active |
| Ranking | Back | `onBack()` | Active |
| Study Library | Category | `onStartPractice()` | Active |
| Study Library | Back | `onBack()` | Active |

---

## 5. REPORT A PROBLEM FLOW

### Location: [ExamViewModelNew.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamViewModelNew.kt) - Lines 336-349

### Implementation

```kotlin
fun reportCurrentQuestion() {
    val currentState = _uiState.value as? ExamUiState.Success ?: return
    val currentQuestion = currentState.currentQuestion ?: return

    viewModelScope.launch {
        try {
            errorReportDao.insert(
                ErrorReportEntity(
                    questionId = currentQuestion.id,
                    reportedAtMillis = System.currentTimeMillis()
                )
            )
            _effects.tryEmit(ExamEffect.QuestionReported)
        } catch (e: Exception) {
            // Log but don't interrupt exam
        }
    }
}
```

### Database Entity

**File:** [ErrorReportEntity.kt](app/src/main/java/com/jigen/practicse/data/local/entity/ErrorReportEntity.kt)

```kotlin
@Entity(tableName = "error_reports")
data class ErrorReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Int,
    val reportedAtMillis: Long,
    val isFlagged: Boolean = true
)
```

### UI Feedback

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 120-124

```kotlin
LaunchedEffect(Unit) {
    viewModel.effects.collectLatest { effect ->
        when (effect) {
            ExamEffect.QuestionReported -> 
                reportStatusMessage = "Thanks. We logged this question for review."
        }
    }
}
```

### UI Display

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt) - Lines 251-261

```kotlin
if (!reportStatusMessage.isNullOrBlank()) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7EE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = reportStatusMessage,
            modifier = Modifier.padding(12.dp),
            color = SuccessGreen,
            fontSize = 12.sp
        )
    }
}
```

### Flow Summary
1. User clicks "Report a Problem in this Question" button
2. `reportCurrentQuestion()` is triggered
3. `ErrorReportEntity` is inserted with current question ID and timestamp
4. Effect is emitted: `ExamEffect.QuestionReported`
5. Message appears in green card: "Thanks. We logged this question for review."
6. Report is stored locally in Room database
7. No interruption to exam flow

---

## 6. MAIN SCREENS - UI STRUCTURE

### 6.1 ExamScreen

**File:** [ExamScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/exam/ExamScreen.kt)

#### States & Loading
- **Loading:** Circular spinner with "Loading..." message
- **Error:** Error message display with padding
- **Success:** Full exam interface
- **Completed:** Score summary (total, percentage, pass/fail)

#### Components
1. **Top Bar:** Title "Civil Service Exam" + Timer (MM:SS format)
2. **Progress Bar:** Linear indicator showing progress through exam
3. **Question Status:** "Question X of Y • Answered: Z" text
4. **HorizontalPager:** Main question display with swiping
5. **Question Number Navigator:** Horizontal LazyRow with colored number buttons
6. **Navigation Buttons:** Back/Next row at bottom
7. **Flag FAB:** Floating action button (top-right position)
8. **Answer Status:** Green/red feedback message after answering

#### Color Scheme
- Background: #F8F9FA (light gray)
- Primary: #1A73E8 (blue)
- Success: #188038 (green)
- Error: #D93025 (red)
- Text: #202124 (dark gray)
- Muted: #6C757D (medium gray)

---

### 6.2 DashboardScreen

**File:** [DashboardScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/dashboard/DashboardScreen.kt)

#### States & Loading
- **Loading:** Circular spinner with "Loading your progress..." message
- **Error:** Error message display
- **Success:** Full dashboard content

#### Components
1. **Header:** "practiCSE" title + Profile clickable button
2. **Action Cards:** 
   - Start New Exam
   - Continue Last Session (conditional)
3. **Status Pill:** "Active Path: Professional Track" with star icon
4. **Section Title:** "PROGRESS TRACKER — SUBJECT PERFORMANCE" (uppercase, spaced)
5. **Progress Cards:** 
   - Shows category scores (Numerical, Verbal, General)
   - Progress bar per category
   - Limited to 3 categories display
   - 3rd card shows "Recent Activity" toggle
6. **Bottom Buttons Row:** Ranking + Study Library buttons (equal width)
7. **Gradient Info Box:** Promotional/informational content
8. **Scrollable:** Full screen vertical scroll enabled

#### Color Scheme
- Background: #F8F9FA (light gray)
- Surface: #FFFFFF (white)
- Primary: #1976D2 (blue, slightly different shade)
- Primary Soft: #EAF2FF (light blue background)
- Text: #202124 (dark)
- Muted: #6C757D (gray)
- Border: #E6E8EC (light gray)

---

### 6.3 RankingScreen

**File:** [RankingScreen.kt](app/src/main/java/com/jigen/practicse/ui/screens/ranking/RankingScreen.kt)

#### States & Loading
- **Loading:** Centered circular spinner
- **Error:** Not explicitly shown in provided code
- **Success:** Ranking content

#### Components
1. **Top App Bar:** 
   - Title: "Ranking" (bold)
   - Back navigation icon (auto-mirrored left arrow)
   - White background
2. **Offline Mode Banner:** 
   - Light blue background (#EAF1FE)
   - Message: "Offline mode: showing sample rankings"
   - Only shown if `isPlaceholder = true`
3. **User Rank Card:** 
   - Shows "Your Rank: #X" (large text)
   - User name + score below
   - White card background
4. **Top 3 Section:** 
   - Podium-style display (if available)
   - Separate smaller cards for ranks 1-3
   - Shows rank number and badge styling
5. **Others List:** 
   - LazyColumn for ranks 4+
   - Card per entry: rank number, name, score
6. **Fallback Placeholder:**
   - 7 sample users if no real data available
   - Current user always included at end

#### Ranking Data Structure
- Retrieved from: `RankingRepository.fetchGlobalTop(100)`
- Fallback cache from: `RankingRepository.getCachedTop(100)`
- Placeholder entries: Sample civil service exam prep users

#### Color Scheme
- Background: #F8F9FA
- Card: #FFFFFF
- Primary: #1A73E8
- Text: #202124
- Muted: #6C757D
- Offline banner: #EAF1FE

---

### 6.4 StudyLibraryScreen

**File:** [StudyLibraryScreen.kt](app/src/main/java/com/jigen/practicse/ui\screens\study_library\StudyLibraryScreen.kt)

#### States & Loading
- **Loading:** Centered circular spinner
- **Error:** Error message in center
- **Success:** Category grid layout

#### Components
1. **Top App Bar:**
   - Title: "Study Library" (bold)
   - Back navigation icon
   - White background
2. **Instructions:** "Choose a category to practice" (14sp, gray)
3. **Category Grid:** 2-column layout
   - Cards chunked into rows of 2
   - Equal width columns with 12dp spacing
4. **Category Card Structure:**
   - Circular badge with category initials (#EAF1FE background)
   - Category title (15sp, semibold)
   - Question count (12sp, gray): "X questions"
   - White card with 2dp elevation
   - Rounded corners (16dp)
5. **Scrollable:** Vertical scroll enabled via LazyColumn

#### Category Data
- Retrieved from: `StudyLibraryViewModel`
- Includes: title, categoryKey (identifier), questionCount
- Examples: Numerical Ability, Verbal Ability, General Information

#### Color Scheme
- Background: #F8F9FA
- Card: #FFFFFF
- Primary: #1A73E8
- Accent: #EAF1FE (light blue badges)
- Text: #202124
- Muted: #6C757D

---

## 7. DEEP DIVE SCREEN (Ask AI)

**File:** [DeepDiveScreen.kt](app/src/main/java/com/jigen/practicse/ui\screens\deepdive\DeepDiveScreen.kt)

#### States & Loading
- **Loading:** Centered circular spinner
- **Error:** Error message + Back button
- **Success:** Full explanation content

#### Components
1. **Header:** "Deep Dive with Grok" title + category
2. **Question Card:** 
   - Shows question text
   - Reference text (if available)
   - White card with rounded corners
3. **Grok Explanation Card:**
   - AI-generated explanation
   - Multiple lines (lineHeight: 22sp)
   - White card format
4. **Back Button:** Fills width, PrimaryBlue color

#### Data Loading
- Fetches question from database by ID
- Generates explanation via Grok API (see section 2)
- Handles fallback messages if API fails

---

## 8. COLOR PALETTE REFERENCE

**File:** [Color.kt](app/src/main/java/com/jigen/practicse/ui/theme/Color.kt)

```kotlin
// Primary Colors
PrimaryBlue = #1976D2 (Dashboard) / #1A73E8 (Exam) - depending on screen
PrimaryBlueLight = #64B5F6
PrimaryBlueDark = #1565C0

// Status Colors
SuccessGreen = #188038
ErrorRed = #D93025
WarningOrange = #F57C00

// Backgrounds
SurfaceColor = #F8F9FA (light gray)
BackgroundColor = #FFFFFF (white)

// Text Colors
TextPrimary = #202124
TextSecondary = #6C757D
TextTertiary = #9CA3AF

// Gray Scale
Gray50 = #F9FAFB through Gray900 = #111827
```

---

## 9. STATE MANAGEMENT ARCHITECTURE

### Exam Flow
```
ExamScreen (UI) 
    ↓
ExamViewModelNew (State Holder)
    ↓
ExamUiState (Sealed Class)
    ├─ Loading
    ├─ Success (with questions, timer, scores)
    ├─ Completed
    └─ Error
```

### Effects Bus
- `ExamEffect.NavigateToPage`
- `ExamEffect.ExamCompleted`
- `ExamEffect.TimeExpired`
- `ExamEffect.QuestionReported`

### Dashboard Flow
```
DashboardScreen
    ↓
DashboardViewModel
    ↓
DashboardUiState
    ├─ Loading
    ├─ Success (with category scores, session resume flag)
    └─ Error
```

### Ranking Flow
```
RankingScreen
    ↓
RankingViewModel
    ↓
RankingUiState
    └─ Success (with top list, user rank, placeholder flag)
```

---

## 10. DATA PERSISTENCE

### Database: Room (Local SQLite)

**File:** [PractiCSEDatabase.kt](app/src/main/java/com/jigen/practicse/data/local/PractiCSEDatabase.kt)

#### Entities
1. **QuestionEntity:** All exam questions with shuffled options
2. **SessionEntity:** Current exam session state
3. **UserProgressEntity:** Answer history and scores
4. **ErrorReportEntity:** Flagged/reported questions
5. **LeaderboardEntryEntity:** Cached ranking data

#### DAOs
- `questionDao()` - Question queries
- `sessionDao()` - Session management
- `progressDao()` - Progress tracking
- `errorReportDao()` - Error reports
- `leaderboardDao()` - Leaderboard caching

---

## 11. NETWORK INTEGRATION

### Grok API (Deep Dive Explanations)
- Direct HTTP connection via `HttpURLConnection`
- JSON request/response parsing with `org.json`
- Error handling: Silent fallback to default messages

### Supabase Integration
- Leaderboard upsert and fetch
- File: [SupabaseLeaderboardRepository.kt](app/src/main/java/com/jigen/practicse/repository/SupabaseLeaderboardRepository.kt)
- Maps JSON responses to `LeaderboardEntryEntity`

### Scores API
- Fetch global top rankings
- File: [RankingRepository.kt](app/src/main/java/com/jigen/practicse/repository/RankingRepository.kt)
- Endpoint: `BuildConfig.SCORES_LIST_ENDPOINT`
- API Key: `BuildConfig.SCORES_API_KEY`
- **Fallback Strategy:** On network failure, uses cached Room database

### Connectivity Check
- `DashboardViewModel` checks network connectivity
- Method: `isNetworkConnected()` using `ConnectivityManager`
- Property: `isOffline` in UI state

---

## 12. KEY FILES SUMMARY

| Path | Purpose |
|------|---------|
| `ExamScreen.kt` | Main exam UI with pager and buttons |
| `ExamViewModelNew.kt` | Exam logic, scoring, timer, reporting |
| `ExamUiState.kt` | Exam state definitions |
| `DeepDiveScreen.kt` | AI explanation UI |
| `DeepDiveViewModel.kt` | Grok API integration, JSON parsing |
| `DashboardScreen.kt` | Home screen UI |
| `DashboardViewModel.kt` | Dashboard data loading, progress tracking |
| `RankingScreen.kt` | Leaderboard UI |
| `RankingViewModel.kt` | Ranking data fetching, fallback handling |
| `StudyLibraryScreen.kt` | Category selection UI |
| `Color.kt` | Centralized color palette |
| `PractiCSEDatabase.kt` | Room database setup |
| `ErrorReportEntity.kt` | Error report data model |
| `RankingRepository.kt` | API calls for rankings with retry logic |
| `SupabaseLeaderboardRepository.kt` | Supabase leaderboard operations |

---

## 13. KNOWN ISSUES & OBSERVATIONS

1. **Grok JSON Parsing:** Uses lenient parsing (optJSONObject, optString) - no explicit error on malformed JSON
2. **No Request Validation:** Doesn't validate request body before sending to Grok
3. **Silent Failures:** Network errors in API calls are silently caught with fallback messages
4. **Timer Precision:** Uses simple 1-second delay loops, may not be perfectly accurate
5. **No Retry Logic:** API calls don't implement retry-on-failure pattern
6. **Hard-coded Timeouts:** 15/30 second timeouts for Grok API, no configuration option
7. **Case-Insensitive Answers:** Answer matching uses `.ignoreCase = true`, but question data may have specific case requirements
8. **Two Different Blue Shades:** Dashboard uses #1976D2 while Exam uses #1A73E8

---

## 14. BUILD CONFIGURATION

**File:** [build.gradle.kts](app/build.gradle.kts)

### Environment Variables (Lines 93-106)
```gradle
val grokApiKey = resolveLocalEnvValue("GROK_API_KEY")
val grokBaseUrl = resolveLocalEnvValue("GROK_BASE_URL")
val grokModel = resolveLocalEnvValue("GROK_MODEL")

buildConfigField("String", "GROK_API_KEY", "\"$grokApiKey\"")
buildConfigField("String", "GROK_BASE_URL", "\"$grokBaseUrl\"")
buildConfigField("String", "GROK_MODEL", "\"$grokModel\"")
```

### Required .env.local entries
```
GROK_API_KEY=your_api_key_here
GROK_BASE_URL=https://api.x.ai/v1
GROK_MODEL=grok-3-mini
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
SCORES_LIST_ENDPOINT=your_scores_endpoint
SCORES_API_KEY=your_scores_api_key
```

