# Fix TimeoutException in Gradle Build

The project is experiencing a `java.util.concurrent.TimeoutException` during the build process. This is often caused by the Gradle daemon or worker processes (like AAPT2) running out of memory or hanging.

## User Review Required

> [!IMPORTANT]
> The plan involves increasing the memory allocated to the Gradle daemon. If your machine has limited RAM (e.g., less than 8GB), we might need to adjust the values.

## Proposed Changes

### Gradle Configuration

#### [MODIFY] [gradle.properties](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/gradle.properties)
- Increase `org.gradle.jvmargs` heap size from `1536m` to `4g`.
- Add `MaxMetaspaceSize` and `file.encoding` settings for better stability on Windows.
- (Optional) Disable `android.enableJetifier` if no legacy support libraries are used, but I will keep it for now to avoid regression unless the timeout persists.

## Verification Plan

### Automated Tests
1. Run `./gradlew --stop` to kill existing daemons.
2. Run `./gradlew clean assembleDebug` to verify the build completes successfully with the new settings.

### Manual Verification
1. Verify in Android Studio that the "Build" output no longer shows the `TimeoutException`.
