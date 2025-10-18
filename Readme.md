
# **Zeku**

**Download video & audio from anywhere, beautifully.**  
Zeku is a modern Android app for downloading video and audio from YouTube and over 1,000 other websites. Built with Material You and powered by yt-dlp, it offers a clean, intuitive interface without sacrificing powerful features.  

> [!IMPORTANT]
> Zeku is in a pre-alpha state, and only suitable for use by developers
>

### **⚠️ Project Status: Alpha Stage**

**Zeku is currently in active development.** This means core features may be incomplete, and you will likely encounter bugs or crashes. It is intended for developers and testers for feedback and contribution purposes and is not yet ready for general use.

## **✨ Features**

Zeku combines a simple user experience with robust downloading capabilities.

* **Powerful Engine:**
    * **Wide Compatibility:** Download from YouTube, Vimeo, and 1,000+ other sites thanks to the yt-dlp backend.
    * **Versatile Formats:** Save files as audio (mp3, m4a, opus) or video (mp4, mkv, webm).
    * **Playlist Support:** Download entire playlists with a single link.
* **Modern User Experience:**
    * **Material You UI:** A clean, dynamic interface built with Jetpack Compose.
    * **Background Downloads:** Start a download and continue using other apps. Progress is shown in a system notification.
    * **Smart Link Handling:** Paste a link directly into the app or use Android's share menu.
    * **Built-in Search:** Find content without leaving the app.
* **Advanced Control:**
    * **Quality Selection:** Choose your preferred video resolution or audio bitrate before downloading.
    * **Resumable Downloads:** Don't worry about network interruptions; easily resume broken transfers.
    * **Privacy-Focused:** No trackers, no ads. Your data is your own.


## **📸 Screenshots**

## **🛠️ Tech Stack & Architecture**

Zeku is built with a modern Android technology stack, ensuring it is robust, efficient, and maintainable.

* **Core:** 100% **Kotlin**
* **UI:** **Jetpack Compose** for a modern, declarative UI.
* **Architecture:** Follows **MVVM (Model-View-ViewModel)** principles.
* **Backend:** Integrates native **yt-dlp** and **FFmpeg** binaries for downloading and processing.
* **Build System:** **Gradle**

## **🚀 Getting Started**

Ready to build and run the project? Follow these steps.

### **Prerequisites**

* Android Studio (Latest stable version recommended)
* Android SDK (minSdkVersion 26, targetSdkVersion 34\)
* Git

### **Build and Run**

1. **Clone the Repository:**  
   git clone \[https://github.com/dontknow492/Zeku.git)

2. **Open in Android Studio:**
    * Launch Android Studio.
    * Select File \> Open and navigate to the cloned project directory.
3. **Sync Gradle:**
    * Let Android Studio sync the project dependencies. It may prompt you to install required SDK components.
4. **Run the App:**
    * The yt-dlp and ffmpeg binaries are already bundled. No special setup is required.
    * Click the Run 'app' button to build and install the app on a connected device or emulator.

### **Build from Command Line**

Navigate to the project's root directory and run the appropriate command for your OS:

* **Windows:**  
  .\\gradlew.bat assembleDebug

* **macOS / Linux:**  
  ./gradlew assembleDebug

The generated APK will be located in app/build/outputs/apk/debug/.

## **🤝 How to Contribute**

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. **Fork the Project**
2. **Create your Feature Branch** (git checkout \-b feature/AmazingFeature)
3. **Commit your Changes** (git commit \-m 'Add some AmazingFeature')
4. **Push to the Branch** (git push origin feature/AmazingFeature)
5. **Open a Pull Request**

Please check the project's **Issues** tab to see if a similar bug report or feature request already exists before creating a new one.

## **📜 License**

Distributed under the MIT License. See LICENSE for more information.  
*(Note: You should add a LICENSE file to your repository. MIT is a common and permissive choice.)*
