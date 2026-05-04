package com.jigen.practicse.ui.screens.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jigen.practicse.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.collectLatest

private val ScreenBackground = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)
private val SuccessGreen = Color(0xFF188038)
private val ErrorRed = Color(0xFFD93025)

@Composable
fun ExamScreen(
	viewModel: ExamViewModel,
	modifier: Modifier = Modifier
) {
	val uiState by viewModel.uiState.collectAsState()

	when (val state = uiState) {
		is ExamUiState.Loading -> {
			Surface(modifier = modifier.fillMaxSize(), color = ScreenBackground) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = PrimaryBlue)
				}
			}
		}

		is ExamUiState.Error -> {
			Surface(modifier = modifier.fillMaxSize(), color = ScreenBackground) {
				Box(
					modifier = Modifier.fillMaxSize().padding(24.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = state.message,
						style = MaterialTheme.typography.bodyLarge,
						color = TextColor
					)
				}
			}
		}

		is ExamUiState.Success -> {
			val pagerState = rememberPagerState(
				initialPage = state.currentQuestionIndex,
				pageCount = { state.questions.size.coerceAtLeast(1) }
			)
			val currentQuestion = remember(state.questions, state.currentQuestionIndex) {
				state.questions.getOrNull(state.currentQuestionIndex)
			}

			LaunchedEffect(state.currentQuestionIndex) {
				if (pagerState.currentPage != state.currentQuestionIndex && state.questions.isNotEmpty()) {
					pagerState.scrollToPage(state.currentQuestionIndex)
				}
			}

			LaunchedEffect(Unit) {
				viewModel.effects.collectLatest { effect ->
					when (effect) {
						is ExamViewModel.ExamEffect.NavigateToPage -> {
							if (state.questions.isNotEmpty()) {
								pagerState.animateScrollToPage(effect.pageIndex)
							}
						}
						ExamViewModel.ExamEffect.ExamCompleted -> Unit
						ExamViewModel.ExamEffect.TimeExpired -> Unit
					}
				}
			}

			Scaffold(
				modifier = modifier.fillMaxSize(),
				containerColor = ScreenBackground,
				topBar = {
					ExamTopBar(remainingTimeMillis = state.remainingTimeMillis)
				},
				floatingActionButton = {
					FloatingActionButton(
						onClick = { viewModel.reportCurrentQuestion() },
						containerColor = PrimaryBlue,
						contentColor = Color.White,
						shape = RoundedCornerShape(16.dp)
					) {
						Text(text = "Report Error", modifier = Modifier.padding(horizontal = 12.dp))
					}
				}
			) { innerPadding ->
					ExamPagerContent(
						questions = state.questions,
						currentQuestionIndex = state.currentQuestionIndex,
						selectedAnswerIndex = state.selectedAnswerIndex,
						isSelectionLocked = state.isSelectionLocked,
						pagerState = pagerState,
						currentQuestion = currentQuestion,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding),
						onAnswerSelected = viewModel::selectAnswer
					)
			}
		}
	}
}


@Composable
private fun ExamTopBar(remainingTimeMillis: Long) {
	TopAppBar(
		title = {
			Text(
				text = "Civil Service Exam",
				color = TextColor,
				style = MaterialTheme.typography.titleMedium
			)
		},
		actions = {
			Text(
				text = formatDuration(remainingTimeMillis),
				color = PrimaryBlue,
				style = MaterialTheme.typography.labelLarge,
				modifier = Modifier.padding(end = 16.dp)
			)
		}
	)
}

@Composable
private fun ExamPagerContent(
	questions: List<QuestionEntity>,
	currentQuestionIndex: Int,
	selectedAnswerIndex: Int?,
	isSelectionLocked: Boolean,
	pagerState: androidx.compose.foundation.pager.PagerState,
	currentQuestion: QuestionEntity?,
	modifier: Modifier = Modifier,
	onAnswerSelected: (Int) -> Unit
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.Top
	) {
		PassageHeader(
			question = currentQuestion,
			modifier = Modifier.fillMaxWidth()
		)

		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 16.dp),
			pageSpacing = 16.dp,
			userScrollEnabled = questions.isNotEmpty()
		) { page ->
			val pageQuestion = questions.getOrNull(page)
			QuestionPage(
				question = pageQuestion,
				selectedAnswerIndex = if (page == currentQuestionIndex) selectedAnswerIndex else null,
				isLocked = page == currentQuestionIndex && isSelectionLocked,
				onAnswerSelected = onAnswerSelected
			)
		}
	}
}
@Composable
private fun PassageHeader(
	question: QuestionEntity?,
	modifier: Modifier = Modifier
) {
	if (question?.passage.isNullOrBlank()) {
		Spacer(modifier = Modifier.height(8.dp))
		return
	}

	Card(
		modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(
				text = question.category,
				style = MaterialTheme.typography.labelLarge,
				color = PrimaryBlue
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = question.passage.orEmpty(),
				style = MaterialTheme.typography.bodyLarge,
				color = TextColor,
				modifier = Modifier.verticalScroll(rememberScrollState())
			)
		}
	}
}

@Composable
private fun QuestionPage(
	question: QuestionEntity?,
	selectedAnswerIndex: Int?,
	isLocked: Boolean,
	onAnswerSelected: (Int) -> Unit
) {
	if (question == null) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator(color = PrimaryBlue)
		}
		return
	}

	Card(
		modifier = Modifier
			.fillMaxSize()
			.padding(bottom = 88.dp),
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(20.dp)
		) {
			Text(
				text = question.questionText,
				style = MaterialTheme.typography.bodyLarge,
				color = TextColor
			)
			Spacer(modifier = Modifier.height(20.dp))

			question.options.forEachIndexed { index, option ->
				val isSelected = selectedAnswerIndex == index
				val isCorrect = index == question.correctIndex
				val background = when {
					isLocked && isSelected && isCorrect -> SuccessGreen.copy(alpha = 0.12f)
					isLocked && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.12f)
					else -> Color(0xFFF1F3F4)
				}
				val borderColor = when {
					isLocked && isSelected && isCorrect -> SuccessGreen
					isLocked && isSelected && !isCorrect -> ErrorRed
					isSelected -> PrimaryBlue
					else -> Color.Transparent
				}

				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 12.dp),
					shape = RoundedCornerShape(18.dp),
					colors = CardDefaults.cardColors(containerColor = background),
					border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
					onClick = { if (!isLocked) onAnswerSelected(index) }
				) {
					Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
						Column(modifier = Modifier.fillMaxWidth()) {
							RowWithRadio(
								selected = isSelected,
								text = option,
								textColor = TextColor,
								accent = if (isLocked && isSelected && !isCorrect) ErrorRed else if (isLocked && isSelected && isCorrect) SuccessGreen else PrimaryBlue
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun RowWithRadio(
	selected: Boolean,
	text: String,
	textColor: Color,
	accent: Color
) {
	androidx.compose.foundation.layout.Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		RadioButton(selected = selected, onClick = null)
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = textColor,
			maxLines = 4,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f)
		)
	}
}

private fun formatDuration(remainingTimeMillis: Long): String {
	val totalSeconds = (remainingTimeMillis / 1000L).coerceAtLeast(0L)
	val minutes = totalSeconds / 60L
	val seconds = totalSeconds % 60L
	return "%02d:%02d".format(minutes, seconds)
}