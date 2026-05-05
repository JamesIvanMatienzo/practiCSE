package com.jigen.practicse.ui.screens.result

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(
	sessionId: String,
	onBackToDashboard: () -> Unit
) {
	// Mock score for demo
	val score = 75
	val isPassed = score >= 80

	Scaffold(
		containerColor = Color(0xFFF8F9FA)
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(paddingValues)
				.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Text(
				"practiCSE",
				fontSize = 20.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF1976D2)
			)

			Spacer(modifier = Modifier.height(48.dp))

			// Score Circle
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(300.dp),
				contentAlignment = Alignment.Center
			) {
				Box(
					modifier = Modifier
						.padding(32.dp)
						.background(
							color = if (isPassed) Color(0xFF188038).copy(alpha = 0.1f) else Color(0xFFD93025).copy(alpha = 0.1f),
							shape = CircleShape
						)
						.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center,
						modifier = Modifier.fillMaxSize()
					) {
						Box(
							modifier = Modifier
								.background(
									color = if (isPassed) Color(0xFF188038) else Color(0xFFD93025),
									shape = CircleShape
								)
								.padding(24.dp),
							contentAlignment = Alignment.Center
						) {
							Text(
								if (isPassed) "✓" else "✗",
								fontSize = 64.sp,
								color = Color.White,
								fontWeight = FontWeight.Bold
							)
						}

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							"$score%",
							fontSize = 48.sp,
							fontWeight = FontWeight.Bold,
							color = if (isPassed) Color(0xFF188038) else Color(0xFFD93025)
						)

						Text(
							if (isPassed) "You Passed!" else "You Need to Review",
							fontSize = 16.sp,
							fontWeight = FontWeight.Bold,
							color = Color(0xFF202124),
							modifier = Modifier.padding(top = 12.dp)
						)

						Text(
							if (isPassed) "Great work! Ready for the next one?" else "Keep practicing!",
							fontSize = 12.sp,
							color = Color(0xFF6C757D),
							modifier = Modifier.padding(top = 6.dp)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				onClick = onBackToDashboard,
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
				shape = RoundedCornerShape(28.dp)
			) {
				Text(
					"Back to Dashboard",
					fontSize = 14.sp,
					fontWeight = FontWeight.Bold,
					color = Color.White
				)
			}

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				"INCORRECT QUESTIONS",
				fontSize = 12.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF202124),
				modifier = Modifier.fillMaxWidth(),
				textAlign = TextAlign.Start
			)

			Spacer(modifier = Modifier.height(12.dp))

			// Mock incorrect question
			Card(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.cardColors(containerColor = Color.White),
				elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						"All lawyers are professionals. Some professionals are teachers. Which conclusion is valid?",
						fontSize = 13.sp,
						color = Color(0xFF202124),
						fontWeight = FontWeight.Medium
					)
					Text(
						"Review Explanation >",
						fontSize = 12.sp,
						color = Color(0xFF1976D2),
						fontWeight = FontWeight.SemiBold,
						modifier = Modifier.padding(top = 8.dp)
					)
				}
			}
		}
	}
}
