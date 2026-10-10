# Employee Management Multiplatform Project Documentation

This document explains the architecture, technologies, and execution instructions for the Employee Management system.

## 🛠 Technologies Used

- **Kotlin Multiplatform (KMP)**: The core architecture allowing code sharing across Desktop, Web, Android, and iOS.
- **Compose Multiplatform**: A declarative UI framework used in the `composeApp` module to build the interface once and render it natively across platforms.
- **Ktor Server**: A lightweight, asynchronous Kotlin web framework used to build the REST API backend.
- **Ktor Client**: Used in the frontend to make HTTP requests (`GET`, `POST`, `PUT`, `DELETE`) to the backend.
- **PostgreSQL**: The relational database used to persistently store employee records.
- **JetBrains Exposed**: A Kotlin SQL framework (ORM) used in the server to safely query and modify the PostgreSQL database.
- **Kotlinx Serialization**: Used to convert Kotlin objects (like the `Employee` class) into JSON strings for API communication.
- **Kotlinx DateTime**: A multiplatform library for handling dates (used for the Employee Date of Birth).

---

## 📁 Project Structure & Code Explanation

The project is divided into three main modules:

### 1. `server` (Backend)
This module runs entirely on the JVM and is responsible for handling database operations and serving API requests.
- **`Application.kt`**: The heart of the backend. 
  - **Database.connect()**: Connects to your PostgreSQL `employeedb`.
  - **SchemaUtils.create(Employees)**: Automatically creates the SQL table if it doesn't exist.
  - **routing { ... }**: Defines the REST API endpoints. It intercepts web requests (like `/employees`) and maps them to Exposed database queries (e.g., `Employees.selectAll()`, `Employees.insert()`).

### 2. `shared` (Common Business Logic)
This module contains code that is shared across *all* platforms (Frontend and Backend).
- **`Employee.kt`**: A `@Serializable` data class. Both the Ktor Server and the Compose Frontend use this exact same class to understand what an Employee looks like.
- **`ImagePicker.kt` (commonMain)**: Uses the `expect` keyword to define an interface for picking an image. Because Android, Desktop, and Web all open file pickers differently, KMP requires platform-specific implementations.
- **`ImagePicker.jvm.kt` (jvmMain)**: The `actual` implementation for Desktop, which opens a `JFileChooser` window so the user can select a `.jpg`/`.png` file from their computer.

### 3. `composeApp` (Frontend UI)
This module contains the Compose Multiplatform UI.
- **`App.kt` (commonMain)**: The main dashboard screen. It fetches the data via Ktor Client and draws the Search Bar, the "+" button, the `LazyColumn` list of employees, and the dialog boxes. 
- **`EmployeePhoto.kt` (commonMain)**: An `expect` function that defines a placeholder for displaying an image.
- **`EmployeePhoto.desktop.kt` (desktopMain)**: The `actual` Desktop implementation that reads the physical image file from your hard drive and draws it inside the Compose UI using Skia (`ImageBitmap`).
- **`main.kt` (desktopMain)**: The entry point for the Desktop application that creates the application window.

---

## 🌍 How Multiplatform Works

Kotlin Multiplatform works using a concept called **Source Sets**:
1. **`commonMain`**: You write 90% of your logic and UI here. This code is purely Kotlin and doesn't know about specific operating systems.
2. **Platform Specific (`desktopMain`, `androidMain`, `iosMain`, `wasmJsMain`)**: You write the remaining 10% here using the `expect` and `actual` keywords. If your `commonMain` code needs to access the camera, it writes `expect fun openCamera()`. Then, inside `androidMain`, you write `actual fun openCamera()` using Android's CameraX API, and in `iosMain` you use Apple's AVFoundation. 

When you compile the app for Android, Gradle combines `commonMain` + `androidMain`. When you compile for Desktop, it combines `commonMain` + `desktopMain`.

---

## 🚀 How to Run the Platforms

*Note: During setup, iOS, Android, and Web targets were temporarily commented out in the `build.gradle.kts` files to stabilize the Desktop build with Ktor 3.0. To run them, you would uncomment `androidTarget()`, `iosX64()`, and `wasmJs()` in the Gradle files.*

### 1. The Server (Must be running first)
The backend must always be running so the frontends have a database to talk to.
```bash
./gradlew :server:run
```

### 2. Desktop (Windows/macOS/Linux)
Compiles a native JVM application.
```bash
./gradlew :composeApp:run
```

### 3. Web (Browser)
Compiles the Kotlin UI into WebAssembly (Wasm) or JavaScript to run directly in Google Chrome / Safari.
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
*Note: Once running, you open the provided `localhost` URL in your browser to see the Web UI.*

### 4. Android
Compiles an APK. Requires Android Studio and an Android Emulator running.
```bash
./gradlew :composeApp:installDebug
```
*Note: You can also open the project in Android Studio, select the `composeApp` configuration in the top right, and click the green Play button.*

### 5. iOS
Compiles an iOS app. **Requires a Mac computer with Xcode installed.**
```bash
./gradlew :composeApp:iosSimulatorArm64Run
```
*Note: You can also open the `iosApp/iosApp.xcworkspace` folder in Xcode and click the Play button to launch the iPhone simulator.*
