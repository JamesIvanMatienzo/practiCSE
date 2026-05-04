Project Overview: practiCSE
Tagline: A distraction-free, offline-first Civil Service Exam Reviewer.
1. The Problem & Mission
Civil Service Exam (CSE) takers in the Philippines often study in environments with inconsistent internet (commutes, public spaces) and are easily distracted by social media or ad-heavy apps. practiCSE is designed to solve this by prioritizing "Momentum" and "Accuracy" in a 100% focused, offline-ready environment.
2. Core User Experience (UX) Pillars
Offline-First (Hybrid-Lite): The app must function entirely without internet once the question bank is downloaded. Data syncing happens silently in the background when a connection is available.
Momentum-Driven Study: Transitions between questions must be automatic and seamless. No "Next" buttons; just select and glide.
Eye-Care Design: Material 3 compliant off-white (#F8F9FA) and professional blue (#1A73E8) palette to support "late-night grinding" without strain.
AI Tutor-on-Demand: Generative AI logic is only used for deep dives into incorrect answers, ensuring the primary study flow remains rule-based and manually verified.
3. Technical Architecture
Platform: Android (Kotlin & Jetpack Compose).
Data Layer: Room SQL for local persistence of questions, user progress, and cached AI explanations.
Logic Layer: ViewModel with StateFlow for reactive UI updates.
Sync Layer: WorkManager for background data transmission (error reports and leaderboard updates).
AI Layer: Gemini API integration for step-by-step logic deconstruction.
4. Key Features to Implement
Dual Tracks: Professional vs. Sub-Professional syllabus filtering.
Smart Dashboard: Visual performance tracking by subject (Math, Logic, Constitution, etc.).
Advanced Exam Interface: HorizontalPager with pinned headers for long reading passages.
Resumption Logic: A "Resume" button that pulls the exact state and question index from the local database.
Error Flagging: A built-in feedback loop for users to report inaccurate question keys.

Instruction for the Agent: "Read this overview carefully. Every piece of code you generate from this point forward must align with these architectural goals and visual constraints. Do not suggest features that require constant connectivity or interrupt the user's flow. Confirm when you are ready for the first technical prompt." 
