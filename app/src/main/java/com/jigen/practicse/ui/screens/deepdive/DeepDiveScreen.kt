package com.jigen.practicse.ui.screens.deepdive

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private val ScreenBackground = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)
private val MutedText = Color(0xFF6C757D)
private val ErrorRed = Color(0xFFD93025)

@Composable
fun DeepDiveScreen(
	context: Context,
	questionId: String,
	onBack: () -> Unit
) {
	val viewModel: DeepDiveViewModel = viewModel(factory = DeepDiveViewModel.factory(context, questionId))
	val uiState by viewModel.uiState.collectAsState()

	Scaffold(containerColor = ScreenBackground) { paddingValues ->
		when (val state = uiState) {
			DeepDiveUiState.Loading -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = PrimaryBlue)
				}
			}

			is DeepDiveUiState.Error -> {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
						.padding(24.dp),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = state.message,
						color = TextColor,
						fontWeight = FontWeight.SemiBold
					)
					Spacer(modifier = Modifier.height(16.dp))
					Button(
						onClick = onBack,
						colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
						shape = RoundedCornerShape(18.dp)
					) {
						Text("Back")
					}
				}
			}

			is DeepDiveUiState.Success -> {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.background(ScreenBackground)
						.verticalScroll(rememberScrollState())
						.padding(paddingValues)
						.padding(20.dp)
				) {
					Text(
						text = "Deep Dive with Groq",
						fontSize = 22.sp,
						fontWeight = FontWeight.Bold,
						color = PrimaryBlue
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = state.question.category,
						color = MutedText,
						fontSize = 13.sp
					)
					Spacer(modifier = Modifier.height(16.dp))

					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(containerColor = Color.White),
						elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
						shape = RoundedCornerShape(20.dp)
					) {
						Column(modifier = Modifier.padding(18.dp)) {
							Text("Question", fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
							Spacer(modifier = Modifier.height(8.dp))
							Text(text = state.question.questionText, color = TextColor)
							if (!state.question.referenceText.isNullOrBlank()) {
								Spacer(modifier = Modifier.height(12.dp))
								Text("Reference", fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
								Spacer(modifier = Modifier.height(8.dp))
								Text(text = state.question.referenceText.orEmpty(), color = TextColor)
							}
						}
					}

					Spacer(modifier = Modifier.height(16.dp))

					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(containerColor = Color.White),
						elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
						shape = RoundedCornerShape(20.dp)
					) {
						Column(modifier = Modifier.padding(18.dp)) {
							Text("Groq Explanation", fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
							Spacer(modifier = Modifier.height(8.dp))
							Text(text = state.explanation, color = TextColor, lineHeight = 22.sp)
						}
					}

					Spacer(modifier = Modifier.height(20.dp))

					Button(
						onClick = onBack,
						modifier = Modifier.fillMaxWidth(),
						colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
						shape = RoundedCornerShape(18.dp)
					) {
						Text("Back")
					}
				}
			}
		}
	}
}
