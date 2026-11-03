package com.tanatos.symphonymusic2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalSegmentedControl
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.tanatos.symphonymusic2.engine.SymphonyDownloader
import com.tanatos.symphonymusic2.engine.SymphonyMusicEngine
import com.tanatos.symphonymusic2.engine.SymphonyUpdater
import com.tanatos.symphonymusic2.model.Artist
import com.tanatos.symphonymusic2.model.Playlist
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.launch

// ==========================================
// 1. HOME SCREEN (Symphony Discovery)
// ==========================================
@Composable
fun SymphonyHomeScreen(
    vibeTracks: List<Track>,
    quickPicks: List<Track>,
    playlists: List<Playlist>,
    artists: List<Artist>,
    currentTrack: Track,
    isPlaying: Boolean,
    activeColorStart: Color,
    onTrackSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYMPHONY 2.0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GeistFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = "Hi-Fi Audio • 3D Liquid Glass",
                        fontSize = 12.sp,
                        fontFamily = GeistFontFamily,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Status Badge
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    cornerRadius = 16.dp,
                    chromaticColor = activeColorStart,
                    highlightIntensity = 1.0f,
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "InnerTube 2.0",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GeistFontFamily
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION: "Tuned to your vibe" (BOLI)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = activeColorStart,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tuned to your vibe",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GeistFontFamily,
                        color = Color.White
                    )
                }

                if (vibeTracks.isNotEmpty()) {
                    Text(
                        text = "Based on interaction",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = GeistFontFamily
                    )
                }
            }

            if (vibeTracks.isEmpty()) {
                // Empty Vibe Placeholder Card matching Symphony logic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tunes matching your vibe yet.\nPlay a song to get started!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SymphonyPebbleGlass(
                            backdrop = backdrop,
                            cornerRadius = 16.dp,
                            chromaticColor = activeColorStart,
                            highlightIntensity = 1.0f,
                            onClick = {
                                if (quickPicks.isNotEmpty()) onTrackSelect(quickPicks.first())
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Play a Quick Pick",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GeistFontFamily,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                // Vibe Tracks Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(vibeTracks) { track ->
                        val isCurrent = track.id == currentTrack.id
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onTrackSelect(track) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .shadow(
                                        elevation = if (isCurrent) 16.dp else 6.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        spotColor = activeColorStart
                                    )
                                    .clip(RoundedCornerShape(20.dp))
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
                                    Text("♪", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                                }

                                if (isCurrent && isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.45f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.GraphicEq,
                                            contentDescription = "Playing",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = track.title,
                                color = if (isCurrent) activeColorStart else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GeistFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontFamily = GeistFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Quick Picks Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Quick Picks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GeistFontFamily,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(quickPicks) { track ->
                    val isCurrent = track.id == currentTrack.id
                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { onTrackSelect(track) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(
                                    elevation = if (isCurrent) 16.dp else 8.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = track.colorStart
                                )
                                .clip(RoundedCornerShape(20.dp))
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
                                Text("♪", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }

                            if (isCurrent && isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = "Playing",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = track.title,
                            color = if (isCurrent) activeColorStart else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GeistFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = GeistFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Featured Playlists Carousel
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Featured Playlists",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GeistFontFamily,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(playlists) { pl ->
                    SymphonyPebbleGlass(
                        backdrop = backdrop,
                        shape = { PrismalRoundedRectangle(22.dp) },
                        cornerRadius = 22.dp,
                        chromaticColor = pl.colorStart,
                        highlightIntensity = 1.0f,
                        onClick = { /* Open playlist */ },
                        modifier = Modifier
                            .width(220.dp)
                            .height(110.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(pl.colorStart, pl.colorEnd))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (pl.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = pl.coverUrl,
                                        contentDescription = pl.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = pl.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GeistFontFamily,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${pl.trackCount} Tracks",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontFamily = GeistFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Artists Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Top Artists",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GeistFontFamily,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(artists) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(90.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(artist.color, Color(0xFF1E293B)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (artist.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = artist.avatarUrl,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = artist.name.take(1),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = artist.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GeistFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. SEARCH SCREEN (Live YouTube InnerTube)
// ==========================================
@Composable
fun SymphonySearchScreen(
    currentTrack: Track,
    isPlaying: Boolean,
    activeColorStart: Color,
    downloader: SymphonyDownloader,
    onTrackSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val downloadProgress by downloader.downloadProgress.collectAsState()

    val discoveryGenres = listOf(
        Pair("Synthwave", Color(0xFF8B5CF6)),
        Pair("Cyberpunk", Color(0xFFEF4444)),
        Pair("Hi-Res FLAC", Color(0xFF10B981)),
        Pair("Lo-Fi Beats", Color(0xFF3B82F6)),
        Pair("Electronic", Color(0xFFF59E0B)),
        Pair("Rock Classics", Color(0xFFEC4899))
    )

    fun doSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        scope.launch {
            val results = SymphonyMusicEngine.searchTracks(query)
            searchResults = results
            isSearching = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Search Header & 3D Glass Search Input
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Search",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Text(
                    text = "Millions of songs on YouTube Music",
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Liquid Glass Search Box
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    cornerRadius = 24.dp,
                    chromaticColor = activeColorStart,
                    highlightIntensity = 1.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.length >= 3) {
                                    doSearch(it)
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = GeistFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(activeColorStart),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Artists, songs, or albums...",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 15.sp,
                                        fontFamily = GeistFontFamily
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Genre / Discovery Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveryGenres) { (genre, color) ->
                    SymphonyPebbleGlass(
                        backdrop = backdrop,
                        cornerRadius = 16.dp,
                        chromaticColor = color,
                        highlightIntensity = 0.9f,
                        onClick = {
                            searchQuery = genre
                            doSearch(genre)
                        },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = genre,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GeistFontFamily,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            }
        }

        // Search State / Results
        if (isSearching) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = activeColorStart,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        } else if (searchResults.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search Results (${searchResults.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            itemsIndexed(searchResults) { _, track ->
                val isCurrent = track.id == currentTrack.id
                val isDownloaded = downloader.isDownloaded(track.id)
                val dlProgress = downloadProgress[track.id]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCurrent) activeColorStart.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.03f))
                        .clickable { onTrackSelect(track) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
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
                            Text("♪", color = Color.White, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isCurrent) activeColorStart else Color.White,
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

                    // Download Button
                    IconButton(
                        onClick = {
                            if (!isDownloaded && dlProgress == null) {
                                downloader.startDownload(track)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        when {
                            dlProgress != null -> {
                                CircularProgressIndicator(
                                    progress = { dlProgress },
                                    color = activeColorStart,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            isDownloaded -> {
                                Icon(
                                    imageVector = Icons.Rounded.DownloadDone,
                                    contentDescription = "Downloaded",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = "Download",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    SymphonyPebbleGlass(
                        backdrop = backdrop,
                        isCircle = true,
                        cornerRadius = 16.dp,
                        chromaticColor = activeColorStart,
                        highlightIntensity = 1.0f,
                        onClick = { onTrackSelect(track) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrent && isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. LIBRARY SCREEN (Persistent Storage)
// ==========================================
@Composable
fun SymphonyLibraryScreen(
    likedTracks: List<Track>,
    downloadedTracks: List<Track>,
    recentTracks: List<Track>,
    playlists: List<Playlist>,
    currentTrack: Track,
    isPlaying: Boolean,
    activeColorStart: Color,
    downloader: SymphonyDownloader,
    onTrackSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Liked", "Offline", "Playlists", "Recent")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Library",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Text(
                    text = "Persistent likes, offline downloads and history",
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented control
                PrismalSegmentedControl(
                    labels = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it },
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Liked Tracks Tab
                if (likedTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No liked tracks yet.\nHeart tracks while listening to save them permanently!",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = GeistFontFamily,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    itemsIndexed(likedTracks) { _, track ->
                        val isCurrent = track.id == currentTrack.id
                        val isDownloaded = downloader.isDownloaded(track.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCurrent) activeColorStart.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.03f))
                                .clickable { onTrackSelect(track) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
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
                                    Text("♪", color = Color.White, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isCurrent) activeColorStart else Color.White,
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

                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Liked",
                                tint = Color(0xFFFF3366),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            1 -> {
                // Offline Downloads Tab
                if (downloadedTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No downloaded songs yet.\nTap the download icon on any song to listen 100% offline!",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = GeistFontFamily,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    itemsIndexed(downloadedTracks) { _, track ->
                        val isCurrent = track.id == currentTrack.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCurrent) activeColorStart.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.03f))
                                .clickable { onTrackSelect(track) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DownloadDone,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isCurrent) activeColorStart else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = GeistFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • Offline Audio",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontFamily = GeistFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { downloader.removeDownload(track.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Playlists Tab
                items(playlists) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable { /* open */ }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(pl.colorStart, pl.colorEnd))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = pl.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GeistFontFamily
                            )
                            Text(
                                text = "${pl.trackCount} Tracks • ${pl.subtitle}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontFamily = GeistFontFamily
                            )
                        }
                    }
                }
            }
            3 -> {
                // Recent History Tab
                if (recentTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent playback history yet.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = GeistFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(recentTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .clickable { onTrackSelect(track) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = activeColorStart,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
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
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SETTINGS SCREEN (Symphony Minimalist)
// ==========================================
@Composable
fun SymphonySettingsScreen(
    updater: SymphonyUpdater,
    downloader: SymphonyDownloader,
    activeColorStart: Color,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    val isCheckingUpdates by updater.isChecking.collectAsState()
    val updateAvailable by updater.updateAvailable.collectAsState()
    val latestVersion by updater.latestVersion.collectAsState()
    val releaseNotes by updater.releaseNotes.collectAsState()
    val downloadProgress by updater.downloadProgress.collectAsState()
    val isDownloading by updater.isDownloading.collectAsState()

    var selectedQuality by remember { mutableIntStateOf(0) }
    val qualities = listOf("Hi-Res FLAC (320 kbps)", "High (256 kbps)", "Normal (160 kbps)")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Text(
                    text = "Preferences, audio quality & in-app updates",
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 1. IN-APP UPDATE CARD
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (updateAvailable) activeColorStart.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.04f)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (updateAvailable) Icons.Rounded.SystemUpdate else Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = if (updateAvailable) activeColorStart else Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (updateAvailable) "New Version Available: v$latestVersion" else "Symphony 2.0 is up to date",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GeistFontFamily
                                    )
                                    Text(
                                        text = if (updateAvailable) "Tap below to download & install" else "Current installed version: v2.0.0",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        fontFamily = GeistFontFamily
                                    )
                                }
                            }

                            if (!updateAvailable) {
                                IconButton(
                                    onClick = { updater.checkForUpdates() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    if (isCheckingUpdates) {
                                        CircularProgressIndicator(
                                            color = activeColorStart,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "Check",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (updateAvailable) {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (releaseNotes.isNotBlank()) {
                                Text(
                                    text = releaseNotes.take(160) + "...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = GeistFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (isDownloading) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    color = activeColorStart,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            } else {
                                SymphonyPebbleGlass(
                                    backdrop = backdrop,
                                    cornerRadius = 16.dp,
                                    chromaticColor = activeColorStart,
                                    highlightIntensity = 1.0f,
                                    onClick = { updater.downloadAndInstallUpdate() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                ) {
                                    Text(
                                        text = "Download & Install v$latestVersion",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GeistFontFamily
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 2. AUDIO & STREAMING PREFERENCES
                // ==========================================
                Text(
                    text = "Streaming Audio Quality",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                qualities.forEachIndexed { index, quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedQuality == index) activeColorStart.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.03f)
                            )
                            .clickable { selectedQuality = index }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = quality,
                            color = if (selectedQuality == index) activeColorStart else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (selectedQuality == index) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = GeistFontFamily
                        )

                        if (selectedQuality == index) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = activeColorStart,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 3. OFFLINE STORAGE & CACHE
                // ==========================================
                Text(
                    text = "Offline Downloads & Cache",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Clear All Offline Downloads",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GeistFontFamily
                        )
                        Text(
                            text = "Free up storage on your phone",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = GeistFontFamily
                        )
                    }

                    SymphonyPebbleGlass(
                        backdrop = backdrop,
                        cornerRadius = 14.dp,
                        chromaticColor = Color(0xFFEF4444),
                        highlightIntensity = 0.9f,
                        onClick = { downloader.clearAllDownloads() },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Clear",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GeistFontFamily,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 4. ABOUT SYMPHONY 2.0
                // ==========================================
                Text(
                    text = "About Symphony 2.0",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Symphony 2.0 Native (v2.0.0)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GeistFontFamily
                        )
                        Text(
                            text = "Application ID: com.tanatos.symphonymusic2",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = GeistFontFamily
                        )
                        Text(
                            text = "Developed by Tanatos • Powered by Prismal AGSL & Media3 ExoPlayer",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = GeistFontFamily
                        )
                    }
                }
            }
        }
    }
}
