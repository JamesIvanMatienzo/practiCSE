package com.jigen.practicse.ui.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val SurfaceColor   = Color(0xFFF8F9FA)
private val PrimaryBlue    = Color(0xFF1A73E8)
private val SuccessGreen   = Color(0xFF2E7D32)
private val ErrorRed       = Color(0xFFD32F2F)
private val TextColor      = Color(0xFF202124)
private val MutedText      = Color(0xFF6C757D)
private val CardWhite      = Color(0xFFFFFFFF)
private val SuccessSoft    = Color(0xFFE8F5E9)
private val ErrorSoft      = Color(0xFFFFEBEE)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun ResultScreen(
	sessionId: String,
	score: Int = 0,
	totalQuestions: Int = 0,
	onBackToDashboard: () -> Unit
) {
	val percentage = if (totalQuestions > 0) (score * 100f) / totalQuestions else 0f
	val isPassed = percentage >= 50f
	val accentColor = if (isPassed) SuccessGreen else ErrorRed
	val softBg = if (isPassed) SuccessSoft else ErrorSoft

	Scaffold(
		containerColor = SurfaceColor
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(paddingValues)
				.padding(horizontal = 24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Spacer(modifier = Modifier.height(48.dp))

			// ── App branding ──────────────────────────────────────
			Text(
				text = "Exam Results",
				style = MaterialTheme.typography.displayMedium.copy(
					fontWeight = FontWeight.Bold,
					fontSize = 28.sp,
					letterSpacing = (-0.5).sp
				),
				color = TextColor
			)

			Spacer(modifier = Modifier.height(32.dp))

			// ── Hero Card ─────────────────────────────────────────
			Card(
				modifier = Modifier
					.fillMaxWidth()
					.shadow(12.dp, RoundedCornerShape(24.dp)),
				shape = RoundedCornerShape(24.dp),
				colors = CardDefaults.cardColors(containerColor = CardWhite),
				elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(32.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					// ── Pass / Fail icon ──────────────────────────
					Box(
						modifier = Modifier
							.size(80.dp)
							.background(softBg, CircleShape),
						contentAlignment = Alignment.Center
					) {
						Box(
							modifier = Modifier
								.size(56.dp)
								.background(accentColor, CircleShape),
							contentAlignment = Alignment.Center
						) {
							Icon(
								imageVector = if (isPassed) Icons.Filled.Check else Icons.Filled.Close,
								contentDescription = if (isPassed) "Passed" else "Failed",
								tint = CardWhite,
								modifier = Modifier.size(32.dp)
							)
						}
					}

					Spacer(modifier = Modifier.height(24.dp))

					// ── Percentage display ────────────────────────
					Text(
						text = "${percentage.toInt()}%",
						style = MaterialTheme.typography.displayMedium.copy(
							fontWeight = FontWeight.ExtraBold,
							fontSize = 64.sp,
							letterSpacing = (-2).sp
						),
						color = accentColor
					)

					Spacer(modifier = Modifier.height(8.dp))

					// ── Status label ──────────────────────────────
					Text(
						text = if (isPassed) "You Passed!" else "You Need to Review",
						style = MaterialTheme.typography.titleMedium.copy(
							fontWeight = FontWeight.Bold
						),
						color = TextColor
					)

					Spacer(modifier = Modifier.height(6.dp))

					// ── Subtitle ──────────────────────────────────
					Text(
						text = if (isPassed)
							"Great work! Ready for the next one?"
						else
							"Keep trying! Review your answers and try again.",
						style = MaterialTheme.typography.bodyMedium,
						color = MutedText,
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(28.dp))

					// ── Score breakdown row ───────────────────────
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.background(SurfaceColor, RoundedCornerShape(16.dp))
							.padding(16.dp),
						horizontalArrangement = Arrangement.SpaceEvenly
					) {
						ScoreColumn(
							label = "Correct",
							value = "$score",
							color = SuccessGreen
						)
						Box(
							modifier = Modifier
								.width(1.dp)
								.height(40.dp)
								.background(Color(0xFFDCE0E4))
						)
						ScoreColumn(
							label = "Wrong",
							value = "${totalQuestions - score}",
							color = ErrorRed
						)
						Box(
							modifier = Modifier
								.width(1.dp)
								.height(40.dp)
								.background(Color(0xFFDCE0E4))
						)
						ScoreColumn(
							label = "Total",
							value = "$totalQuestions",
							color = PrimaryBlue
						)
					}
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			// ── Back to Dashboard button ──────────────────────────
			Button(
				onClick = onBackToDashboard,
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
				shape = RoundedCornerShape(24.dp)
			) {
				Text(
					text = "Back to Dashboard",
					style = MaterialTheme.typography.bodyMedium.copy(
						fontWeight = FontWeight.Bold,
						fontSize = 15.sp
					),
					color = Color.White
				)
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

// ── Helper composable ─────────────────────────────────────────────────────────
@Composable
private fun ScoreColumn(label: String, value: String, color: Color) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = value,
			style = MaterialTheme.typography.titleLarge.copy(
				fontWeight = FontWeight.ExtraBold,
				fontSize = 22.sp
			),
			color = color
		)
		Spacer(modifier = Modifier.height(2.dp))
		Text(
			text = label,
			style = MaterialTheme.typography.bodySmall.copy(
				fontWeight = FontWeight.Medium
			),
			color = MutedText
		)
	}
}
