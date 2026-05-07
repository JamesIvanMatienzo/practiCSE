package com.jigen.practicse.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.jigen.practicse.R

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
	LaunchedEffect(Unit) {
		onNavigateToLogin()
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFFF8F9FA)),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = painterResource(id = R.drawable.practicse_logo),
			contentDescription = "practiCSE Logo",
			modifier = Modifier.size(150.dp)
		)
	}
}
