package com.jigen.practicse.ui.screens.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jigen.practicse.R

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue  = Color(0xFF1A73E8)
private val SlateGray    = Color(0xFF5F6368)
private val TextColor    = Color(0xFF202124)
private val CardWhite    = Color(0xFFFFFFFF)
private val DividerColor = Color(0xFFE0E0E0)

@Composable
fun LoginScreen(
	onContinue: (email: String, password: String) -> String?,
	onSignUp: () -> Unit,
	onGuestContinue: () -> Unit = {}
) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var signInError by remember { mutableStateOf<String?>(null) }
	val canSignIn = email.isNotBlank() && password.isNotBlank()

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(SurfaceColor)
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
				Spacer(modifier = Modifier.height(48.dp))

				Image(
					painter = painterResource(id = R.drawable.practicse_logo),
					contentDescription = "practiCSE Logo",
					modifier = Modifier.size(120.dp)
				)

				Spacer(modifier = Modifier.height(24.dp))

				Text(
					"Your path to civil service success",
					style = MaterialTheme.typography.bodyMedium,
					color = SlateGray,
					modifier = Modifier.padding(top = 8.dp)
				)

				Spacer(modifier = Modifier.height(48.dp))

				OutlinedTextField(
					value = email,
					onValueChange = { email = it },
					modifier = Modifier.fillMaxWidth(),
					placeholder = { Text("Email", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
					singleLine = true,
					shape = RoundedCornerShape(14.dp)
				)

				Spacer(modifier = Modifier.height(14.dp))

				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					modifier = Modifier.fillMaxWidth(),
					placeholder = { Text("Password", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
					singleLine = true,
					shape = RoundedCornerShape(14.dp)
				)

				Spacer(modifier = Modifier.height(18.dp))

				Button(
					onClick = {
						signInError = onContinue(email, password)
					},
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = if (canSignIn) PrimaryBlue else Color(0xFFBDBDBD)
					),
					shape = RoundedCornerShape(14.dp)
					enabled = canSignIn
				) {
					Text(
						"Sign In",
						style = MaterialTheme.typography.bodyMedium.copy(
							fontWeight = FontWeight.SemiBold,
							fontSize = 16.sp
						),
						color = Color.White
					)
				}

				signInError?.let { error ->
					Spacer(modifier = Modifier.height(10.dp))
					Text(
						text = error,
						style = MaterialTheme.typography.bodySmall,
						color = Color(0xFFD32F2F),
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)
				}

				Spacer(modifier = Modifier.height(18.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
					Text(
						"or",
						modifier = Modifier.padding(horizontal = 12.dp),
						style = MaterialTheme.typography.bodySmall,
						color = SlateGray
					)
					HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
				}

				Spacer(modifier = Modifier.height(18.dp))

				Button(
					onClick = {
						signInError = "Google sign-in is not available yet. Use your email and password."
					},
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = CardWhite,
						contentColor = TextColor
					),
					shape = RoundedCornerShape(14.dp)
				) {
					Text(
						"Continue with Google",
						style = MaterialTheme.typography.bodyMedium.copy(
							fontWeight = FontWeight.Medium
						)
					)
				}
			}

			// ── Bottom section ────────────────────────────────────
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.padding(bottom = 12.dp)
			) {
				TextButton(onClick = onSignUp) {
					Text(
						"Don't have an account? Sign Up",
						style = MaterialTheme.typography.bodyMedium.copy(
							fontSize = 14.sp
						),
						color = PrimaryBlue,
						textAlign = TextAlign.Center
					)
				}

				TextButton(onClick = onGuestContinue) {
					Text(
						"Continue as Guest",
						style = MaterialTheme.typography.bodyMedium.copy(
							fontSize = 13.sp,
							fontWeight = FontWeight.Normal
						),
						color = SlateGray,
						textAlign = TextAlign.Center
					)
				}
			}
		}
	}
}