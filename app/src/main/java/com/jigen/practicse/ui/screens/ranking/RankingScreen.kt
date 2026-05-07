package com.jigen.practicse.ui.screens.ranking

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Switch
import com.jigen.practicse.data.local.AppPreferencesStore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity


private val SurfaceColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF1A73E8)
        private val PrimaryBlueDeep = Color(0xFF0D47A1)
        private val PrimaryBlueSoft = Color(0xFFEAF2FF)
        private val BorderColor = Color(0xFFDCE7FA)
        private val Gold = Color(0xFFF4B400)
        private val Silver = Color(0xFF9AA0A6)
        private val Bronze = Color(0xFFB06D3B)
        private val TextColor = Color(0xFF202124)
        private val MutedText = Color(0xFF6C757D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(context: Context, onBack: () -> Unit = {}) {
    val viewModel: RankingViewModel = viewModel(factory = RankingViewModel.factory(context))
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.jigen.practicse.R.drawable.ic_rank_leaderboard),
                            contentDescription = "Ranking",
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryBlue
                        )
                        Text("Leaderboard", fontWeight = FontWeight.Bold, color = TextColor)
                    }
                },
                actions = {
                        // offline toggle + badge
                        val prefs = AppPreferencesStore(context)
                        var offlineEnabled by remember { mutableStateOf(prefs.isOfflineRankingEnabled()) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (offlineEnabled) {
                                Box(
                                    modifier = Modifier
                                        .background(color = PrimaryBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("OFFLINE", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(if (offlineEnabled) "Offline" else "Online", color = MutedText, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = offlineEnabled, onCheckedChange = {
                                offlineEnabled = it
                                prefs.setOfflineRankingEnabled(it)
                                // refresh rankings
                                viewModel.refresh()
                            })
                        }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        when (val s = state) {
            is RankingUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is RankingUiState.Success -> {
                val top3 = s.top.take(3)
                val others = s.top.drop(3)
                val totalEntries = s.top.size

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderColor, RoundedCornerShape(22.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(PrimaryBlue, PrimaryBlueDeep))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.jigen.practicse.R.drawable.ic_rank_leaderboard),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${totalEntries} ranked players",
                                        color = TextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (s.isPlaceholder) {
                                            "Offline sample data is shown here."
                                        } else {
                                            "Updated from the latest leaderboard sync."
                                        },
                                        color = MutedText,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (s.isPlaceholder) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    text = "Offline mode: showing sample rankings",
                                    color = PrimaryBlue,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Your Rank", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = s.userRank?.let { "#$it" } ?: "Unranked",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = s.userEntry?.let { "${displayName(it.userName)} • ${it.totalScore}" } ?: "No score yet",
                                    color = TextColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (top3.isNotEmpty()) {
                        item {
                            Text(
                                text = "Top 3",
                                color = TextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                top3.forEachIndexed { index, entry ->
                                    PodiumItem(
                                        rank = index + 1,
                                        entry = entry,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Leaderboard",
                            color = TextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    itemsIndexed(others) { index, entry ->
                        LeaderboardRow(rank = index + 4, name = entry.userName, score = entry.totalScore)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (others.isEmpty() && top3.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "More rankings will appear as players submit scores.",
                                fontSize = 12.sp,
                                color = MutedText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is RankingUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load rankings: ${s.message}", color = TextColor)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, name: String, score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color = PrimaryBlue.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(rank.toString(), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))

            // avatar (use provided drawables for known sample users)
            val avatarRes = when (name.lowercase()) {
                "emman" -> com.jigen.practicse.R.drawable.emman
                "ivan" -> com.jigen.practicse.R.drawable.ivan
                "gem" -> com.jigen.practicse.R.drawable.gem
                "james" -> com.jigen.practicse.R.drawable.james
                else -> 0
            }

            if (avatarRes != 0) {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = "avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = PrimaryBlue.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials(name), fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName(name), fontWeight = FontWeight.SemiBold, color = TextColor)
                Text("Score: $score", color = MutedText, fontSize = 12.sp)
            }
            Text(score.toString(), color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PodiumItem(rank: Int, entry: LeaderboardEntryEntity, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (rank == 1) 64.dp else 54.dp)
                .background(color = PrimaryBlue.copy(alpha = 0.18f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Show avatar image for known sample users
            val avatar = when (entry.userName.lowercase()) {
                "emman" -> com.jigen.practicse.R.drawable.emman
                "ivan" -> com.jigen.practicse.R.drawable.ivan
                "gem" -> com.jigen.practicse.R.drawable.gem
                "james" -> com.jigen.practicse.R.drawable.james
                else -> 0
            }

            if (avatar != 0) {
                Image(
                    painter = painterResource(id = avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (rank == 1) 56.dp else 46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials(entry.userName),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "#$rank ${displayName(entry.userName)}",
            color = TextColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = entry.totalScore.toString(),
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

private fun displayName(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        .ifBlank { name }
}

private fun initials(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "U" }
}
