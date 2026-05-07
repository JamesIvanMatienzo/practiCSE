package com.jigen.practicse.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.ExamConfigStore
import com.jigen.practicse.ui.screens.splash.SplashScreen
import com.jigen.practicse.ui.screens.login.LoginScreen
import com.jigen.practicse.ui.screens.login.SignUpScreen
import com.jigen.practicse.ui.screens.dashboard.DashboardScreen
import com.jigen.practicse.ui.screens.exam.ExamScreen
import com.jigen.practicse.ui.screens.onboarding.OnboardingScreen
import com.jigen.practicse.ui.screens.ranking.RankingScreen
import com.jigen.practicse.ui.screens.result.ResultScreen
import com.jigen.practicse.ui.screens.profile.ProfileScreen
import com.jigen.practicse.ui.screens.settings.SettingsScreen
import com.jigen.practicse.ui.screens.about.AboutScreen
import com.jigen.practicse.ui.screens.deepdive.DeepDiveScreen
import com.jigen.practicse.ui.screens.study_library.StudyLibraryScreen

@Composable
fun NavGraph(
	navController: NavHostController,
	context: Context,
	startDestination: String = Screen.Login.route
) {
	val configStore = ExamConfigStore(context)
	val appPreferencesStore = AppPreferencesStore(context)

	NavHost(
		navController = navController,
		startDestination = startDestination
	) {
		composable(Screen.Splash.route) {
			SplashScreen(
				onNavigateToLogin = {
					navController.navigate(Screen.Login.route) {
						popUpTo(Screen.Splash.route) { inclusive = true }
					}
				}
			)
		}

		composable(Screen.Login.route) {
			LoginScreen(
				onContinue = {
					navController.navigate(Screen.Onboarding.route) {
						popUpTo(Screen.Login.route) { inclusive = true }
					}
				},
				onSignUp = {
					navController.navigate(Screen.SignUp.route)
				}
			)
		}

		composable(Screen.SignUp.route) {
			SignUpScreen(
				context = context,
				onBack = {
					navController.popBackStack()
				},
				onSignUpComplete = {
					navController.navigate(Screen.Onboarding.route) {
						popUpTo(Screen.Login.route) { inclusive = true }
					}
				}
			)
		}

		composable(Screen.Onboarding.route) {
			OnboardingScreen(
				context = context,
				onTrackSelected = {
					appPreferencesStore.setActiveTrack(it)
					navController.navigate(Screen.Dashboard.route) {
						popUpTo(Screen.Onboarding.route) { inclusive = true }
					}
				}
			)
		}

		composable(Screen.Dashboard.route) {
			DashboardScreen(
				context = context,
				onProfileClick = {
					navController.navigate(Screen.Profile.route) {
						launchSingleTop = true
					}
				},
				onStartNewExam = { requested ->
					configStore.setAllExamCount(requested)
					navController.navigate(Screen.Exam.createRoute("new"))
				},
				onContinueSession = {
					navController.navigate(Screen.Exam.createRoute("resume"))
				},
				onRanking = {
					navController.navigate(Screen.Ranking.route) {
						launchSingleTop = true
					}
				},
				onStudyLibrary = {
					navController.navigate(Screen.StudyLibrary.route) {
						launchSingleTop = true
					}
				}
			)
		}

		composable(
			Screen.Exam.route,
			arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
		) { backStackEntry ->
			val sessionId = backStackEntry.arguments?.getString("sessionId") ?: "new"
			ExamScreen(
				context = context,
				sessionId = sessionId,
				onDeepDive = { questionId ->
					navController.navigate(Screen.DeepDive.createRoute(questionId))
				},
				onBack = {
					navController.navigate(Screen.Dashboard.route) {
						popUpTo(Screen.Dashboard.route) { inclusive = false }
					}
				},
				onResult = { score, totalQuestions ->
					navController.navigate(
						Screen.Result.createRoute(sessionId, score, totalQuestions)
					) {
						popUpTo(Screen.Dashboard.route) { inclusive = false }
					}
				}
			)
		}

		composable(
			Screen.Result.route,
			arguments = listOf(
				navArgument("sessionId") { type = NavType.StringType },
				navArgument("score") { type = NavType.IntType },
				navArgument("totalQuestions") { type = NavType.IntType }
			)
		) { backStackEntry ->
			val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
			val score = backStackEntry.arguments?.getInt("score") ?: 0
			val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
			ResultScreen(
				sessionId = sessionId,
				score = score,
				totalQuestions = totalQuestions,
				onBackToDashboard = {
					navController.navigate(Screen.Dashboard.route) {
						popUpTo(Screen.Dashboard.route) { inclusive = false }
					}
				}
			)
		}

		composable(Screen.Ranking.route) {
			RankingScreen(
				context = context,
				onBack = {
					navController.popBackStack()
				}
			)
		}

		composable(Screen.Profile.route) {
			ProfileScreen(
				onBack = {
					navController.popBackStack()
				},
				onLogout = {
					appPreferencesStore.clearProfile()
					navController.navigate(Screen.Login.route) {
						popUpTo(Screen.Login.route) { inclusive = true }
						launchSingleTop = true
					}
				}
			)
		}

		composable(Screen.Settings.route) {
			SettingsScreen(
				context = context,
				onBack = {
					navController.popBackStack()
				}
			)
		}

		composable(Screen.About.route) {
			AboutScreen(
				onBack = {
					navController.popBackStack()
				}
			)
		}

		composable(Screen.StudyLibrary.route) {
			StudyLibraryScreen(
				context = context,
				onBack = {
					navController.popBackStack()
				},
				onStartPractice = { categoryKey, requested ->
					when (categoryKey) {
						"numerical_ability" -> configStore.setNumericalCount(requested)
						"verbal_ability" -> configStore.setVerbalCount(requested)
						"general_information" -> configStore.setGeneralCount(requested)
					}
					navController.navigate(Screen.Exam.createRoute("new@$categoryKey"))
				}
			)
		}

		composable(
			Screen.DeepDive.route,
			arguments = listOf(navArgument("questionId") { type = NavType.StringType })
		) { backStackEntry ->
			val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
			DeepDiveScreen(
				context = context,
				questionId = questionId,
				onBack = { navController.popBackStack() }
			)
		}
	}
}