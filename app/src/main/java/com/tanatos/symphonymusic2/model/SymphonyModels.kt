package com.tanatos.symphonymusic2.model

import androidx.compose.ui.graphics.Color

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationSeconds: Int = 180,
    val thumbnailUrl: String = "",
    val audioStreamUrl: String = "",
    val colorStart: Color = Color(0xFF4FA5FF),
    val colorEnd: Color = Color(0xFF8B5CF6),
    val lyrics: List<LyricLine> = emptyList(),
    val isLiveStream: Boolean = false
)

data class Playlist(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val trackCount: Int = 0,
    val coverUrl: String = "",
    val colorStart: Color = Color(0xFF3B82F6),
    val colorEnd: Color = Color(0xFF10B981)
)

data class Artist(
    val id: String,
    val name: String,
    val monthlyListeners: String = "",
    val avatarUrl: String = "",
    val color: Color = Color(0xFFEC4899)
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class SyncedLyrics(
    val trackId: String,
    val lines: List<LyricLine>
)

object SymphonyCatalog {
    val sampleTracks = listOf(
        Track(
            id = "symphony_starboy",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            durationSeconds = 230,
            thumbnailUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80",
            audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            colorStart = Color(0xFFEF4444),
            colorEnd = Color(0xFF7C3AED),
            lyrics = listOf(
                LyricLine(0, "♪ [Instrumental Synth Intro] ♪"),
                LyricLine(8000, "I'm tryna put you in the worst mood, ah"),
                LyricLine(12500, "P1 cleaner than your church shoes, ah"),
                LyricLine(16800, "Milli point two just to hurt you, ah"),
                LyricLine(21000, "All red Lamb' just to tease you, ah"),
                LyricLine(25500, "None of these toys on lease too, ah"),
                LyricLine(29800, "Made your whole year in a week too, yah"),
                LyricLine(34200, "Main bitch out your league too, ah"),
                LyricLine(38500, "Side bitch out of your league too, ah"),
                LyricLine(42800, "Look what you've done"),
                LyricLine(46900, "I'm a motherfuckin' starboy"),
                LyricLine(51000, "Look what you've done"),
                LyricLine(55200, "I'm a motherfuckin' starboy")
            )
        ),
        Track(
            id = "symphony_midnight_city",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationSeconds = 244,
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            colorStart = Color(0xFF3B82F6),
            colorEnd = Color(0xFF06B6D4),
            lyrics = listOf(
                LyricLine(0, "♪ [Ethereal Electronic Echoes] ♪"),
                LyricLine(15000, "Waiting in a car"),
                LyricLine(22000, "Waiting for a ride in the dark"),
                LyricLine(30000, "The night city grows"),
                LyricLine(38000, "Look and see her eyes, they glow"),
                LyricLine(45000, "Waiting in a car"),
                LyricLine(52000, "Waiting for a ride in the dark"),
                LyricLine(60000, "The city is my church"),
                LyricLine(68000, "It wraps me in the blinding twilight")
            )
        ),
        Track(
            id = "symphony_blinding_lights",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationSeconds = 200,
            thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            colorStart = Color(0xFFF59E0B),
            colorEnd = Color(0xFFDC2626),
            lyrics = listOf(
                LyricLine(0, "Yeah"),
                LyricLine(12000, "I've been tryna call"),
                LyricLine(16000, "I've been on my own for long enough"),
                LyricLine(22000, "Maybe you can show me how to love, maybe"),
                LyricLine(30000, "I'm going through withdrawals"),
                LyricLine(35000, "You don't even have to do too much"),
                LyricLine(40000, "You can turn me on with just a touch, baby"),
                LyricLine(47000, "I look around and Sin City's cold and empty"),
                LyricLine(53000, "No one's around to judge me"),
                LyricLine(58000, "I can't see clearly when you're gone"),
                LyricLine(64000, "I said, ooh, I'm blinded by the lights"),
                LyricLine(70000, "No, I can't sleep until I feel your touch")
            )
        ),
        Track(
            id = "symphony_aerodynamic",
            title = "Aerodynamic",
            artist = "Daft Punk",
            album = "Discovery",
            durationSeconds = 212,
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            colorStart = Color(0xFF10B981),
            colorEnd = Color(0xFF3B82F6),
            lyrics = listOf(
                LyricLine(0, "♪ [Church Bell Chimes] ♪"),
                LyricLine(12000, "♪ [Funk Groove Pulse] ♪"),
                LyricLine(35000, "♪ [Virtuoso Guitar Solo] ♪"),
                LyricLine(80000, "♪ [Bassline Crescendo] ♪")
            )
        ),
        Track(
            id = "symphony_resonance",
            title = "Resonance",
            artist = "HOME",
            album = "Odyssey",
            durationSeconds = 212,
            thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            audioStreamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            colorStart = Color(0xFF8B5CF6),
            colorEnd = Color(0xFFEC4899),
            lyrics = listOf(
                LyricLine(0, "♪ [Analog Synth Wave 1] ♪"),
                LyricLine(24000, "♪ [Chillwave Ambient Sweep] ♪"),
                LyricLine(52000, "♪ [Nostalgic Resonance Drop] ♪")
            )
        )
    )

    val samplePlaylists = listOf(
        Playlist(
            id = "pl_synthwave",
            title = "Liquid Synthwave & Vapor",
            subtitle = "AGSL Glass Aura Selection",
            trackCount = 42,
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            colorStart = Color(0xFF8B5CF6),
            colorEnd = Color(0xFFEC4899)
        ),
        Playlist(
            id = "pl_midnight",
            title = "Midnight Drive 2.0",
            subtitle = "Deep bass and glowing highways",
            trackCount = 28,
            coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
            colorStart = Color(0xFF3B82F6),
            colorEnd = Color(0xFF10B981)
        ),
        Playlist(
            id = "pl_cyberpunk",
            title = "Cyberpunk Neo-Tokyo",
            subtitle = "High octane electronic beats",
            trackCount = 35,
            coverUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80",
            colorStart = Color(0xFFEF4444),
            colorEnd = Color(0xFFF59E0B)
        ),
        Playlist(
            id = "pl_ambient",
            title = "Liquid Glass Chill",
            subtitle = "Pure relaxation and spatial sound",
            trackCount = 50,
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            colorStart = Color(0xFF06B6D4),
            colorEnd = Color(0xFF3B82F6)
        )
    )

    val sampleArtists = listOf(
        Artist("ar_weeknd", "The Weeknd", "112.4M monthly listeners", "https://images.unsplash.com/photo-1520523839898-5071217b73a6?w=600&auto=format&fit=crop&q=80", Color(0xFFEF4444)),
        Artist("ar_daft", "Daft Punk", "28.6M monthly listeners", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80", Color(0xFFF59E0B)),
        Artist("ar_m83", "M83", "14.2M monthly listeners", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80", Color(0xFF3B82F6)),
        Artist("ar_home", "HOME", "3.8M monthly listeners", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80", Color(0xFF8B5CF6))
    )
}
