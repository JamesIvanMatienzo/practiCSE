package com.jigen.practicse.ui.screens.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun LoginScreen(onContinue: () -> Unit) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFFF8F9FA))
			.padding(24.dp)
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceBetween
		) {
			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(modifier = Modifier.height(72.dp))

				Text(
					"practiCSE",
					fontSize = 34.sp,
					fontWeight = FontWeight.Bold,
					color = Color(0xFF1976D2)
				)

				Text(
					"Your path to civil service success",
					fontSize = 14.sp,
					color = Color(0xFF5F6368),
					modifier = Modifier.padding(top = 8.dp)
				)

				Spacer(modifier = Modifier.height(48.dp))

				OutlinedTextField(
					value = email,
					onValueChange = { email = it },
					modifier = Modifier.fillMaxWidth(),
					placeholder = { Text("Email") },
					leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
					singleLine = true,
					shape = RoundedCornerShape(14.dp)
				)

				Spacer(modifier = Modifier.height(14.dp))

				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					modifier = Modifier.fillMaxWidth(),
					placeholder = { Text("Password") },
					leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
					singleLine = true,
					shape = RoundedCornerShape(14.dp)
				)

				Spacer(modifier = Modifier.height(18.dp))

				Button(
					onClick = onContinue,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
					shape = RoundedCornerShape(14.dp)
				) {
					Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
				}

				Spacer(modifier = Modifier.height(18.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
					Text(
						"or",
						modifier = Modifier.padding(horizontal = 12.dp),
						color = Color(0xFF5F6368),
						fontSize = 13.sp
					)
					HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
				}

				Spacer(modifier = Modifier.height(18.dp))

				Button(
					onClick = onContinue,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF202124)),
					shape = RoundedCornerShape(14.dp)
				) {
					Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
				}
			}

			Text(
				"Don't have an account? Sign Up",
				fontSize = 14.sp,
				color = Color(0xFF1976D2),
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(bottom = 12.dp)
			)
		}
	}
}