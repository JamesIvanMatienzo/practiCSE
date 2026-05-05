package com.jigen.practicse.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.jigen.practicse.data.local.AppPreferencesStore

@Composable
fun OnboardingScreen(
	context: Context,
	onTrackSelected: (trackName: String) -> Unit
) {
	val store = remember(context) { AppPreferencesStore(context) }
	var selectedTrack by remember { mutableStateOf<String?>(null) }

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
			Spacer(modifier = Modifier.height(32.dp))

			Text(
				"practiCSE",
				fontSize = 32.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF1976D2)
			)

			Text(
				"A distraction-free, offline-first Civil Service Exam Reviewer",
				fontSize = 12.sp,
				color = Color(0xFF6C757D),
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 8.dp)
			)

			Spacer(modifier = Modifier.height(48.dp))

			Text(
				"Select Your Track",
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF202124),
				modifier = Modifier.align(Alignment.Start)
			)

			Spacer(modifier = Modifier.height(16.dp))

			// Professional Track
			TrackCard(
				title = "Professional Track",
				description = "Math, Logic, Constitution, and General Information",
				isSelected = selectedTrack == "professional",
				onClick = { selectedTrack = "professional" }
			)

			Spacer(modifier = Modifier.height(12.dp))

			// Sub-Professional Track
			TrackCard(
				title = "Sub-Professional Track",
				description = "Numerical Reasoning, Clerical Operations, and Spelling",
				isSelected = selectedTrack == "sub_professional",
				onClick = { selectedTrack = "sub_professional" }
			)

			Spacer(modifier = Modifier.height(48.dp))

			Button(
				onClick = {
					if (selectedTrack != null) {
						store.setActiveTrack(selectedTrack!!)
						onTrackSelected(selectedTrack!!)
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = Color(0xFF1976D2),
					disabledContainerColor = Color(0xFFCED4DA)
				),
				enabled = selectedTrack != null,
				shape = RoundedCornerShape(28.dp)
			) {
				Text(
					"Continue",
					fontSize = 16.sp,
					fontWeight = FontWeight.Bold,
					color = Color.White
				)
			}

			Spacer(modifier = Modifier.height(12.dp))

			Text(
				"No App, No Paywall, Fully Offline",
				fontSize = 12.sp,
				color = Color(0xFF6C757D),
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
private fun TrackCard(
	title: String,
	description: String,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.border(
				width = 2.dp,
				color = if (isSelected) Color(0xFF1976D2) else Color(0xFFE8EAED),
				shape = RoundedCornerShape(12.dp)
			)
			.background(
				color = if (isSelected) Color(0xFFF0F7FF) else Color.White,
				shape = RoundedCornerShape(12.dp)
			)
			.clickable { onClick() }
			.padding(16.dp)
	) {
		Column {
			Text(
				title,
				fontSize = 16.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF202124)
			)
			Text(
				description,
				fontSize = 13.sp,
				color = Color(0xFF6C757D),
				modifier = Modifier.padding(top = 6.dp)
			)
		}
	}
}
