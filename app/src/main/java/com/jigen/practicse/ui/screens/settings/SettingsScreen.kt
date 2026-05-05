package com.jigen.practicse.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
	var fontSize by remember { mutableStateOf("Medium") }
	var darkMode by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Settings") },
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
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			// Font Size Setting
			SettingItem(
				label = "Font Size",
				value = fontSize,
				onClick = { /* Show font size options */ }
			)

			// Dark Mode Setting (Coming Soon)
			SettingItem(
				label = "Dark Mode",
				value = "Coming Soon",
				onClick = { /* Future: enable dark mode */ },
				enabled = false
			)
		}
	}
}

@Composable
private fun SettingItem(
	label: String,
	value: String,
	onClick: () -> Unit,
	enabled: Boolean = true
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.White, RoundedCornerShape(8.dp))
			.clickable(enabled = enabled) { onClick() }
			.padding(16.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			label,
			fontSize = 14.sp,
			fontWeight = FontWeight.SemiBold,
			color = Color(0xFF202124)
		)

		Text(
			value,
			fontSize = 13.sp,
			color = if (enabled) Color(0xFF1976D2) else Color(0xFF9CA3AF),
			fontWeight = FontWeight.Medium
		)
	}
}
