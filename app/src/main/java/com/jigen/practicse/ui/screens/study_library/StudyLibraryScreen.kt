package com.jigen.practicse.ui.screens.study_library

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)
private val MutedText = Color(0xFF6C757D)
private val AccentBackground = Color(0xFFEAF1FE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyLibraryScreen(
	context: Context,
	onBack: () -> Unit = {},
	onStartPractice: (String, Int) -> Unit = { _, _ -> }
) {
	val viewModel: StudyLibraryViewModel = viewModel(factory = StudyLibraryViewModel.factory(context))
	val state by viewModel.uiState.collectAsState()
	var pendingCategory by remember { mutableStateOf<StudyCategoryItem?>(null) }

	Scaffold(
		containerColor = SurfaceColor,
		topBar = {
			TopAppBar(
				title = { Text("Study Library", fontWeight = FontWeight.Bold) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
			)
		}
	) { paddingValues ->
		when (val s = state) {
			is StudyLibraryUiState.Loading -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = PrimaryBlue)
				}
			}

			is StudyLibraryUiState.Success -> {
				LazyColumn(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
						.padding(horizontal = 16.dp, vertical = 12.dp)
				) {
					item {
						Text(
							text = "Choose a category to practice",
							fontSize = 14.sp,
							color = MutedText,
							modifier = Modifier.padding(bottom = 12.dp)
						)
					}

					items(s.categories.chunked(2)) { row ->
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							row.forEach { category ->
								StudyCategoryCard(
									category = category,
									modifier = Modifier.weight(1f),
									onClick = { pendingCategory = category }
								)
							}
							if (row.size == 1) {
								Spacer(modifier = Modifier.weight(1f))
							}
						}
						Spacer(modifier = Modifier.height(12.dp))
					}
				}
			}

			is StudyLibraryUiState.Error -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = s.message,
						color = TextColor,
						fontSize = 14.sp
					)
				}
			}
		}

		pendingCategory?.let { selected ->
			QuestionCountDialog(
				title = selected.title,
				helper = "How many random questions do you want for this category?",
				maxAllowed = selected.questionCount,
				onDismiss = { pendingCategory = null },
				onConfirm = { requested ->
					onStartPractice(selected.categoryKey, requested)
					pendingCategory = null
				}
			)
		}
	}
}

@Composable
private fun StudyCategoryCard(
	category: StudyCategoryItem,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	val iconRes = when {
		category.categoryKey == "numerical_ability" -> com.jigen.practicse.R.drawable.ic_numerical_ability
		category.categoryKey == "verbal_ability" -> com.jigen.practicse.R.drawable.ic_verbal_ability
		category.categoryKey == "general_information" -> com.jigen.practicse.R.drawable.ic_general_information
		else -> com.jigen.practicse.R.drawable.ic_general_information
	}

	Card(
		modifier = modifier
			.shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(containerColor = Color.White),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Box(
				modifier = Modifier
					.size(72.dp)
					.clip(RoundedCornerShape(16.dp)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = iconRes),
					contentDescription = category.title,
					modifier = Modifier.size(44.dp),
					tint = Color.Unspecified
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = category.title,
				fontSize = 14.sp,
				fontWeight = FontWeight.SemiBold,
				color = TextColor,
				lineHeight = 18.sp,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(6.dp))
			Text(
				text = "${category.questionCount} questions",
				fontSize = 12.sp,
				color = MutedText,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
		}
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
		maxAllowed <= 0 -> "No questions available in this category."
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

data class StudyCategoryItem(
	val title: String,
	val categoryKey: String,
	val questionCount: Int
)

sealed class StudyLibraryUiState {
	object Loading : StudyLibraryUiState()
	data class Success(val categories: List<StudyCategoryItem>) : StudyLibraryUiState()
	data class Error(val message: String) : StudyLibraryUiState()
}
