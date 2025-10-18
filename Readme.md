# Zeku - Modern audio & video downloader
Zeku is a modern Android app to download video and audio from YouTube and 1000\+ other websites using `yt-dlp`. It combines a clean, material-style UI with powerful, configurable download options and background download support.

> [!IMPORTANT]
> Zeku is in a pre-alpha state, and only suitable for use by developers
>


## Features

- Download audio (mp3, m4a) and video (mp4, mkv, webm) formats
- Batch downloads and playlist support
- Select quality and format before downloading
- Background downloading with notifications and resumable transfers
- Built-in search and paste\&download workflows
- Uses `yt-dlp` as the backend for broad site compatibility
- Lightweight, privacy-conscious (no tracking)

## Technologies

- Kotlin and Java (Android)
- Jetpack compose
- Gradle build system
- Bundled `yt-dlp` binary invoked from the app
- Ffmpeg


## Getting started

Prerequisites:
- Android Studio (recommended) or Gradle CLI
- Android SDK with an API level supported by the project
- Windows users: use `gradlew.bat` from project root for CLI builds

Build and run in Android Studio:
1. Open the project in Android Studio.
2. Let Gradle sync and install any required SDK components.
3. Run the app on an emulator or a device.

CLI build (Windows):
```bash
.\gradlew.bat assembleDebugeo, audio form youtube, +1000 website using yt-dlp