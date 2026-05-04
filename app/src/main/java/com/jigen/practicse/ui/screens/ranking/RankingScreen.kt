package com.jigen.practicse.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context

private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
private val TextColor = Color(0xFF202124)

@Composable
fun RankingScreen(context: Context, onBack: () -> Unit = {}) {
    val vm: RankingViewModel = viewModel(factory = RankingViewModel.factory(context))
    val state by vm.uiState.collectAsState()

    Scaffold(containerColor = SurfaceColor) { padding ->
        when (val s = state) {
            is RankingUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Loading rankings...", color = TextColor)
                }
            }

            is RankingUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // User rank header
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Your Rank", fontSize = 14.sp, color = TextColor, fontWeight = FontWeight.Bold)
                            Text(
                                text = s.userRank?.let { "#${it}" } ?: "Unranked",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                            Text(text = s.userEntry?.totalScore?.toString() ?: "--", color = TextColor)
                        }
                    }

                    // Spacer
                    Row(modifier = Modifier.height(12.dp)) {}

                    // Leaderboard list
                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        itemsIndexed(s.top) { index, entry ->
                            LeaderboardRow(index + 1, entry.userName, entry.totalScore)
                        }
                    }
                }
            }

            is RankingUiState.Error -> {
                Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                    Text("Failed to load rankings", color = TextColor)
                    Text(s.message ?: "", color = TextColor)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, name: String, score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#$rank", fontWeight = FontWeight.Bold, color = TextColor, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, color = TextColor)
                Text("Score: $score", color = Color(0xFF6C757D), fontSize = 12.sp)
            }
        }
    }
}
