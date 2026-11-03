# Symphony 2.0 🎵✨

> **The Next Generation Liquid Glass Native Music Player for Android**  
> Built with 100% Kotlin, Jetpack Compose, and custom AGSL hardware shaders.

---

## 🌟 Highlights

- **3D Convex Liquid Glass UI**: Custom AGSL hardware shaders replicating convex optical physics, silver specular meniscus caps, razor-sharp highlight edges, and chromatic dispersion rims.
- **Dynamic Material You Palette**: Real-time palette extraction (`androidx.palette`) from album artwork dynamically tints living ambient glowing backdrops and liquid glass controls.
- **Media3 Background Audio Engine**: Robust background playback powered by AndroidX `Media3` + `ExoPlayer`, handling audio focus, headphone disconnects, lockscreen controls, and system notifications.
- **YouTube Music Streaming**: Live online song searching, trending tracks, quick picks, and unciphered audio stream resolution.
- **LRCLIB Synchronized Lyrics**: Real-time karaoke lyrics drawer with automated synchronization and scrolling.
- **Interactive AGSL Glass Lab**: Live tuning playground for blur radius, refraction height, refraction amount, and specular highlights.
- **Floating Liquid Dock & Miniplayer**: Seamless gesture navigation, spring physics press states, and full-screen player expansion.

---

## 📱 Screenshots & Architecture

```
com.tanatos.symphonymusic2
├── engine/
│   ├── SymphonyAudioService.kt    # AndroidX MediaSessionService + ExoPlayer
│   ├── SymphonyPlaybackManager.kt # Reactive StateFlow playback coordinator
│   ├── SymphonyMusicEngine.kt     # YouTube InnerTube API & Invidious stream extractor
│   └── SymphonyLyricsEngine.kt    # LRCLIB synchronized lyrics engine
├── model/
│   └── SymphonyModels.kt          # Data models (Track, Playlist, LyricLine)
├── ui/
│   ├── SymphonyApp.kt             # Root app coordinator & glass layers
│   ├── SymphonyPebbleGlass.kt     # 3D convex liquid glass pebble component
│   ├── SymphonyMiniPlayer.kt      # Floating dock miniplayer
│   ├── SymphonyFullPlayer.kt      # Expanded player with lyrics drawer & ambient glow
│   ├── SymphonyScreens.kt         # Home, Search, Library & Glass Lab screens
│   └── GeistTheme.kt              # Modern typography with Geist font family
└── MainActivity.kt                # Edge-to-edge window setup & service binder
```

---

## 🛠️ Building & Installing

### Prerequisites
- Android Studio Ladybug / Meerkat or Command Line Tools
- Android SDK 35+
- JDK 17+

### Build Debug APK
```bash
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```
The optimized and signed APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 📄 License
Licensed under Apache 2.0.
