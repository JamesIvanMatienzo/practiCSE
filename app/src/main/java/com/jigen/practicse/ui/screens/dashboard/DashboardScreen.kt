package com.jigen.practicse.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context

// Color Palette
private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)
private val SuccessGreen = Color(0xFF2E7D32)
private val OfflineGray = Color(0xFF9CA3AF)

@Composable
fun DashboardScreen(
	context: Context,
	onStartNewExam: () -> Unit = {},
	onContinueSession: () -> Unit = {},
	onRanking: () -> Unit = {},
	onStudyLibrary: () -> Unit = {}
) {
	val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(context))
	val uiState by viewModel.uiState.collectAsState()

	Scaffold(
		containerColor = SurfaceColor,
		topBar = {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.background(SurfaceColor)
					.padding(16.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Exam Prep Hub",
					fontSize = 24.sp,
					fontWeight = FontWeight.Bold,
					color = TextColor
				)

				// Offline Mode Indicator
				when (val state = uiState) {
					is DashboardUiState.Success -> {
						if (state.isOffline) {
							AssistChip(
								onClick = {},
								label = {
									Text("Offline Mode", fontSize = 12.sp, color = TextColor)
								},
								modifier = Modifier.height(32.dp),
								colors = AssistChipDefaults.assistChipColors(
									containerColor = Color(0xFFE8EAED)
								)
							)
						}
					}
					else -> {}
				}
			}
		}
	) { paddingValues ->
		when (val state = uiState) {
			is DashboardUiState.Loading -> {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(paddingValues)
						.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					CircularProgressIndicator(color = PrimaryBlue)
					Spacer(modifier = Modifier.height(16.dp))
					Text("Loading your progress...", color = TextColor)
				}
			}

			is DashboardUiState.Success -> {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.verticalScroll(rememberScrollState())
						.padding(paddingValues)
						.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(16.dp)
				) {
					// Action Cards Section
					Row(
						modifier = Modifier
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						// Start New Exam Card
						Card(
							modifier = Modifier
								.weight(1f)
								.height(120.dp),
							colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
							elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
						) {
							Button(
								onClick = onStartNewExam,
								modifier = Modifier
									.fillMaxWidth()
									.padding(0.dp),
								colors = ButtonDefaults.buttonColors(
									containerColor = PrimaryBlue,
									contentColor = Color.White
								)
							) {
								Column(
									horizontalAlignment = Alignment.CenterHorizontally,
									verticalArrangement = Arrangement.Center,
									modifier = Modifier.fillMaxWidth()
								) {
									Text(
										"Start New",
										fontSize = 14.sp,
										fontWeight = FontWeight.Bold
									)
									Text("Exam", fontSize = 12.sp)
								}
							}
						}

						// Continue Last Session Card
						Card(
							modifier = Modifier
								.weight(1f)
								.height(120.dp),
							colors = CardDefaults.cardColors(
								containerColor = if (state.hasSessionToResume) Color(0xFF4285F4) else Color(0xFFCED4DA)
							),
							elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
						) {
							Button(
								onClick = if (state.hasSessionToResume) onContinueSession else {},
								modifier = Modifier
									.fillMaxWidth()
									.padding(0.dp),
								enabled = state.hasSessionToResume,
								colors = ButtonDefaults.buttonColors(
									containerColor = if (state.hasSessionToResume) Color(0xFF4285F4) else Color(0xFFCED4DA),
									contentColor = Color.White,
									disabledContentColor = Color(0xFF6C757D)
								)
							) {
								Column(
									horizontalAlignment = Alignment.CenterHorizontally,
									verticalArrangement = Arrangement.Center,
									modifier = Modifier.fillMaxWidth()
								) {
									Text(
										"Continue",
										fontSize = 14.sp,
										fontWeight = FontWeight.Bold
									)
									Text(
										"Session",
										fontSize = 12.sp
									)
								}
							}
						}
					}

					Spacer(modifier = Modifier.height(8.dp))

					// Secondary Navigation
					Row(
						modifier = Modifier
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						Button(
							onClick = onRanking,
							modifier = Modifier
								.weight(1f)
								.height(48.dp),
							colors = ButtonDefaults.buttonColors(
								containerColor = Color(0xFFF0F3F7),
								contentColor = PrimaryBlue
							)
						) {
							Text("Ranking", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
						}

						Button(
							onClick = onStudyLibrary,
							modifier = Modifier
								.weight(1f)
								.height(48.dp),
							colors = ButtonDefaults.buttonColors(
								containerColor = Color(0xFFF0F3F7),
								contentColor = PrimaryBlue
							)
						) {
							Text("Study Library", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
						}
					}

					Spacer(modifier = Modifier.height(16.dp))

					// Progress Section Header
					Text(
						"Progress by Category",
						fontSize = 16.sp,
						fontWeight = FontWeight.Bold,
						color = TextColor,
						modifier = Modifier.padding(top = 8.dp)
					)

					Text(
						"Total Attempts: ${state.totalAttempts}",
						fontSize = 12.sp,
						color = Color(0xFF6C757D),
						modifier = Modifier.padding(bottom = 8.dp)
					)

					// Progress Indicators by Category
					if (state.categoryScores.isEmpty()) {
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp),
							colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
						) {
							Text(
								"No progress yet. Start an exam to begin!",
								modifier = Modifier
									.fillMaxWidth()
									.padding(16.dp),
								fontSize = 14.sp,
								color = Color(0xFF6C757D),
								textAlign = androidx.compose.ui.text.style.TextAlign.Center
							)
						}
					} else {
						state.categoryScores.forEach { score ->
							CategoryProgressCard(score)
						}
					}

					Spacer(modifier = Modifier.height(32.dp))
				}
			}

			is DashboardUiState.Error -> {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(paddingValues)
						.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Text("Error loading dashboard", color = TextColor, fontWeight = FontWeight.Bold)
					Spacer(modifier = Modifier.height(8.dp))
					Text(state.message, color = Color(0xFF6C757D), fontSize = 12.sp)
				}
			}
		}
	}
}

@Composable
private fun CategoryProgressCard(score: CategoryScore) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = score.category,
					fontSize = 13.sp,
					fontWeight = FontWeight.SemiBold,
					color = TextColor
				)

				Text(
					text = "${score.correctCount}/${score.totalCount}",
					fontSize = 11.sp,
					color = Color(0xFF6C757D)
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			// Progress Bar with color based on percentage
			val progressColor = if (score.percentage >= 75f) SuccessGreen else PrimaryBlue

			LinearProgressIndicator(
				progress = { score.percentage / 100f },
				modifier = Modifier
					.fillMaxWidth()
					.height(6.dp),
				trackColor = Color(0xFFE8EAED),
				color = progressColor
			)

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = "${String.format("%.0f", score.percentage)}%",
				fontSize = 10.sp,
				color = if (score.percentage >= 75f) SuccessGreen else PrimaryBlue,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}
