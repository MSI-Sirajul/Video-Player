# Advanced Video Player

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-007396?style=flat-square&logo=java&logoColor=white)
![Developer](https://img.shields.io/badge/Developer-MSI--Sirajul-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-orange?style=flat-square)
![Size](https://img.shields.io/github/repo-size/MSI-Sirajul/Video-Player?style=flat-square&color=red)

A powerful, highly optimized, and feature-rich Android Video Player built using the Media3 (ExoPlayer) library. Designed with a focus on User Experience (UX), performance, and a modern Material Design interface. It supports all major video formats, background playback, gestures, and advanced file management.

**Project Link:** [Video-Player](https://github.com/MSI-Sirajul/Video-Player)

## App Previews

| Home Screen | Video Player | Settings Menu |
|:---:|:---:|:---:|
| ![Home](asset/image1.png) | ![Player](screenshots/image3.png) | ![Settings](screenshots/settings.png) |

| Mini Player | Audio Booster | Folder View |
|:---:|:---:|:---:|
| ![MiniPlayer](asset/image2.png) | ![Booster](screenshots/booster.png) | ![Folder](screenshots/folder.png) |

*(Note: Please upload screenshots to a 'screenshots' folder in your repository to make these images visible.)*

## Key Features

### Playback Experience
*   **Core Engine:** Built on top of the robust Media3 ExoPlayer library for smooth playback.
*   **Background Play:** Continue listening to audio even when the app is minimized or the screen is locked, complete with a persistent notification bar.
*   **Mini Player:** A floating mini-player allows seamless navigation within the app while the video continues to play.
*   **Resume Support:** Automatically remembers the last played position for all videos.
*   **Audio Booster:** Integrated software audio booster to increase volume up to 150%.
*   **Speed Control:** Adjustable playback speed ranging from 0.5x to 2.0x.

### Smart Controls & Gestures
*   **Intuitive Gestures:** Swipe controls for Volume (Right), Brightness (Left), and Seek (Horizontal).
*   **Smart Pinch Zoom:** Zoom in/out (25% to 400%) with real-time percentage indication.
*   **Safe Zone Logic:** Prevents accidental system bar triggers during gesture usage.
*   **Screen Lock:** Lock mode to prevent accidental touches during playback.
*   **Orientation:** Automatic sensor-based rotation with manual override.

### Library & File Management
*   **Scanning:** Fast background scanning using a custom database helper.
*   **View Modes:** Toggle between Grid View and List View.
*   **Sorting:** Sort videos by Name, Date, Size, or Duration.
*   **Filtering:** Filter content by specific extensions (MP4, MKV, etc.) or hide short videos.
*   **Folder Support:** Browse videos via specific folders with storage indication (Internal/SD Card).
*   **Search:** Global search functionality to find videos instantly.

### UI & Customization
*   **Material Design:** Clean, minimal, and responsive UI optimized for all screen densities (SDP/SSP support).
*   **Themes:** Full support for System Default, Dark Mode, and Light Mode.
*   **Animations:** Smooth Lottie animations for loading states and slide transitions for fragments.

## Technical Stack & Libraries

*   **Language:** Java
*   **Minimum SDK:** 24 (Android 7.0)
*   **Target SDK:** 34 (Android 14)
*   **Architecture:** MVVM Pattern (Partial), Room/SQLite Database

### Dependencies
*   **Media3 ExoPlayer:** `androidx.media3:media3-exoplayer` (Core Player)
*   **Media3 UI:** `androidx.media3:media3-ui` (Player Views)
*   **Media3 Session:** `androidx.media3:media3-session` (Background Service)
*   **Glide:** `com.github.bumptech.glide:glide` (Image/Thumbnail Loading)
*   **Lottie:** `com.airbnb.android:lottie` (Vector Animations)
*   **SDP & SSP:** `com.intuit.sdp` / `com.intuit.ssp` (Responsive Layout Dimensions)
*   **AndroidX:** AppCompat, ConstraintLayout, RecyclerView, CardView, ViewPager2

## Required Permissions

The application requires the following permissions to function correctly:

*   `READ_EXTERNAL_STORAGE` / `READ_MEDIA_VIDEO`: To scan and list video files from the device.
*   `FOREGROUND_SERVICE`: To maintain background playback and notification controls.
*   `POST_NOTIFICATIONS`: To display the media control notification (Android 13+).
*   `WAKE_LOCK`: To keep the screen on during video playback.

## Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/MSI-Sirajul/Video-Player.git
    ```
2.  Open the project in **Android Studio**.
3.  Sync the Gradle project to download dependencies.
4.  Build and Run the application on your Android device or emulator.

## Developer Info

**Developed by:** MSI-Sirajul

*   **GitHub:** [MSI-Sirajul](https://github.com/MSI-Sirajul)
*   **Repository:** [Video Player](https://github.com/MSI-Sirajul/Video-Player)

If you find this project useful, please consider giving it a star on GitHub.

---

*This project is for educational and open-source purposes.*