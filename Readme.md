# Zeku - Modern audio & video downloader
Zeku is a modern Android app to download video and audio from YouTube and 1000\+ other websites using `yt-dlp`. It combines a clean, material-style UI with powerful, configurable download options and background download support.

> [!IMPORTANT]
> Zeku is currently in a early development stage. This means core features may be incomplete,
> and the app is likely to contain bugs or experience crashes. It is intended for developers 
> for testing, feedback, and contribution purposes, and is not yet suitable for general use.
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
- Android SDK (minSdkVersion 26, targetSdkVersion 34)
- Git
- Windows users: use `gradlew.bat` from project root for CLI builds

### Build and Run in Android Studio
1.  **Clone the repository:**
2.  **Open the project** in Android Studio.
3.  Let Gradle sync all project dependencies. Android Studio may prompt you to install required SDK components.
4.  The `yt-dlp` and `ffmpeg` binaries are bundled with the project. No special setup is required.
5.  Run the app on a connected physical device or an Android emulator.



### Build from Command Line
```bash
.\gradlew.bat assembleDebug.
```

## Contributing

Contributions are welcome! Since Zeku is in an early stage, there are many opportunities to help. Whether it's reporting a bug, suggesting a new feature, or writing code, your input is valuable.

Please check the project's **Issues** tab to see if a similar bug or feature request already exists. To submit code, please open a pull request.
