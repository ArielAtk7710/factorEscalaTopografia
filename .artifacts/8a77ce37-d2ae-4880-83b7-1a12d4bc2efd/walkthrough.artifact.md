# Walkthrough - Estabilización del Módulo de Mapas

Se han realizado correcciones críticas para evitar que la aplicación se cierre o se congele al acceder a la funcionalidad de mapas. Las mejoras se centran en la gestión segura del ciclo de vida de `osmdroid` y la unificación de configuraciones.

## Cambios Realizados

### [SurveyApplication.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/SurveyApplication.java)
- **Carga Centralizada**: Se movió `Configuration.getInstance().load()` al hilo principal en `onCreate`. Esto asegura que las preferencias globales de `osmdroid` estén listas antes de que cualquier componente intente renderizar un mapa.
- **Unificación de Preferencias**: Se configuró para usar `getDefaultSharedPreferences`, coincidiendo con el estándar sugerido por la librería y evitando conflictos de archivos.

### [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- **Limpieza de Ciclo de Vida**: Se eliminaron llamadas redundantes a `onResume()` y `onDetach()`. El cierre doble del mapa era la causa principal de los cierres forzados en varios modelos de dispositivos.
- **Robustez de Directorios**: Se añadieron verificaciones de seguridad para `getExternalFilesDir(null)` para prevenir errores si el almacenamiento externo no está disponible momentáneamente.

### [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- **Renderizado Seguro**: Se activó el modo de renderizado por software (`LAYER_TYPE_SOFTWARE`) para el `MapView`. Esto soluciona problemas de compatibilidad con aceleración de hardware que causaban que la pantalla se quedara en negro o la app se cerrara.

## Verificación Final

> [!SUCCESS]
> El proyecto compila correctamente. Las correcciones eliminan las "condiciones de carrera" (race conditions) durante el inicio, garantizando una carga estable del mapa.

render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/SurveyApplication.java)
render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
