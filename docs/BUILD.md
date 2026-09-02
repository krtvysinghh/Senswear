# Senswear Build & Packaging Guide

---

## 1. Prerequisites
- **JDK**: OpenJDK 17+ (or OpenJDK 21)
- **Android SDK**: Compile SDK 36 (Android 16), Min SDK 26 (Android 8.0)
- **Build Tools**: 36.0.0
- **Gradle**: 9.1.0 with Android Gradle Plugin (AGP) 9.0.1

---

## 2. CLI Build Commands

### Set Environment Variables (macOS/Linux)
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
```

### Run Unit Test Suite
```bash
./gradlew testDebugUnitTest
```

### Assemble Debug APK
```bash
./gradlew assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Assemble Release APK
```bash
./gradlew assembleRelease
```
