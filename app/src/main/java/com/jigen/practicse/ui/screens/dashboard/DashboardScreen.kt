package com.jigen.practicse.ui.screens.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1976D2)
private val PrimaryBlueSoft = Color(0xFFEAF2FF)
private val TextColor = Color(0xFF202124)
private val MutedText = Color(0xFF6C757D)
private val BorderColor = Color(0xFFE6E8EC)
private val CardShadow = Color(0x14000000)
private val SuccessGreen = Color(0xFF188038)
private val OfflineGray = Color(0xFF9CA3AF)

@Composable
fun DashboardScreen(
	context: Context,
	onProfileClick: () -> Unit = {},
	onStartNewExam: (Int) -> Unit = {},
	onContinueSession: () -> Unit = {},
	onRanking: () -> Unit = {},
	onStudyLibrary: () -> Unit = {}
) {
	val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(context))
	val uiState by viewModel.uiState.collectAsState()

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(SurfaceColor)
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp)
			.padding(top = 14.dp, bottom = 20.dp)
	) {
		DashboardHeader(onProfileClick = onProfileClick)

		Spacer(modifier = Modifier.height(14.dp))

		when (val state = uiState) {
			is DashboardUiState.Loading -> {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.height(420.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					CircularProgressIndicator(color = PrimaryBlue)
					Spacer(modifier = Modifier.height(16.dp))
					Text(
						"Loading your progress...",
						color = TextColor,
						fontSize = 14.sp
					)
				}
			}

			is DashboardUiState.Success -> {
				DashboardBody(
					state = state,
					onStartNewExam = onStartNewExam,
					onContinueSession = onContinueSession,
					onRanking = onRanking,
					onStudyLibrary = onStudyLibrary
				)
			}

			is DashboardUiState.Error -> {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.height(420.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Text(
						"Error loading dashboard",
						color = TextColor,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						state.message,
						color = MutedText,
						fontSize = 12.sp,
						textAlign = TextAlign.Center
					)
				}
			}
		}
	}
}

@Composable
private fun DashboardHeader(onProfileClick: () -> Unit) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(34.dp)
					.clip(RoundedCornerShape(10.dp))
					.background(PrimaryBlueSoft),
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = com.jigen.practicse.R.drawable.ic_practicse_logo),
					contentDescription = null,
					tint = Color.Unspecified,
					modifier = Modifier.size(24.dp)
				)
			}

			Spacer(modifier = Modifier.width(10.dp))

			Column {
				Text(
					text = "practiCSE",
					fontSize = 20.sp,
					fontWeight = FontWeight.Bold,
					color = TextColor
				)
				Text(
					text = "Civil Service exam reviewer",
					fontSize = 10.sp,
					color = MutedText
				)
			}
		}

		Row(
			modifier = Modifier
				.clickable(onClick = onProfileClick)
				.clip(RoundedCornerShape(28.dp))
				.border(1.dp, BorderColor, RoundedCornerShape(28.dp))
				.background(Color.White)
				.padding(horizontal = 10.dp, vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(28.dp)
					.clip(CircleShape)
					.background(PrimaryBlueSoft),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Filled.AccountCircle,
					contentDescription = null,
					tint = PrimaryBlue,
					modifier = Modifier.size(22.dp)
				)
			}

			Spacer(modifier = Modifier.width(10.dp))

			Text(
				text = "Profile",
				fontSize = 12.sp,
				fontWeight = FontWeight.SemiBold,
				color = TextColor
			)
		}
	}
}

@Composable
private fun DashboardBody(
	state: DashboardUiState.Success,
	onStartNewExam: (Int) -> Unit,
	onContinueSession: () -> Unit,
	onRanking: () -> Unit,
	onStudyLibrary: () -> Unit
) {
	var showQuestionDialog by remember { mutableStateOf(false) }

	Spacer(modifier = Modifier.height(12.dp))

	ActionCard(
		icon = Icons.Filled.Info,
		title = "Start a New Exam",
		description = "Test yourself with a fresh set of questions",
		iconBackground = PrimaryBlue,
		onClick = { showQuestionDialog = true }
	)

	if (showQuestionDialog) {
		QuestionCountDialog(
			title = "Start New Exam",
			helper = "How many random questions do you want?",
			maxAllowed = state.availableQuestionCount,
			onDismiss = { showQuestionDialog = false },
			onConfirm = { requested ->
				showQuestionDialog = false
				onStartNewExam(requested)
			}
		)
	}

	Spacer(modifier = Modifier.height(14.dp))

	ActionCard(
		icon = Icons.Filled.PlayArrow,
		title = "Continue Last Session",
		description = "Pick up where you left off",
		iconBackground = PrimaryBlue,
		enabled = state.hasSessionToResume,
		onClick = onContinueSession
	)

	Spacer(modifier = Modifier.height(10.dp))

	StatusPill(
		text = "Active Path: ${state.activeTrackLabel}",
		textColor = PrimaryBlue,
		icon = Icons.Filled.Star,
		iconTint = PrimaryBlue
	)

	Spacer(modifier = Modifier.height(16.dp))

	Text(
		text = "PROGRESS TRACKER — SUBJECT PERFORMANCE",
		fontSize = 11.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 1.sp,
		color = MutedText
	)

	Spacer(modifier = Modifier.height(12.dp))

	if (state.categoryScores.isEmpty()) {
		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = Color.White),
			elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
			shape = RoundedCornerShape(16.dp)
		) {
			Column(modifier = Modifier.padding(18.dp)) {
				Text("No progress yet", fontWeight = FontWeight.SemiBold, color = TextColor)
				Spacer(modifier = Modifier.height(4.dp))
				Text("Start an exam to begin tracking performance.", color = MutedText, fontSize = 12.sp)
			}
		}
	} else {
		state.categoryScores.take(3).forEachIndexed { index, score ->
			Spacer(modifier = Modifier.height(if (index == 0) 0.dp else 10.dp))
			ProgressCard(score = score, showRecentActivity = index == 2)
		}
	}

	Spacer(modifier = Modifier.height(14.dp))

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		OutlinedActionButton(
			icon = Icons.Filled.Star,
			label = "Ranking",
			onClick = onRanking,
			modifier = Modifier.weight(1f)
		)

		OutlinedActionButton(
			icon = Icons.Filled.Info,
			label = "Browse Library",
			onClick = onStudyLibrary,
			modifier = Modifier.weight(1f)
		)
	}

	Spacer(modifier = Modifier.height(14.dp))

	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(containerColor = Color.Transparent),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		shape = RoundedCornerShape(18.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(
					Brush.horizontalGradient(
						listOf(Color(0xFFE8F1FF), Color(0xFFF4ECFF))
					)
				)
				.padding(18.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(42.dp)
						.clip(CircleShape)
						.background(Color.White),
					contentAlignment = Alignment.Center
				) {
					Icon(Icons.Filled.Info, contentDescription = null, tint = PrimaryBlue)
				}

				Spacer(modifier = Modifier.width(12.dp))

				Column(modifier = Modifier.weight(1f)) {
					Text("Keep it up!", fontWeight = FontWeight.Bold, color = TextColor)
					Text(
						"You're making great progress.",
						color = MutedText,
						fontSize = 12.sp,
						lineHeight = 16.sp
					)
				}
			}
		}
	}

	Spacer(modifier = Modifier.height(14.dp))

	StatusPill(
		text = "Offline Mode: Active",
		textColor = TextColor,
		icon = Icons.Filled.Info,
		iconTint = OfflineGray
	)

	Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun StatusPill(
	text: String,
	textColor: Color,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	iconTint: Color
) {
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.border(1.dp, BorderColor, RoundedCornerShape(999.dp))
			.background(Color.White)
			.padding(horizontal = 12.dp, vertical = 7.dp),
			verticalAlignment = Alignment.CenterVertically
	) {
		Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
		Spacer(modifier = Modifier.width(6.dp))
		Text(text, color = textColor, fontSize = 11.sp, lineHeight = 14.sp)
	}
}

@Composable
private fun QuestionCountDialog(
	title: String,
	helper: String,
	maxAllowed: Int,
	onDismiss: () -> Unit,
	onConfirm: (Int) -> Unit
) {
	var input by remember { mutableStateOf(if (maxAllowed >= 10) "10" else maxAllowed.coerceAtLeast(1).toString()) }
	val parsed = input.toIntOrNull()
	val isValid = parsed != null && parsed in 1..maxAllowed
	val errorText = when {
		maxAllowed <= 0 -> "No questions available in the database."
		input.isBlank() -> "Enter a number between 1 and $maxAllowed"
		parsed == null -> "Numbers only"
		parsed < 1 -> "Minimum is 1"
		parsed > maxAllowed -> "Maximum is $maxAllowed"
		else -> null
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title, fontWeight = FontWeight.Bold) },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(helper, color = MutedText, fontSize = 13.sp)
				OutlinedTextField(
					value = input,
					onValueChange = { value ->
						input = value.filter { it.isDigit() }
					},
					label = { Text("Question count") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					isError = errorText != null,
					modifier = Modifier.fillMaxWidth()
				)
				if (errorText != null) {
					Text(errorText, color = Color(0xFFD93025), fontSize = 12.sp)
				}
			}
		},
		confirmButton = {
			TextButton(onClick = { if (isValid) onConfirm(parsed ?: 1) }, enabled = isValid) {
				Text("Start")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) { Text("Cancel") }
		}
	)
}

@Composable
private fun ActionCard(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	title: String,
	description: String,
	iconBackground: Color,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled, onClick = onClick),
		shape = RoundedCornerShape(18.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(54.dp)
					.clip(CircleShape)
					.background(iconBackground),
				contentAlignment = Alignment.Center
			) {
				Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
			}

			Spacer(modifier = Modifier.width(16.dp))

			Column(modifier = Modifier.weight(1f)) {
				Text(title, fontWeight = FontWeight.Bold, color = TextColor, fontSize = 16.sp)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					description,
					color = MutedText,
					fontSize = 13.sp,
					lineHeight = 18.sp,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

@Composable
private fun OutlinedActionButton(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Button(
		onClick = onClick,
		modifier = modifier.height(50.dp),
		shape = RoundedCornerShape(16.dp),
		border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
		colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryBlue)
	) {
		Icon(icon, contentDescription = null, tint = PrimaryBlue)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = label,
			fontWeight = FontWeight.SemiBold,
			fontSize = 12.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Center,
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
private fun ProgressCard(
	score: CategoryScore,
	showRecentActivity: Boolean
) {
	val progressColor = if (score.percentage >= 75f) SuccessGreen else PrimaryBlue

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
	) {
		Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
					Text(score.categoryLabel, color = TextColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
					Text(
						text = "${score.correctCount} correct out of ${score.totalCount} attempts",
						color = MutedText,
						fontSize = 11.sp
					)
				Text(
					text = "${String.format("%.0f", score.percentage)}%",
					color = progressColor,
					fontWeight = FontWeight.Bold,
					fontSize = 12.sp
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			LinearProgressIndicator(
				progress = { score.percentage / 100f },
				modifier = Modifier
					.fillMaxWidth()
					.height(6.dp),
				trackColor = Color(0xFFE5E7EB),
				color = progressColor
			)

			if (showRecentActivity) {
				Spacer(modifier = Modifier.height(6.dp))
				Text("Recent activity", color = MutedText, fontSize = 11.sp)
			}
		}
	}
}
