package com.jigen.practicse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
	primary = PrimaryBlue,
	primaryContainer = PrimaryBlueLight,
	secondary = WarningOrange,
	tertiary = SuccessGreen,
	background = BackgroundColor,
	surface = SurfaceColor,
	error = ErrorColor,
	onPrimary = Color.White,
	onSecondary = Color.White,
	onTertiary = Color.White,
	onBackground = TextPrimary,
	onSurface = TextPrimary,
	onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
	primary = PrimaryBlueLight,
	primaryContainer = PrimaryBlueDark,
	secondary = WarningOrange,
	tertiary = SuccessGreen,
	background = DarkBackgroundColor,
	surface = DarkSurfaceColor,
	error = ErrorColor,
	onPrimary = Color.Black,
	onSecondary = Color.Black,
	onTertiary = Color.White,
	onBackground = DarkTextPrimary,
	onSurface = DarkTextPrimary,
	onError = Color.Black
)

@Composable
fun PractiCSETheme(
	darkTheme: Boolean = false,
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

	MaterialTheme(
		colorScheme = colorScheme,
		typography = PractiCSETypography,
		content = content
	)
}
