package com.jigen.practicse.ui.screens.ranking

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val SurfaceColor    = Color(0xFFF8F9FA)
private val PrimaryBlue     = Color(0xFF1A73E8)
private val PrimaryBlueDeep = Color(0xFF0D47A1)
private val PrimaryBlueSoft = Color(0xFFEAF2FF)
private val BorderColor     = Color(0xFFDCE7FA)
private val Gold            = Color(0xFFF4B400)
private val Silver          = Color(0xFF9AA0A6)
private val Bronze          = Color(0xFFB06D3B)
private val TextColor       = Color(0xFF202124)
private val MutedText       = Color(0xFF6C757D)
private val CardWhite       = Color(0xFFFFFFFF)
private val OnlineGreen     = Color(0xFF34A853)

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(context: Context, onBack: () -> Unit = {}) {
    val viewModel: RankingViewModel = viewModel(factory = RankingViewModel.factory(context))
    val state by viewModel.uiState.collectAsState()

    val prefs = AppPreferencesStore(context)
    // offlineEnabled = true  → showing offline/cached data
    // switch ON (checked)    → Online mode  → offlineEnabled = false
    // switch OFF (unchecked) → Offline mode → offlineEnabled = true
    var offlineEnabled by remember { mutableStateOf(prefs.isOfflineRankingEnabled()) }
    val isOnline = !offlineEnabled

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
                            painter = painterResource(id = com.jigen.practicse.R.drawable.ic_podium),
                            contentDescription = "Ranking",
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryBlue
                        )
                        Text(
                            "Leaderboard",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            color = TextColor
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            color = if (isOnline) OnlineGreen else MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isOnline,   // ON = Online
                            onCheckedChange = { nowOnline ->
                                offlineEnabled = !nowOnline
                                prefs.setOfflineRankingEnabled(!nowOnline)
                                viewModel.refresh()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor   = OnlineGreen,
                                checkedThumbColor   = CardWhite,
                                uncheckedTrackColor = MutedText.copy(alpha = 0.4f),
                                uncheckedThumbColor = CardWhite
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
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
                val top3         = s.top.take(3)
                val others       = s.top.drop(3)
                val totalEntries = s.top.size

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    // ── Header banner ────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite)
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = com.jigen.practicse.R.drawable.ic_podium),
                                            contentDescription = null,
                                            tint = CardWhite,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "$totalEntries Ranked Players",
                                            color = TextColor,
                                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            if (s.isPlaceholder) "Live leaderboard"
                                            else "Local device scores",
                                            color = MutedText,
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryBlue, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (s.isPlaceholder)
                                            "Online mode — showing live rankings"
                                        else
                                            "Offline mode — showing local scores",
                                        color = CardWhite,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    // ── TOP 3 Podium section ──────────────────────────────────
                    if (top3.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardWhite)
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 0.dp)
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "TOP 3",
                                    color = MutedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(bottom = 18.dp)
                                )

                                // Avatar row: [2nd] [1st raised] [3rd]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (top3.size >= 2) {
                                        PodiumItem(
                                            rank = 2,
                                            entry = top3[1],
                                            medalColor = Silver,
                                            avatarSize = 60.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else Spacer(modifier = Modifier.weight(1f))

                                    if (top3.isNotEmpty()) {
                                        PodiumItem(
                                            rank = 1,
                                            entry = top3[0],
                                            medalColor = Gold,
                                            avatarSize = 76.dp,
                                            isFirst = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else Spacer(modifier = Modifier.weight(1f))

                                    if (top3.size >= 3) {
                                        PodiumItem(
                                            rank = 3,
                                            entry = top3[2],
                                            medalColor = Bronze,
                                            avatarSize = 52.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else Spacer(modifier = Modifier.weight(1f))
                                }

                                // Podium step blocks
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(Silver.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = CardWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(Gold.copy(alpha = 0.85f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = CardWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(Bronze.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("3", color = CardWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ── Leaderboard list header ───────────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Leaderboard",
                                color = TextColor,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${s.top.size} players",
                                color = MutedText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // ── Ranks 4+ rows ─────────────────────────────────────────
                    if (others.isEmpty() && top3.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Take a quiz to see your offline rank",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = MutedText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (others.isEmpty() && top3.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "More rankings will appear as players submit scores.",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = MutedText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    itemsIndexed(others) { index, entry ->
                        LeaderboardRow(
                            rank  = index + 4,
                            entry = entry,
                            isEven = index % 2 == 0
                        )
                    }

                    // ── Your Rank card (bottom) ───────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val userEntry = s.userEntry
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(6.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(listOf(PrimaryBlueSoft, CardWhite))
                                    )
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Your Rank",
                                        color = MutedText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = s.userRank?.let { "#$it" } ?: "Unranked",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = userEntry?.let {
                                            "${displayName(it.userName)} • Score: ${it.totalScore}"
                                        } ?: "Complete a quiz to appear here.",
                                        color = MutedText,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
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

// ── Leaderboard row ───────────────────────────────────────────────────────────
@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntryEntity, isEven: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isEven) CardWhite else SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(rank.toString(), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))

        // Try to display photo from entry first
        val photoBitmap = decodePhotoFromBase64(entry.photoBase64)
        val avatarRes = when (entry.userName.lowercase()) {
            "emman", "emmanuel" -> com.jigen.practicse.R.drawable.emman
            "ivan"  -> com.jigen.practicse.R.drawable.ivan
            "gem"   -> com.jigen.practicse.R.drawable.gem
            "james" -> com.jigen.practicse.R.drawable.james
            else    -> 0
        }

        when {
            photoBitmap != null -> {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = "avatar",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(2.dp, BorderColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            avatarRes != 0 -> {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = "avatar",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(2.dp, BorderColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Fall back to initials if no photo or hardcoded avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlueSoft)
                        .border(2.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials(entry.userName), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(displayName(entry.userName), fontWeight = FontWeight.SemiBold, color = TextColor, fontSize = 14.sp)
            Text("Score: ${entry.totalScore}", color = MutedText, fontSize = 12.sp)
        }
        Text(entry.totalScore.toString(), color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
    }
    Divider(color = BorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)
}

// ── Podium avatar item ────────────────────────────────────────────────────────
@Composable
private fun PodiumItem(
    rank: Int,
    entry: LeaderboardEntryEntity,
    medalColor: Color,
    avatarSize: Dp,
    isFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            // Avatar with medal-coloured ring
            Box(
                modifier = Modifier
                    .size(avatarSize + 6.dp)
                    .clip(CircleShape)
                    .background(medalColor.copy(alpha = 0.2f))
                    .border(3.dp, medalColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val photoBitmap = decodePhotoFromBase64(entry.photoBase64)
                val avatar = when (entry.userName.lowercase()) {
                    "emman", "emmanuel" -> com.jigen.practicse.R.drawable.emman
                    "ivan"  -> com.jigen.practicse.R.drawable.ivan
                    "gem"   -> com.jigen.practicse.R.drawable.gem
                    "james" -> com.jigen.practicse.R.drawable.james
                    else    -> 0
                }
                when {
                    photoBitmap != null -> {
                        Image(
                            bitmap = photoBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    avatar != 0 -> {
                        Image(
                            painter = painterResource(id = avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Text(
                            initials(entry.userName),
                            color = medalColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isFirst) 22.sp else 16.sp
                        )
                    }
                }
            }
            // Medal number badge — sits centered at the very bottom of the avatar circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(medalColor)
                    .border(2.dp, CardWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rank.toString(),
                    color = CardWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            displayName(entry.userName),
            color = TextColor,
            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = if (isFirst) 13.sp else 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            entry.totalScore.toString(),
            color = medalColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun displayName(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        .ifBlank { name }

private fun initials(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "U" }

private fun decodePhotoFromBase64(photoBase64: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        if (photoBase64.isNullOrBlank()) {
            null
        } else {
            val decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap?.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}