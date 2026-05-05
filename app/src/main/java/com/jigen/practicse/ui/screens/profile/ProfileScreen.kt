package com.jigen.practicse.ui.screens.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.UserProfileState
import androidx.compose.runtime.saveable.rememberSaveable

private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1976D2)
private val PrimaryBlueSoft = Color(0xFFEAF2FF)
private val TextColor = Color(0xFF202124)
private val MutedText = Color(0xFF6C757D)
private val BorderColor = Color(0xFFE6E8EC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit = {}) {
	val context = LocalContext.current
	val store = remember(context) { AppPreferencesStore(context) }
	val initialProfile = remember { store.loadProfile() }

	var firstName by rememberSaveable { mutableStateOf(initialProfile.firstName) }
	var middleName by rememberSaveable { mutableStateOf(initialProfile.middleName) }
	var surname by rememberSaveable { mutableStateOf(initialProfile.surname) }
	var age by rememberSaveable { mutableStateOf(initialProfile.age) }
	var school by rememberSaveable { mutableStateOf(initialProfile.school) }
	var photoUri by rememberSaveable { mutableStateOf(initialProfile.photoUri) }

	val imageBitmap = remember(photoUri) { loadBitmapFromUri(context, photoUri) }
	val pickPhotoLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
		photoUri = uri?.toString()
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Profile") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				},
				actions = {
					TextButton(onClick = {
						store.clearProfile()
						onLogout()
					}) {
						Text("Logout")
					}
				}
			)
		},
		containerColor = SurfaceColor
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(paddingValues)
				.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Card(
				modifier = Modifier.fillMaxWidth(),
				shape = RoundedCornerShape(20.dp),
				colors = CardDefaults.cardColors(containerColor = Color.White),
				elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
			) {
				Column(
					modifier = Modifier.padding(20.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Box(
						modifier = Modifier
							.size(96.dp)
							.clip(CircleShape)
							.background(PrimaryBlueSoft)
							.border(1.dp, BorderColor, CircleShape),
						contentAlignment = Alignment.Center
					) {
						if (imageBitmap != null) {
							Image(
								bitmap = imageBitmap,
								contentDescription = "Profile photo",
								modifier = Modifier.fillMaxSize(),
								contentScale = ContentScale.Crop
							)
						} else {
							Icon(
								Icons.Filled.AccountCircle,
								contentDescription = null,
								tint = PrimaryBlue,
								modifier = Modifier.size(56.dp)
							)
						}
					}

					Text(
						text = "${store.getActiveTrackLabel()}",
						fontWeight = FontWeight.SemiBold,
						color = PrimaryBlue
					)

					OutlinedButton(onClick = { pickPhotoLauncher.launch("image/*") }) {
						Text("Change photo")
					}
				}
			}

			ProfileField(label = "Surname", value = surname, onValueChange = { surname = it }, placeholder = "Enter surname")
			ProfileField(label = "First Name", value = firstName, onValueChange = { firstName = it }, placeholder = "Enter first name")
			ProfileField(label = "Middle Name", value = middleName, onValueChange = { middleName = it }, placeholder = "Enter middle name (optional)")
			ProfileField(label = "Age", value = age, onValueChange = { age = it.filter(Char::isDigit) }, placeholder = "Enter age")
			ProfileField(label = "School", value = school, onValueChange = { school = it }, placeholder = "Enter your school or institution")

			Button(
				onClick = {
					store.saveProfile(
						UserProfileState(
							firstName = firstName,
							middleName = middleName,
							surname = surname,
							age = age,
							school = school,
							photoUri = photoUri,
							activeTrack = store.getActiveTrackKey()
						)
					)
					onBack()
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
				shape = RoundedCornerShape(18.dp)
			) {
				Text("Save Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
			}

			Text(
				text = "Saved locally on this device.",
				color = MutedText,
				fontSize = 12.sp
			)
		}
	}
}

@Composable
private fun ProfileField(
	label: String,
	value: String,
	onValueChange: (String) -> Unit,
	placeholder: String
) {
	Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
		Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextColor)
		OutlinedTextField(
			value = value,
			onValueChange = onValueChange,
			placeholder = { Text(placeholder) },
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			shape = RoundedCornerShape(14.dp),
			colors = TextFieldDefaults.colors(
				focusedContainerColor = Color.White,
				unfocusedContainerColor = Color.White,
				focusedIndicatorColor = PrimaryBlue,
				unfocusedIndicatorColor = BorderColor,
				focusedTextColor = TextColor,
				unfocusedTextColor = TextColor
			)
		)
	}
}

private fun loadBitmapFromUri(context: android.content.Context, photoUri: String?): androidx.compose.ui.graphics.ImageBitmap? {
	if (photoUri.isNullOrBlank()) return null
	return runCatching {
		context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { inputStream ->
			BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
		}
	}.getOrNull()
}