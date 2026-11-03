package com.tanatos.symphonymusic2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassButton
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.tanatos.symphonymusic2.model.LyricLine
import com.tanatos.symphonymusic2.model.SyncedLyrics
import com.tanatos.symphonymusic2.model.Track

@Composable
fun SymphonyFullPlayer(
    currentTrack: Track,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isLiked: Boolean,
    isShuffle: Boolean,
    repeatMode: Int, // 0 = off, 1 = all, 2 = one
    queue: List<Track>,
    activeColorStart: Color,
    activeColorEnd: Color,
    syncedLyrics: SyncedLyrics?,
    currentLyricIndex: Int,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSelectQueueTrack: (Track) -> Unit,
    onCollapse: () -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    var showLyrics by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }

    val safeDuration = durationMs.coerceAtLeast(1000L)
    val progress = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val currentSeconds = currentPositionMs / 1000L
    val totalSeconds = safeDuration / 1000L
    val currentMinutes = currentSeconds / 60
    val currentSecs = currentSeconds % 60
    val totalMinutes = totalSeconds / 60
    val totalSecs = totalSeconds % 60
    val timeElapsed = String.format("%02d:%02d", currentMinutes, currentSecs)
    val timeTotal = String.format("%02d:%02d", totalMinutes, totalSecs)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F).copy(alpha = 0.94f))
            .statusBarsPadding()
    ) {
        // Living Dynamic Material You Ambient Glow behind the album art
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .size(330.dp)
                .blur(85.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeColorStart.copy(alpha = 0.70f),
                            activeColorEnd.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Collapse Chevron Button
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    isCircle = true,
                    cornerRadius = 20.dp,
                    chromaticColor = activeColorStart,
                    highlightIntensity = 1.0f,
                    onClick = onCollapse,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Header Center Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SYMPHONY 2.0 HI-FI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GeistFontFamily,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = currentTrack.album.ifEmpty { "Playing from YouTube Music" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GeistFontFamily,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Audio FX / Graphic Eq Button
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    isCircle = true,
                    cornerRadius = 20.dp,
                    chromaticColor = activeColorStart,
                    highlightIntensity = 1.0f,
                    onClick = { /* Audio FX */ },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Equalizer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Middle Hero Display (Album Art OR Lyrics OR Queue)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    showLyrics -> {
                        LyricsPanel(
                            syncedLyrics = syncedLyrics,
                            currentLyricIndex = currentLyricIndex,
                            backdrop = backdrop,
                            accentColor = activeColorStart
                        )
                    }
                    showQueue -> {
                        QueuePanel(
                            queue = queue,
                            currentTrackId = currentTrack.id,
                            onSelect = onSelectQueueTrack,
                            backdrop = backdrop,
                            activeColor = activeColorStart
                        )
                    }
                    else -> {
                        // Large 3D Glass Hero Album Cover
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .shadow(
                                    elevation = 32.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    spotColor = activeColorStart
                                )
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(activeColorStart, activeColorEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentTrack.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = currentTrack.thumbnailUrl,
                                    contentDescription = currentTrack.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "♪",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 120.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Glass Sheen Reflection Arc
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.22f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.35f)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Track Info + Like Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GeistFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentTrack.artist,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GeistFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Like Button
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF3366) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Lossless Audio Badge + Mode Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Hi-Res Master • 24-bit / 96kHz FLAC",
                        color = activeColorStart,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GeistFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Lyrics Drawer Toggle
                    IconButton(
                        onClick = {
                            showLyrics = !showLyrics
                            if (showLyrics) showQueue = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = "Lyrics",
                            tint = if (showLyrics) activeColorStart else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Queue Drawer Toggle
                    IconButton(
                        onClick = {
                            showQueue = !showQueue
                            if (showQueue) showLyrics = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (showQueue) activeColorStart else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Glass Time Scrubber Slider
            PrismalGlassSlider(
                value = { progress },
                onValueChange = { newProgress ->
                    onSeek((newProgress * safeDuration).toLong())
                },
                valueRange = 0f..1f,
                visibilityThreshold = 0.005f,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )

            // Timestamps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeElapsed,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = timeTotal,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Playback Control Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) activeColorStart else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // HERO 3D Liquid Glass Play/Pause Pebble Jewel (72dp)
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    isCircle = true,
                    cornerRadius = 35.dp,
                    chromaticColor = activeColorStart,
                    highlightIntensity = 1.35f,
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(72.dp)
                        .padding(4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (repeatMode == 2) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode > 0) activeColorStart else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LyricsPanel(
    syncedLyrics: SyncedLyrics?,
    currentLyricIndex: Int,
    backdrop: PrismalBackdrop,
    accentColor: Color
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentLyricIndex) {
        if (currentLyricIndex in 0 until (syncedLyrics?.lines?.size ?: 0)) {
            listState.animateScrollToItem((currentLyricIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(16.dp)
    ) {
        if (syncedLyrics == null || syncedLyrics.lines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No synchronized lyrics available for this track\nFetching from LRCLIB...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = GeistFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(syncedLyrics.lines) { index, line ->
                    val isCurrent = index == currentLyricIndex
                    Text(
                        text = line.text,
                        color = if (isCurrent) accentColor else Color.White.copy(alpha = 0.45f),
                        fontSize = if (isCurrent) 22.sp else 16.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = GeistFontFamily,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QueuePanel(
    queue: List<Track>,
    currentTrackId: String,
    onSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "UP NEXT IN SYMPHONY 2.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GeistFontFamily,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue) { _, track ->
                    val isCurrent = track.id == currentTrackId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) activeColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
                            .clickable { onSelect(track) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(track.colorStart, track.colorEnd))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (track.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = track.thumbnailUrl,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("♪", color = Color.White, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = if (isCurrent) activeColor else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = GeistFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontFamily = GeistFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isCurrent) {
                            Text(
                                text = "PLAYING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
