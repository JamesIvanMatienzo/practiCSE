package com.jigen.practicse.ui.screens.exam

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jigen.practicse.ui.screens.exam.ExamViewModelNew.ExamEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ScreenBackground = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)
private val SuccessGreen = Color(0xFF188038)
private val ErrorRed = Color(0xFFD93025)
private val MutedGray = Color(0xFF6C757D)
private val FlagYellow = Color(0xFFF9AB00)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
	context: Context,
	modifier: Modifier = Modifier,
	sessionId: String = "new",
	onDeepDive: (String) -> Unit = {},
	onBack: () -> Unit = {},
	onResult: (score: Int, totalQuestions: Int) -> Unit = { _, _ -> },
) {
	val viewModel: ExamViewModelNew = viewModel(
		factory = ExamViewModelNew.factory(context, sessionMode = sessionId)
	)
	val uiState by viewModel.uiState.collectAsState()
	var reportStatusMessage by remember { mutableStateOf<String?>(null) }
	var showExitDialog by remember { mutableStateOf(false) }

	if (showExitDialog) {
		AlertDialog(
			onDismissRequest = { showExitDialog = false },
			title = { Text("Exit Exam?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextColor) },
			text = { Text("Your progress will be saved. You can continue later.", style = MaterialTheme.typography.bodyMedium, color = MutedGray) },
			confirmButton = {
				Button(
					onClick = { showExitDialog = false; onBack() },
					colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
					shape = RoundedCornerShape(8.dp)
				) { Text("Exit", style = MaterialTheme.typography.bodyMedium, color = Color.White) }
			},
			dismissButton = {
				androidx.compose.material3.TextButton(onClick = { showExitDialog = false }) {
					Text("Keep Going", style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue)
				}
			},
			containerColor = Color.White
		)
	}

	when (val state = uiState) {
		is ExamUiState.Loading -> {
			Surface(modifier = modifier.fillMaxSize(), color = ScreenBackground) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator(color = PrimaryBlue)
				}
			}
		}

		is ExamUiState.Error -> {
			Surface(modifier = modifier.fillMaxSize(), color = ScreenBackground) {
				Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
					Text(text = state.message, style = MaterialTheme.typography.bodyLarge, color = TextColor)
				}
			}
		}

		is ExamUiState.Success -> {
			val safeInitialPage = state.currentIndex.coerceIn(0, state.questions.lastIndex.coerceAtLeast(0))
			val pagerState = rememberPagerState(
				initialPage = safeInitialPage,
				pageCount = { state.questions.size.coerceAtLeast(1) }
			)

			LaunchedEffect(state.currentIndex) {
				if (state.questions.isNotEmpty() && pagerState.currentPage != state.currentIndex) {
					pagerState.animateScrollToPage(state.currentIndex)
				}
			}

			LaunchedEffect(pagerState) {
				snapshotFlow { pagerState.currentPage }.collect { page ->
					viewModel.goToQuestion(page)
				}
			}

			LaunchedEffect(Unit) {
				viewModel.effects.collectLatest { effect ->
					when (effect) {
						is ExamEffect.NavigateToPage -> Unit
						ExamEffect.ExamCompleted -> reportStatusMessage = null
						ExamEffect.TimeExpired -> reportStatusMessage = "Time is up. Your exam has been submitted."
						ExamEffect.QuestionReported -> reportStatusMessage = "Thanks. We logged this question for review."
					}
				}
			}

			Scaffold(
				modifier = modifier.fillMaxSize(),
				containerColor = ScreenBackground,
				topBar = {
					ExamTopBar(
						remainingTimeMillis = state.remainingTimeMillis,
						currentIndex = state.currentIndex,
						totalQuestions = state.totalQuestions,
						answeredCount = state.answeredCount,
						correctCount = state.correctCount,
						wrongCount = state.wrongCount,
						currentQuestion = state.currentQuestion,
						flaggedQuestionIds = state.flaggedQuestionIds,
						onToggleFlag = { viewModel.toggleFlagCurrentQuestion() },
						onExit = { showExitDialog = true }
					)
				}
			) { innerPadding ->
				ExamPagerContent(
					questions = state.questions,
					currentQuestionIndex = state.currentIndex,
					selectedAnswers = state.selectedAnswers,
					evaluatedQuestions = state.evaluatedQuestions,
					flaggedQuestionIds = state.flaggedQuestionIds,
					voidedQuestionIds = state.voidedQuestionIds,
					pagerState = pagerState,
					reportStatusMessage = reportStatusMessage,
					modifier = Modifier.fillMaxSize().padding(innerPadding),
					onAnswerSelected = viewModel::selectAnswer,
					onDeepDive = onDeepDive,
					onReportError = viewModel::reportCurrentQuestion,
					onPrevious = viewModel::previousQuestion,
					onNext = viewModel::nextQuestion,
					onReportDismissed = { reportStatusMessage = null }
				)
			}
		}

		is ExamUiState.Completed -> {
			// Navigate to the ResultScreen automatically
			LaunchedEffect(state) {
				onResult(state.totalScore, state.totalQuestions)
			}

			// Fallback UI shown briefly while navigating
			Surface(modifier = modifier.fillMaxSize(), color = ScreenBackground) {
				Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
					Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
						CircularProgressIndicator(color = PrimaryBlue)
						Spacer(modifier = Modifier.height(16.dp))
						Text(
							text = "Preparing your results…",
							style = MaterialTheme.typography.bodyLarge,
							color = TextColor
						)
					}
				}
			}
		}
	}
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExamTopBar(
	remainingTimeMillis: Long,
	currentIndex: Int,
	totalQuestions: Int,
	answeredCount: Int,
	correctCount: Int,
	wrongCount: Int,
	currentQuestion: QuestionUiState?,
	flaggedQuestionIds: Set<Int>,
	onToggleFlag: () -> Unit,
	onExit: () -> Unit
) {
	Column(modifier = Modifier.background(Color.White)) {
		TopAppBar(
			title = {
				Text(text = "Civil Service Exam", color = TextColor, style = MaterialTheme.typography.titleMedium)
			},
			navigationIcon = {
				androidx.compose.material3.IconButton(onClick = onExit) {
					androidx.compose.material3.Icon(
						imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = "Exit Exam",
						tint = TextColor
					)
				}
			},
			actions = {
				Button(
					onClick = onToggleFlag,
					modifier = Modifier.padding(end = 8.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = if (currentQuestion?.id in flaggedQuestionIds) FlagYellow else Color(0xFFE8F0FE)
					),
					shape = RoundedCornerShape(12.dp)
				) {
					Text(
						text = if (currentQuestion?.id in flaggedQuestionIds) "Flagged" else "Flag",
						color = if (currentQuestion?.id in flaggedQuestionIds) Color.White else PrimaryBlue,
						style = MaterialTheme.typography.labelSmall
					)
				}
				Text(
					text = formatDuration(remainingTimeMillis),
					color = PrimaryBlue,
					style = MaterialTheme.typography.labelLarge,
					modifier = Modifier.padding(end = 16.dp)
				)
			}
		)
		val progress = if (totalQuestions == 0) 0f else (currentIndex + 1f) / totalQuestions.toFloat()
		LinearProgressIndicator(
			progress = { progress },
			modifier = Modifier.fillMaxWidth().height(8.dp),
			color = PrimaryBlue,
			trackColor = Color(0xFFE3ECFA)
		)
		Row(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Q${currentIndex + 1}/$totalQuestions",
				modifier = Modifier,
				color = TextColor,
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
			)
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				CompactStatChip(label = "Correct", value = correctCount, bgColor = SuccessGreen)
				CompactStatChip(label = "Wrong", value = wrongCount, bgColor = ErrorRed)
			}
		}
	}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExamPagerContent(
	questions: List<QuestionUiState>,
	currentQuestionIndex: Int,
	selectedAnswers: Map<Int, String>,
	evaluatedQuestions: Set<Int>,
	flaggedQuestionIds: Set<Int>,
	voidedQuestionIds: Set<Int>,
	pagerState: androidx.compose.foundation.pager.PagerState,
	reportStatusMessage: String?,
	modifier: Modifier = Modifier,
	onAnswerSelected: (String) -> Unit,
	onDeepDive: (String) -> Unit,
	onReportError: () -> Unit,
	onPrevious: () -> Unit,
	onNext: () -> Unit,
	onReportDismissed: () -> Unit
) {
	var showReportDialog by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

// Auto-dismiss dialog after 3 seconds
	LaunchedEffect(reportStatusMessage) {
		if (!reportStatusMessage.isNullOrBlank()) {
			showReportDialog = true
			delay(3000L)
			showReportDialog = false
			onReportDismissed()
		}
	}

	if (showReportDialog && !reportStatusMessage.isNullOrBlank()) {
		AlertDialog(
			onDismissRequest = { 
				showReportDialog = false 
				onReportDismissed()
			},
			title = { Text("Thank You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = PrimaryBlue) },
			text = { Text(reportStatusMessage, style = MaterialTheme.typography.bodyMedium, color = TextColor) },
			confirmButton = {
				Button(
					onClick = { 
						showReportDialog = false 
						onReportDismissed()
					},
					colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
					shape = RoundedCornerShape(8.dp)
				) {
					Text("OK", style = MaterialTheme.typography.bodyMedium, color = Color.White)
				}
			},
			containerColor = Color.White
		)
	}

	Column(modifier = modifier, verticalArrangement = Arrangement.Top) {

		HorizontalPager(
			state = pagerState,
			modifier = Modifier.weight(1f),
			contentPadding = PaddingValues(horizontal = 16.dp),
			pageSpacing = 16.dp,
			flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
			userScrollEnabled = questions.isNotEmpty()
		) { page ->
			val pageQuestion = questions.getOrNull(page)
			QuestionPage(
				question = pageQuestion,
				selectedAnswer = pageQuestion?.let { selectedAnswers[it.id] },
				isEvaluated = pageQuestion?.id in evaluatedQuestions,
				isFlagged = pageQuestion?.id in flaggedQuestionIds,
				isVoided = pageQuestion?.id in voidedQuestionIds,
				onAnswerSelected = onAnswerSelected,
				onDeepDive = onDeepDive,
				onReportError = onReportError
			)
		}

		LazyRow(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp)
		) {
			itemsIndexed(questions) { index, question ->
				val isCurrent = index == pagerState.currentPage
				val selected = selectedAnswers[question.id]
				val isAnsweredCorrectly = question.id in evaluatedQuestions && answersMatch(selected, question.correctAnswer)
				val isAnsweredWrong = question.id in evaluatedQuestions && !answersMatch(selected, question.correctAnswer)
				val isFlagged = question.id in flaggedQuestionIds
				val bgColor = when {
					isCurrent -> PrimaryBlue
					isFlagged -> FlagYellow
					isAnsweredCorrectly -> SuccessGreen
					isAnsweredWrong -> ErrorRed
					else -> Color(0xFFE8EAED)
				}

				Card(
					shape = RoundedCornerShape(8.dp),
					colors = CardDefaults.cardColors(containerColor = bgColor),
					onClick = {
						scope.launch {
							pagerState.animateScrollToPage(index)
						}
					}
				) {
					Text(
						text = "${index + 1}",
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
						color = Color.White,
						style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
					)
				}
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp)
		) {
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
		}
	}
}

@Composable
private fun QuestionPage(
	question: QuestionUiState?,
	selectedAnswer: String?,
	isEvaluated: Boolean,
	isFlagged: Boolean,
	isVoided: Boolean,
	onAnswerSelected: (String) -> Unit,
	onDeepDive: (String) -> Unit,
	onReportError: () -> Unit
) {
	if (question == null) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			CircularProgressIndicator(color = PrimaryBlue)
		}
		return
	}

	Card(
		modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
	) {
		Column(modifier = Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp)) {
			if (!question.referenceText.isNullOrBlank()) {
				Card(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(14.dp),
					colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FE))
				) {
					Column(modifier = Modifier.padding(12.dp)) {
						Text("Reference", style = MaterialTheme.typography.titleMedium, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
						Spacer(modifier = Modifier.height(6.dp))
						Text(text = question.referenceText.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = TextColor)
					}
				}
				Spacer(modifier = Modifier.height(14.dp))
			}

			if (isVoided) {
				Text(
					text = "Question Reported & Voided",
					color = MutedGray,
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
					modifier = Modifier.padding(bottom = 8.dp)
				)
			}
			Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				if (isFlagged) {
					Box(modifier = Modifier.size(8.dp).background(FlagYellow, CircleShape))
				}
				Text(text = question.text, style = MaterialTheme.typography.bodyLarge, color = if (isVoided) MutedGray else TextColor)
			}

			Spacer(modifier = Modifier.height(20.dp))

			question.shuffledOptions.forEach { option ->
				val isSelected = answersMatch(selectedAnswer, option)
				val isSelectedCorrect = answersMatch(selectedAnswer, question.correctAnswer)
				val background = when {
					isEvaluated && isSelected && isSelectedCorrect -> Color(0xFFE7F5EA)
					isEvaluated && isSelected && !isSelectedCorrect -> Color(0xFFFDECEC)
					isSelected -> Color(0xFFEAF2FF)
					else -> Color(0xFFF1F3F4)
				}
				val borderColor = when {
					isEvaluated && isSelected && isSelectedCorrect -> SuccessGreen
					isEvaluated && isSelected && !isSelectedCorrect -> ErrorRed
					isSelected -> PrimaryBlue
					else -> Color.Transparent
				}

				Card(
					modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
					shape = RoundedCornerShape(18.dp),
					colors = CardDefaults.cardColors(containerColor = background),
					border = BorderStroke(1.dp, borderColor),
					onClick = { if (!isVoided) onAnswerSelected(option) }
				) {
					Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
						RowWithRadio(
							selected = isSelected,
							text = option,
							textColor = TextColor
						)
					}
				}
			}

			if (isEvaluated) {
				Text(
					text = if (answersMatch(selectedAnswer, question.correctAnswer)) {
						"Correct answer. Great job."
					} else {
						"That answer is incorrect. Correct answer: ${question.correctAnswer}"
					},
					color = if (answersMatch(selectedAnswer, question.correctAnswer)) SuccessGreen else ErrorRed,
					style = MaterialTheme.typography.bodyMedium.copy(
						fontWeight = FontWeight.SemiBold
					),
					modifier = Modifier.padding(bottom = 10.dp)
				)
			}

			Button(
				onClick = { onDeepDive(question.id.toString()) },
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
				shape = RoundedCornerShape(18.dp)
			) {
				Text(text = "Ask AI for Deep Explanation", style = MaterialTheme.typography.bodyMedium)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Button(
				onClick = onReportError,
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDECEC)), 
				shape = RoundedCornerShape(18.dp),
				enabled = !isVoided
			) {
				Text(text = if (isVoided) "Question Reported & Voided" else "Report a Problem in this Question", style = MaterialTheme.typography.bodyMedium, color = if (isVoided) MutedGray else ErrorRed) 
			}
		}
	}
}

@Composable
private fun RowWithRadio(
	selected: Boolean,
	text: String,
	textColor: Color
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		RadioButton(selected = selected, onClick = null)
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = textColor,
			modifier = Modifier.weight(1f)
		)
	}
}

private fun answersMatch(left: String?, right: String?): Boolean {
	val l = left?.trim()
	val r = right?.trim()
	return !l.isNullOrEmpty() && !r.isNullOrEmpty() && l.equals(r, ignoreCase = true)
}

@Composable
private fun CompactStatChip(
	label: String,
	value: Int,
	bgColor: Color
) {
	Surface(
		modifier = Modifier
			.clip(RoundedCornerShape(8.dp)),
		color = bgColor.copy(alpha = 0.15f),
		contentColor = bgColor
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
				color = bgColor
			)
			Text(
				text = ": $value",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
				color = bgColor
			)
		}
	}
}

private fun formatDuration(remainingTimeMillis: Long): String {
	val totalSeconds = (remainingTimeMillis / 1000L).coerceAtLeast(0L)
	val minutes = totalSeconds / 60L
	val seconds = totalSeconds % 60L
	return "%02d:%02d".format(minutes, seconds)
}