package com.tanatos.symphonymusic2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styropyr0.prismal.components.LocalPrismalBottomTabHighlightedIndex
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource
import com.tanatos.symphonymusic2.engine.SymphonyPlaybackManager
import com.tanatos.symphonymusic2.model.SymphonyCatalog

enum class SymphonyTab {
    Home,
    Search,
    Library,
    GlassLab
}

@Composable
fun SymphonyApp(playbackManager: SymphonyPlaybackManager) {
    // Backdrop layers for live AGSL sampling
    val backdropLayer = rememberPrismalGlassLayer()
    val screenLayer = rememberPrismalGlassLayer()
    val modalBackdrop = rememberPrismalMergedSource(backdropLayer, screenLayer)

    // Playback StateFlows from SymphonyPlaybackManager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val isLoading by playbackManager.isLoading.collectAsState()
    val playbackPositionMs by playbackManager.playbackPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val queue by playbackManager.queue.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()
    val likedIds by playbackManager.likedIds.collectAsState()
    val activeColors by playbackManager.activeColors.collectAsState()
    val syncedLyrics by playbackManager.syncedLyrics.collectAsState()
    val currentLyricIndex by playbackManager.currentLyricIndex.collectAsState()

    // UI Navigation State
    var selectedTab by remember { mutableIntStateOf(SymphonyTab.Home.ordinal) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }

    // Glass Lab Tuning Parameters
    var blurRadiusDp by remember { mutableFloatStateOf(16f) }
    var refractionHeightDp by remember { mutableFloatStateOf(18f) }
    var refractionAmountDp by remember { mutableFloatStateOf(32f) }
    var specularIntensity by remember { mutableFloatStateOf(1.15f) }

    val safeDuration = durationMs.coerceAtLeast(1000L)
    val progress = (playbackPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF07080C)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ==========================================
            // LAYER 0: LIVING DYNAMIC AMBIENT BACKDROP
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .prismalGlassLayer(backdropLayer)
            ) {
                // Primary Deep Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07080C))
                )

                // Living Dynamic Glow Blob 1 (Top Left)
                Box(
                    modifier = Modifier
                        .size(340.dp)
                        .align(Alignment.TopStart)
                        .blur(100.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeColors.first.copy(alpha = 0.40f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Living Dynamic Glow Blob 2 (Center Right)
                Box(
                    modifier = Modifier
                        .size(360.dp)
                        .align(Alignment.CenterEnd)
                        .blur(110.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeColors.second.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Living Dynamic Glow Blob 3 (Bottom Center)
                Box(
                    modifier = Modifier
                        .size(380.dp)
                        .align(Alignment.BottomCenter)
                        .blur(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeColors.first.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // ==========================================
            // LAYER 1: CURRENT ACTIVE SCREEN
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .prismalGlassLayer(screenLayer)
            ) {
                when (selectedTab) {
                    SymphonyTab.Home.ordinal -> {
                        SymphonyHomeScreen(
                            tracks = queue,
                            playlists = SymphonyCatalog.samplePlaylists,
                            artists = SymphonyCatalog.sampleArtists,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            activeColorStart = activeColors.first,
                            onTrackSelect = { track ->
                                playbackManager.playTrack(track)
                            },
                            backdrop = backdropLayer
                        )
                    }
                    SymphonyTab.Search.ordinal -> {
                        SymphonySearchScreen(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            activeColorStart = activeColors.first,
                            onTrackSelect = { track ->
                                playbackManager.playTrack(track)
                            },
                            backdrop = backdropLayer
                        )
                    }
                    SymphonyTab.Library.ordinal -> {
                        val likedTracks = queue.filter { likedIds.contains(it.id) }
                        SymphonyLibraryScreen(
                            likedTracks = likedTracks,
                            playlists = SymphonyCatalog.samplePlaylists,
                            artists = SymphonyCatalog.sampleArtists,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            activeColorStart = activeColors.first,
                            onTrackSelect = { track ->
                                playbackManager.playTrack(track)
                            },
                            backdrop = backdropLayer
                        )
                    }
                    SymphonyTab.GlassLab.ordinal -> {
                        SymphonyGlassLabScreen(
                            blurRadius = blurRadiusDp,
                            refractionHeight = refractionHeightDp,
                            refractionAmount = refractionAmountDp,
                            specularIntensity = specularIntensity,
                            onBlurRadiusChange = { blurRadiusDp = it },
                            onRefractionHeightChange = { refractionHeightDp = it },
                            onRefractionAmountChange = { refractionAmountDp = it },
                            onSpecularIntensityChange = { specularIntensity = it },
                            activeColorStart = activeColors.first,
                            backdrop = backdropLayer
                        )
                    }
                }
            }

            // ==========================================
            // LAYER 2: FLOATING DOCK & FLOATING MINIPLAYER
            // ==========================================
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Floating Mini Player (Shown when Full Player is closed)
                if (!isFullPlayerOpen) {
                    SymphonyMiniPlayer(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        progress = progress,
                        isLiked = likedIds.contains(currentTrack.id),
                        activeColorStart = activeColors.first,
                        activeColorEnd = activeColors.second,
                        onTogglePlayPause = { playbackManager.togglePlayPause() },
                        onPrevious = { playbackManager.skipPrevious() },
                        onNext = { playbackManager.skipNext() },
                        onToggleLike = { playbackManager.toggleLike(currentTrack.id) },
                        onExpand = { isFullPlayerOpen = true },
                        backdrop = modalBackdrop
                    )
                }

                // Floating 3D Liquid Glass Navigation Dock
                PrismalGlassBottomTabs(
                    selectedTabIndex = { selectedTab },
                    onTabSelected = { selectedTab = it },
                    backdrop = modalBackdrop,
                    tabsCount = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    SymphonyNavTab(
                        title = "Home",
                        icon = Icons.Rounded.Home,
                        index = SymphonyTab.Home.ordinal,
                        onClick = { selectedTab = SymphonyTab.Home.ordinal }
                    )
                    SymphonyNavTab(
                        title = "Search",
                        icon = Icons.Rounded.Search,
                        index = SymphonyTab.Search.ordinal,
                        onClick = { selectedTab = SymphonyTab.Search.ordinal }
                    )
                    SymphonyNavTab(
                        title = "Library",
                        icon = Icons.Rounded.LibraryMusic,
                        index = SymphonyTab.Library.ordinal,
                        onClick = { selectedTab = SymphonyTab.Library.ordinal }
                    )
                    SymphonyNavTab(
                        title = "Glass Lab",
                        icon = Icons.Rounded.Tune,
                        index = SymphonyTab.GlassLab.ordinal,
                        onClick = { selectedTab = SymphonyTab.GlassLab.ordinal }
                    )
                }
            }

            // ==========================================
            // LAYER 3: FULL SCREEN HI-FI PLAYER DIALOG
            // ==========================================
            AnimatedVisibility(
                visible = isFullPlayerOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                SymphonyFullPlayer(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    currentPositionMs = playbackPositionMs,
                    durationMs = durationMs,
                    isLiked = likedIds.contains(currentTrack.id),
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    queue = queue,
                    activeColorStart = activeColors.first,
                    activeColorEnd = activeColors.second,
                    syncedLyrics = syncedLyrics,
                    currentLyricIndex = currentLyricIndex,
                    onTogglePlayPause = { playbackManager.togglePlayPause() },
                    onSeek = { newPositionMs -> playbackManager.seekTo(newPositionMs) },
                    onPrevious = { playbackManager.skipPrevious() },
                    onNext = { playbackManager.skipNext() },
                    onToggleLike = { playbackManager.toggleLike(currentTrack.id) },
                    onToggleShuffle = { playbackManager.toggleShuffle() },
                    onToggleRepeat = { playbackManager.toggleRepeat() },
                    onSelectQueueTrack = { track -> playbackManager.playTrack(track) },
                    onCollapse = { isFullPlayerOpen = false },
                    backdrop = backdropLayer
                )
            }
        }
    }
}

@Composable
private fun RowScope.SymphonyNavTab(
    title: String,
    icon: ImageVector,
    index: Int,
    onClick: () -> Unit
) {
    val highlightedIndexProvider = LocalPrismalBottomTabHighlightedIndex.current
    val isHighlighted = highlightedIndexProvider() == index
    PrismalGlassBottomTab(
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontFamily = GeistFontFamily,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}
