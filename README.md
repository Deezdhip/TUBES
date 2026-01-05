# FocusTask

<div align="center">

![FocusTask Banner](screenshots/banner.png)

**A Modern, Minimalist Productivity Suite for Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)

</div>

---

## 📱 About

**FocusTask** is a native Android task management application built with **Kotlin** and **Jetpack Compose**. It combines task management (To-Do), focus time tracking (Focus Timer), and productivity analytics (Dashboard) in a single ecosystem with real-time synchronization.

Designed with a **Deep Navy & Clean White** theme, FocusTask delivers a premium, futuristic user experience while maintaining simplicity and functionality.

---

## 📸 Screenshots

| Login | Home | Timer | Dashboard |
|:-----:|:----:|:-----:|:---------:|
| ![Login](screenshots/login.png) | ![Home](screenshots/home.png) | ![Timer](screenshots/timer.png) | ![Dashboard](screenshots/dashboard.png) |

---

## ✨ Key Features

### 🎨 Modern UI/UX
- **Deep Navy & Clean White** theme with elegant contrast
- **Slide transitions** for smooth navigation
- **Glassmorphism** authentication screens
- **Futuristic splash screen** with scale animation

### 📋 Smart Task Management
- **Auto-sorting**: Pinned tasks first, then overdue, then by deadline
- **Visual categories**: Work, Study, Personal, Others with distinct colors
- **Quick search** functionality
- **Soft delete** with Recycle Bin for task recovery

### ⏱️ Real-Time Focus Timer
- **Accurate tracking**: Records focus time to the second (not estimates)
- **Control Panel design**: Minimalist floating card UI
- **Keep screen on**: Prevents screen timeout during focus sessions
- **Auto-sync**: Focus time automatically updates to statistics

### 📊 Live Dashboard
- **Stacked Insight Cards**: Modern analytics layout (not grid)
- **Hero Metric**: Total focus time with gradient card
- **Completion Rate**: Visual progress with percentage
- **Category Breakdown**: Focus distribution by category
- **Overdue Alerts**: Warning card for past-deadline tasks

### ☁️ Cloud Sync
- **Firebase Authentication**: Secure email/password login
- **Cloud Firestore**: Real-time data synchronization
- **Offline support**: Works without internet, syncs when connected

### 🖼️ Efficient Storage
- **Base64 Profile Photos**: Compressed image storage without external bucket
- **No Firebase Storage required**: Reduces complexity and cost

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 100% |
| **UI Toolkit** | Jetpack Compose (Material3) |
| **Architecture** | MVVM + Clean Architecture |
| **Backend** | Firebase Authentication, Cloud Firestore |
| **Concurrency** | Kotlin Coroutines & Flow (StateFlow) |
| **Image Loading** | Coil |
| **Icons** | Material Icons Extended |

---

## 🏗️ Technical Highlights

### MVVM Architecture
```
├── model/          # Data classes (Task, User)
├── repository/     # Firebase data operations
├── viewmodel/      # UI state management
├── ui/
│   ├── screens/    # Composable screens
│   ├── components/ # Reusable UI components
│   └── theme/      # Colors, Typography, Theme
└── util/           # Helper classes
```

### Key Solutions

**Real-Time Focus Tracking**
- Timer records actual seconds spent (`focusTimeSpent` field)
- Accumulated across multiple sessions per task
- Dashboard calculates total from live data, not estimates

**Base64 Image Storage**
- Profile photos compressed and stored as Base64 strings
- Stored directly in Firestore user document
- Eliminates need for Firebase Storage bucket

**Reactive State Management**
- `StateFlow` for UI state observation
- `combine()` for merging multiple data streams
- Real-time Firestore listeners for instant updates

---

## 🚀 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Firebase project with Authentication & Firestore enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/focustask.git
   cd focustask
   ```

2. **Configure Firebase**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable **Email/Password Authentication**
   - Create a **Cloud Firestore** database
   - Download `google-services.json`
   - Place it in `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click **Run**

---

## 📁 Project Structure

```
app/src/main/java/com/example/tubes/
├── MainActivity.kt
├── model/
│   ├── Task.kt
│   └── User.kt
├── repository/
│   ├── AuthRepository.kt
│   ├── TaskRepository.kt
│   └── UserRepository.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── TaskViewModel.kt
│   ├── TimerViewModel.kt
│   └── StatisticsViewModel.kt
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── TaskListScreen.kt
│   │   ├── TimerScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── ProfileScreen.kt
│   │   └── RecycleBinScreen.kt
│   ├── components/
│   │   ├── TaskItem.kt
│   │   └── AddTaskDialog.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    ├── DateUtils.kt
    ├── NotificationHelper.kt
    └── SoundManager.kt
```

---

## 📄 License

```
MIT License

Copyright (c) 2026 FocusTask

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**Built with ❤️ using Kotlin & Jetpack Compose**

</div>
