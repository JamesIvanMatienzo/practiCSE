package com.jigen.practicse.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("About") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		},
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
				"v1.0.0",
				fontSize = 14.sp,
				color = Color(0xFF6C757D),
				modifier = Modifier.padding(top = 8.dp)
			)

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				"About This App",
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF202124)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				"A distraction-free, offline-first Civil Service Exam Reviewer designed to help students prepare effectively without distractions.\n\n" +
						"Features:\n" +
						"• Comprehensive question bank\n" +
						"• Offline support\n" +
						"• Progress tracking\n" +
						"• AI-powered explanations\n" +
						"• Global leaderboard\n\n" +
						"Built with passion for your success.",
				fontSize = 13.sp,
				color = Color(0xFF202124),
				textAlign = TextAlign.Center,
				lineHeight = 20.sp,
				modifier = Modifier.padding(16.dp)
			)

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				"© 2026 All rights reserved",
				fontSize = 11.sp,
				color = Color(0xFF9CA3AF),
				textAlign = TextAlign.Center
			)
		}
	}
}
