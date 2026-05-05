package com.jigen.practicse.ui.screens.login

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.UserProfileState

@Composable
fun SignUpScreen(context: Context, onBack: () -> Unit, onSignUpComplete: () -> Unit) {
	var fullName by remember { mutableStateOf("") }
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var confirmPassword by remember { mutableStateOf("") }
	var school by remember { mutableStateOf("") }
	var age by remember { mutableStateOf("") }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFFF8F9FA))
			.padding(24.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Back button
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(48.dp),
				contentAlignment = Alignment.CenterStart
			) {
				IconButton(onClick = onBack) {
					Icon(
						Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = "Back",
						tint = Color(0xFF1976D2)
					)
				}
			}

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				"Create Account",
				fontSize = 28.sp,
				fontWeight = FontWeight.Bold,
				color = Color(0xFF202124)
			)

			Text(
				"Join practiCSE to start your exam preparation",
				fontSize = 14.sp,
				color = Color(0xFF5F6368),
				modifier = Modifier.padding(top = 8.dp)
			)

			Spacer(modifier = Modifier.height(32.dp))

			// Full Name
			OutlinedTextField(
				value = fullName,
				onValueChange = { fullName = it },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("Full Name") },
				singleLine = true,
				shape = RoundedCornerShape(14.dp)
			)

			Spacer(modifier = Modifier.height(14.dp))

			// School
			OutlinedTextField(
				value = school,
				onValueChange = { school = it },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("School or Institution") },
				singleLine = true,
				shape = RoundedCornerShape(14.dp)
			)

			Spacer(modifier = Modifier.height(14.dp))

			// Age
			OutlinedTextField(
				value = age,
				onValueChange = { age = it.filter(Char::isDigit) },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("Age") },
				singleLine = true,
				shape = RoundedCornerShape(14.dp)
			)

			Spacer(modifier = Modifier.height(14.dp))

			// Email
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

			// Password
			OutlinedTextField(
				value = password,
				onValueChange = { password = it },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("Password") },
				leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
				singleLine = true,
				shape = RoundedCornerShape(14.dp),
				visualTransformation = PasswordVisualTransformation()
			)

			Spacer(modifier = Modifier.height(14.dp))

			// Confirm Password
			OutlinedTextField(
				value = confirmPassword,
				onValueChange = { confirmPassword = it },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("Confirm Password") },
				leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
				singleLine = true,
				shape = RoundedCornerShape(14.dp),
				visualTransformation = PasswordVisualTransformation()
			)

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				onClick = {
					if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword) {
						// Parse full name into parts
						val nameParts = fullName.trim().split("\\s+".toRegex())
						val firstName = nameParts.getOrNull(0) ?: ""
						val lastName = nameParts.getOrNull(nameParts.size - 1) ?: ""
						val middleName = if (nameParts.size > 2) {
							nameParts.subList(1, nameParts.size - 1).joinToString(" ")
						} else {
							""
						}
						val surname = if (nameParts.size > 1) lastName else ""

						// Save profile
						val store = AppPreferencesStore(context)
						store.saveProfile(
							UserProfileState(
								firstName = firstName,
								middleName = middleName,
								surname = surname,
								age = age,
								school = school,
								photoUri = null
							)
						)

						onSignUpComplete()
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword && school.isNotBlank()) {
						Color(0xFF1976D2)
					} else {
						Color(0xFFBDBDBD)
					}
				),
				shape = RoundedCornerShape(14.dp),
				enabled = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword && school.isNotBlank()
			) {
				Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
			}

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				"Already have an account? Sign In",
				fontSize = 14.sp,
				color = Color(0xFF1976D2),
				modifier = Modifier.padding(top = 8.dp)
			)

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}
