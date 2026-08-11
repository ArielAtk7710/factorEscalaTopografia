# Plan de Restauración de Funcionalidad del Mini Mapa y Sugerencia de GPS

Este plan detalla la activación de los controles interactivos del mini mapa en la vista Automático y la mejora del sistema de notificaciones cuando el GPS está desactivado.

## Proposed Changes

### [Módulo: Fragmento Automático (Kotlin)]

#### [MODIFY] [AutomaticFragment.kt](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.kt)
- Configurar los listeners de clic para los botones flotantes (FAB) del mini mapa:
    - **`fab_mini_center_location`**: Llamar a `miniMapManager?.centerOnCurrentLocation()` para centrar la cámara en la posición actual.
    - **`fab_mini_toggle_map_type`**: Llamar a `miniMapManager?.toggleMapType()` para alternar entre mapa callejero y satelital.
- Asegurar que el centrado automático inicial esté activo.

---

### [Módulo: Localización e Idiomas]

#### [MODIFY] [strings.xml (Todos)](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/values/strings.xml)
- Actualizar la etiqueta `msg_activate_gps` para incluir la sugerencia del modo manual:
    - **Español**: "Active el GPS de su dispositivo o use el modo Manual."
    - **Inglés**: "Activate GPS or use Manual mode."
    - **Francés**: "Activez le GPS ou utilisez le mode Manuel."
    - **Portugués**: "Ative o GPS ou use o modo Manual."

## Verification Plan

### Manual Verification
1.  **Mini Mapa**: Activar el mapa, esperar señal y presionar el botón de centrado (naranja) y el de capas (azul). Verificar que funcionen.
2.  **Aviso GPS**: Apagar el GPS desde el sistema Android. Verificar que el aviso diga: *"Active el GPS de su dispositivo o use el modo Manual."*
3.  **Persistencia**: Verificar que al cambiar de pestaña el mapa no pierda su configuración.
