# Plan Maestro: v2.5 - Estabilidad, Temas y Modo Offline

Este plan detalla las acciones técnicas para asegurar la compatibilidad total de temas (Claro/Oscuro), restringir la orientación de pantalla y robustecer el funcionamiento sin conexión a internet (Offline).

## 1. Compatibilidad de Temas (Claro/Oscuro)

### Auditoría Visual
- **Problema**: Algunos diálogos y celdas utilizan colores hexadecimales hardcoded (ej: `#FFFFFF`, `#1B1E23`) que no cambian al alternar el tema.
- **Acción**: Reemplazar todos los colores fijos en los layouts XML por recursos semánticos:
    - `@color/bg_main` para fondos de pantalla.
    - `@color/bg_surface` para tarjetas y diálogos.
    - `@color/text_primary` y `@color/text_secondary` para tipografía.
- **Acción**: Verificar que `values-night/colors.xml` tenga los contrastes adecuados para el "Modo Oscuro" forzado.

## 2. Restricciones Globales de UI

### Orientación de Pantalla
- **[MODIFY] [AndroidManifest.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/AndroidManifest.xml)**:
    - Añadir `android:screenOrientation="portrait"` a `MainActivity` y `SplashScreen`. Esto evitará reinicios accidentales de sensores y mapas al inclinar el dispositivo.

### Modo Oscuro por Defecto
- Asegurar que en el primer arranque, antes de que el usuario elija nada, la app inicie en modo oscuro (`AppCompatDelegate.MODE_NIGHT_YES`).

## 3. Estrategia de Funcionamiento Offline

### Detección y Mensajería
- **Asistente de Vuelo (Clima)**: Si no hay red, mostrar un mensaje claro: "Esta función requiere conexión a Internet para el análisis meteorológico".
- **Altura Online (DEM)**: Si falla la conexión, activar automáticamente el **Respaldo de Altura GPS** informando al usuario: "Sin conexión. Utilizando sensor GPS local como respaldo".

### Robustez de Callbacks (Estabilidad)
- **Acción**: Revisar todos los hilos secundarios en `WeatherFragment`, `MapFragment` y `AutomaticFragment`.
- **Protección**: Envolver cada interacción con la UI en una verificación `if (isAdded() && getContext() != null)`. Esto evitará el 90% de los cierres inesperados (Crashes) por fragmentos destruidos.

## Plan de Ejecución

1.  **Fase 1**: Modificar `AndroidManifest.xml` para bloquear la rotación.
2.  **Fase 2**: Auditoría masiva de colores en layouts (Limpieza de hardcoded hex).
3.  **Fase 3**: Implementar checks de `NetworkUtils` en los disparadores de Clima y Elevación.
4.  **Fase 4**: Blindaje de estabilidad con `isAdded()` en callbacks asíncronos.

---
## Verificación Final

- **Prueba Offline**: Iniciar la app en Modo Avión y verificar que no se cuelgue al entrar a Clima o intentar guardar un punto en el mapa.
- **Prueba de Tema**: Cambiar el tema del sistema a "Claro" y verificar que la app siga siendo legible (o se mantenga en oscuro si el usuario lo prefiere).

> [!IMPORTANT]
> Estas medidas transforman la aplicación de una herramienta de consumo en una **Herramienta de Grado Industrial**, capaz de operar en zonas remotas sin señal y sin riesgos de bloqueo.
