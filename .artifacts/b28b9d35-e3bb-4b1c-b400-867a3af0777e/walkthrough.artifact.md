# Walkthrough - Fixing Gradle TimeoutException

I have successfully resolved the `java.util.concurrent.TimeoutException` that was occurring during the build. This error was caused by the Gradle daemon running out of memory while handling worker processes in Gradle 8.13.

## Changes Made

### Configuration Update
Modified [gradle.properties](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/gradle.properties) to increase the heap size and add stability flags:
- Increased `org.gradle.jvmargs` to `-Xmx4g` (from `1536m`).
- Added `-XX:MaxMetaspaceSize=1g` to prevent metadata-related crashes.
- Added `-Dfile.encoding=UTF-8` to ensure consistent file handling on Windows.

## Verification Results

### Build Success
- Ran `./gradlew --stop` to ensure new JVM settings were picked up by a fresh daemon.
- Successfully executed `./gradlew clean assembleDebug`, which completed without any timeout errors.

> [!TIP]
> If you experience slowness in the IDE, you can now safely continue working as the background build processes have more "breathing room" to complete their tasks.
