package com.tanatos.symphonymusic2.ui

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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.components.PrismalSegmentedControl
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.tanatos.symphonymusic2.engine.SymphonyMusicEngine
import com.tanatos.symphonymusic2.model.Artist
import com.tanatos.symphonymusic2.model.Playlist
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.launch

// ==========================================
// 1. HOME SCREEN (Symphony Discovery)
// ==========================================
@Composable
fun SymphonyHomeScreen(
    tracks: List<Track>,
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
                        text = "3D AGSL Liquid Glass • Hi-Fi Streaming",
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

        // Quick Picks Section
        item {
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
                items(tracks) { track ->
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

        // Trending Tracks Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Trending Now",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GeistFontFamily,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(tracks) { index, track ->
            val isCurrent = track.id == currentTrack.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCurrent) activeColorStart.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                    .clickable { onTrackSelect(track) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Text(
                    text = "${index + 1}",
                    color = if (isCurrent) activeColorStart else Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    modifier = Modifier.width(26.dp)
                )

                // Thumbnail
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

                // Play pebble icon
                SymphonyPebbleGlass(
                    backdrop = backdrop,
                    isCircle = true,
                    cornerRadius = 16.dp,
                    chromaticColor = track.colorStart,
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
    onTrackSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    val scope = rememberCoroutineScope()

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
// 3. LIBRARY SCREEN
// ==========================================
@Composable
fun SymphonyLibraryScreen(
    likedTracks: List<Track>,
    playlists: List<Playlist>,
    artists: List<Artist>,
    currentTrack: Track,
    isPlaying: Boolean,
    activeColorStart: Color,
    onTrackSelect: (Track) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Liked Songs", "Playlists", "Artists")

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
                    text = "Your music, playlists and favorites",
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
                if (likedTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No liked tracks yet.\nHeart tracks while listening to add them here!",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = GeistFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(likedTracks) { _, track ->
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
                            if (pl.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = pl.coverUrl,
                                    contentDescription = pl.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Rounded.MusicNote, null, tint = Color.White)
                            }
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
            2 -> {
                items(artists) { ar ->
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
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(ar.color, Color(0xFF1E293B)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ar.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ar.avatarUrl,
                                    contentDescription = ar.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(ar.name.take(1), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = ar.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GeistFontFamily
                            )
                            Text(
                                text = ar.monthlyListeners,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontFamily = GeistFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. GLASS LAB SCREEN (AGSL Shader Tuning)
// ==========================================
@Composable
fun SymphonyGlassLabScreen(
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    specularIntensity: Float,
    onBlurRadiusChange: (Float) -> Unit,
    onRefractionHeightChange: (Float) -> Unit,
    onRefractionAmountChange: (Float) -> Unit,
    onSpecularIntensityChange: (Float) -> Unit,
    activeColorStart: Color,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "AGSL Glass Lab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GeistFontFamily,
                    color = Color.White
                )
                Text(
                    text = "Tune hardware shaders and liquid physics in real-time",
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive preview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    activeColorStart.copy(alpha = 0.5f),
                                    Color(0xFF0F172A),
                                    Color(0xFF3B82F6).copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SymphonyPebbleGlass(
                        backdrop = backdrop,
                        shape = { PrismalRoundedRectangle(24.dp) },
                        cornerRadius = 24.dp,
                        chromaticColor = activeColorStart,
                        highlightIntensity = specularIntensity,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(100.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "3D Liquid Glass Pebble",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GeistFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Live AGSL Refraction & Meniscus",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = GeistFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sliders
                Text(
                    text = "Blur Radius: ${blurRadius.toInt()} dp",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = GeistFontFamily
                )
                PrismalGlassSlider(
                    value = { (blurRadius - 4f) / (40f - 4f) },
                    onValueChange = { onBlurRadiusChange(4f + it * 36f) },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.005f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Refraction Height: ${refractionHeight.toInt()} dp",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = GeistFontFamily
                )
                PrismalGlassSlider(
                    value = { (refractionHeight - 4f) / (40f - 4f) },
                    onValueChange = { onRefractionHeightChange(4f + it * 36f) },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.005f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Refraction Amount: ${refractionAmount.toInt()} dp",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = GeistFontFamily
                )
                PrismalGlassSlider(
                    value = { refractionAmount / 64f },
                    onValueChange = { onRefractionAmountChange(it * 64f) },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.005f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Specular Intensity: ${String.format("%.2f", specularIntensity)}x",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = GeistFontFamily
                )
                PrismalGlassSlider(
                    value = { (specularIntensity - 0.5f) / 1.5f },
                    onValueChange = { onSpecularIntensityChange(0.5f + it * 1.5f) },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.005f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
