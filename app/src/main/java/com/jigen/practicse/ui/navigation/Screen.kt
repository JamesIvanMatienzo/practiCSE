package com.jigen.practicse.ui.navigation

sealed class Screen(val route: String) {
	object Splash : Screen("splash")
	object Login : Screen("login")
	object SignUp : Screen("signup")
	object Onboarding : Screen("onboarding")
	object Dashboard : Screen("dashboard")
	object Exam : Screen("exam/{sessionId}") {
		fun createRoute(sessionId: String) = "exam/$sessionId"
	}
	object Result : Screen("result/{sessionId}") {
		fun createRoute(sessionId: String) = "result/$sessionId"
	}
	object DeepDive : Screen("deepdive/{questionId}") {
		fun createRoute(questionId: String) = "deepdive/$questionId"
	}
	object Ranking : Screen("ranking")
		object StudyLibrary : Screen("study_library")
	object Profile : Screen("profile")
	object Settings : Screen("settings")
	object About : Screen("about")
}
