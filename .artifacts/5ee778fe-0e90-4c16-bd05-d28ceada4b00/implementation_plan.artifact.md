# Fix missing `msg_weather_offline` string resource

The project fails to compile because the `msg_weather_offline` string resource is missing from the `strings.xml` files, but it is referenced in the Java code.

## User Review Required

> [!IMPORTANT]
> I will be adding a new string resource to both the default (Spanish) and English `strings.xml` files.

## Proposed Changes

### [Component Name]

I will modify the following resource files to include the missing string.

#### [MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values/strings.xml)
Adding: `<string name="msg_weather_offline">El servicio de clima no está disponible sin conexión.</string>`

#### [MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values-en/strings.xml)
Adding: `<string name="msg_weather_offline">Weather service is not available offline.</string>`

## Verification Plan

### Automated Tests
- I will run `./gradlew :app:compileDebugJavaWithJavac` to verify that the compilation error is resolved.

### Manual Verification
- N/A (UI change is minimal, just preventing a crash/error)
