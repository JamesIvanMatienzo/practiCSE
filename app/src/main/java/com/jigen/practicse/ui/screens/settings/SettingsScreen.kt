package com.jigen.practicse.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.jigen.practicse.data.local.ExamConfigStore

private val SurfaceColor = Color(0xFFF8F9FA)
private val TextColor = Color(0xFF202124)
private val PrimaryBlue = Color(0xFF1976D2)
private val MutedGray = Color(0xFF6C757D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
context: Context,
onBack: () -> Unit
) {
val configStore = remember { ExamConfigStore(context) }
val config = remember { configStore.getConfig() }

var numericalCount by remember { mutableStateOf(config.numericalCount) }
var verbalCount by remember { mutableStateOf(config.verbalCount) }
var generalCount by remember { mutableStateOf(config.generalCount) }

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
containerColor = SurfaceColor
) { paddingValues ->
Column(
modifier = Modifier
.fillMaxSize()
.verticalScroll(rememberScrollState())
.padding(paddingValues)
.padding(16.dp),
verticalArrangement = Arrangement.spacedBy(14.dp)
) {
Text(
text = "Mock Exam Question Distribution",
fontSize = 16.sp,
fontWeight = FontWeight.Bold,
color = TextColor
)
Text(
text = "These values control how many questions are used in All Exam and Study Library per category.",
fontSize = 12.sp,
color = MutedGray
)

CountSettingRow(
label = "Numerical Ability",
selected = numericalCount,
onSelected = {
numericalCount = it
configStore.setNumericalCount(it)
}
)

CountSettingRow(
label = "Verbal Ability",
selected = verbalCount,
onSelected = {
verbalCount = it
configStore.setVerbalCount(it)
}
)

CountSettingRow(
label = "General Information",
selected = generalCount,
onSelected = {
generalCount = it
configStore.setGeneralCount(it)
}
)

val totalAll = numericalCount + verbalCount + generalCount
Text(
text = "Total questions for All Exam: $totalAll",
fontSize = 13.sp,
fontWeight = FontWeight.SemiBold,
color = PrimaryBlue
)

Spacer(modifier = Modifier.height(8.dp))

Button(
onClick = onBack,
modifier = Modifier.fillMaxWidth().height(52.dp),
colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
shape = RoundedCornerShape(14.dp)
) {
Text("Done")
}
}
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountSettingRow(
label: String,
selected: Int,
onSelected: (Int) -> Unit
) {
val options = listOf(5, 10, 15, 20, 25, 30)
var expanded by remember { mutableStateOf(false) }

Row(
modifier = Modifier
.fillMaxWidth()
.background(Color.White, RoundedCornerShape(10.dp))
.padding(12.dp),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Text(text = label, color = TextColor, fontWeight = FontWeight.SemiBold)

ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
OutlinedTextField(
value = selected.toString(),
onValueChange = {},
readOnly = true,
modifier = Modifier.menuAnchor().width(100.dp),
trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
)
ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
options.forEach { count ->
DropdownMenuItem(
text = { Text(count.toString()) },
onClick = {
onSelected(count)
expanded = false
}
)
}
}
}
}
}