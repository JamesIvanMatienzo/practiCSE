package com.jigen.practicse.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jigen.practicse.ui.screens.dashboard.DashboardScreen
import com.jigen.practicse.ui.screens.exam.ExamScreen
import com.jigen.practicse.ui.screens.onboarding.OnboardingScreen
import com.jigen.practicse.ui.screens.ranking.RankingScreen
import com.jigen.practicse.ui.screens.result.ResultScreen
import com.jigen.practicse.ui.screens.profile.ProfileScreen
import com.jigen.practicse.ui.screens.settings.SettingsScreen
import com.jigen.practicse.ui.screens.about.AboutScreen

@Composable
fun NavGraph(
	navController: NavHostController,
	context: Context,
	startDestination: String = Screen.Dashboard.route
) {
	NavHost(
		navController = navController,
		startDestination = startDestination
	) {
		composable(Screen.Onboarding.route) {
			OnboardingScreen(
				context = context,
				onTrackSelected = {
					navController.navigate(Screen.Dashboard.route) {
						popUpTo(Screen.Onboarding.route) { inclusive = true }
					}
				}
			)
		}

		composable(Screen.Dashboard.route) {
			DashboardScreen(
				context = context,
				onStartNewExam = {
					navController.navigate(Screen.Exam.createRoute("new"))
				},
				onContinueSession = {
					navController.navigate(Screen.Exam.createRoute("resume"))
				},
				onRanking = {
					navController.navigate(Screen.Ranking.route)
				},
				onStudyLibrary = {
					// Future: implement study library navigation
				}
			)
		}

		composable(
			Screen.Exam.route,
			arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
		) { backStackEntry ->
			val sessionId = backStackEntry.arguments?.getString("sessionId") ?: "new"
			ExamScreen(context = context, sessionId = sessionId)
		}

		composable(
			Screen.Result.route,
			arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
		) { backStackEntry ->
			val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
			ResultScreen(
				sessionId = sessionId,
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
				}
			)
		}

		composable(Screen.Settings.route) {
			SettingsScreen(
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
	}
}
