# DevOps Dashboard Frontend

A modern, high-performance cross-platform dashboard built with **Compose Multiplatform**. This frontend provides a premium, glassmorphic UI for managing deployments, viewing real-time logs, and configuring system settings.

## ✨ Key Features

- **Glassmorphic Design:** A sleek, modern aesthetic with translucent cards, vibrant gradients, and smooth animations.
- **Real-Time Monitoring:** Live updates via WebSockets for deployment status and health checks.
- **Log Streaming:** Integrated terminal viewer to stream GitHub Action build logs in real-time.
- **Dynamic Filtering:** Search and filter deployments by status (Active, Failed, Completed) or name.
- **Secure Authentication:** JWT-based login system with local token persistence and role-based UI views.
- **Multi-Platform:** Built using Compose Multiplatform, targeting Desktop (JVM) and Web (Kotlin/JS/Wasm).

## 🛠️ Tech Stack

- **Framework:** Compose Multiplatform (JetBrains)
- **Language:** Kotlin
- **Networking:** Ktor Client (HTTP & WebSockets)
- **State Management:** Kotlin Coroutines & Flow
- **Styling:** Material 3 with Custom Glassmorphism components
- **Build Tool:** Gradle

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher (Java 21+ recommended)
- IntelliJ IDEA (with Compose Multiplatform plugin)

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/devops-frontend.git
   cd devops-frontend
   ```

2. **Run Desktop App:**
   ```bash
   ./gradlew :composeApp:run
   ```

3. **Run Web App:**
   ```bash
   ./gradlew :composeApp:jsBrowserRun
   ```

## ⚙️ Configuration

The frontend connects to the `devops_api` backend. Ensure the backend URL is correctly configured in the `AuthStore` or environment settings for production builds.

## 📸 UI Components
- **DeploymentCard:** Pulsating status indicators and live progress bars.
- **StatsRow:** Summary dashboard for infrastructure overview.
- **SettingsScreen:** Secure management of GitHub tokens and webhook secrets.
