package com.jigen.practicse.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
	var surname by remember { mutableStateOf("") }
	var firstName by remember { mutableStateOf("") }
	var middleName by remember { mutableStateOf("") }
	var age by remember { mutableStateOf("") }
	var school by remember { mutableStateOf("") }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Profile") },
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
			// Profile Photo Placeholder
			Spacer(modifier = Modifier.height(24.dp))

			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					"👤",
					fontSize = 64.sp
				)
				Text(
					"Upload photo",
					fontSize = 12.sp,
					color = Color(0xFF6C757D),
					modifier = Modifier.padding(top = 8.dp)
				)
			}

			Spacer(modifier = Modifier.height(32.dp))

			// Form Fields
			Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
				Text(
					"Surname",
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					color = Color(0xFF202124)
				)
				TextField(
					value = surname,
					onValueChange = { surname = it },
					placeholder = { Text("Enter surname") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(8.dp)
				)

				Text(
					"First Name",
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					color = Color(0xFF202124)
				)
				TextField(
					value = firstName,
					onValueChange = { firstName = it },
					placeholder = { Text("Enter first name") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(8.dp)
				)

				Text(
					"Middle Name",
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					color = Color(0xFF202124)
				)
				TextField(
					value = middleName,
					onValueChange = { middleName = it },
					placeholder = { Text("Enter middle name (optional)") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(8.dp)
				)

				Text(
					"Age",
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					color = Color(0xFF202124)
				)
				TextField(
					value = age,
					onValueChange = { age = it },
					placeholder = { Text("Enter age") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					shape = RoundedCornerShape(8.dp)
				)

				Text(
					"School",
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					color = Color(0xFF202124)
				)
				TextField(
					value = school,
					onValueChange = { school = it },
					placeholder = { Text("Enter your school or institution") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(8.dp)
				)
			}

			Spacer(modifier = Modifier.height(32.dp))

			Button(
				onClick = { /* Save profile */ },
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
				shape = RoundedCornerShape(28.dp)
			) {
				Text(
					"Save & Continue",
					fontSize = 14.sp,
					fontWeight = FontWeight.Bold,
					color = Color.White
				)
			}

			Text(
				"You can edit this later",
				fontSize = 12.sp,
				color = Color(0xFF6C757D),
				modifier = Modifier.padding(top = 12.dp)
			)
		}
	}
}
