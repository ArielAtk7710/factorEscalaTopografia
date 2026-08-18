# Plan de Estabilización del Módulo de Mapas (Fix Crashes)

El usuario reporta que la aplicación se congela y se cierra al entrar a la opción de Mapa. Basado en el análisis del código, se han identificado inconsistencias críticas en la inicialización de `osmdroid`, redundancias en el ciclo de vida y posibles problemas de renderizado de hardware.

## User Review Required

> [!IMPORTANT]
> Se unificará la configuración de los mapas en un solo lugar y se desactivará la aceleración de hardware para el componente de mapa si se detectan problemas de renderizado. Esto garantizará que la app sea estable en dispositivos de gama baja o con versiones de Android recientes.

## Proposed Changes

### Global Configuration (Thread Safety)

#### [MODIFY] [SurveyApplication.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/SurveyApplication.java)
- Mover la carga de configuración de `osmdroid` (`Configuration.getInstance().load`) al hilo principal en `onCreate` para evitar condiciones de carrera.
- Unificar el uso de `SharedPreferences` (usar el predeterminado para coincidir con `MapManager`).

### Map Lifecycle & Resources (Fix Crashes)

#### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- Eliminar `mapView.onResume()` del constructor (es demasiado pronto).
- Eliminar la redundancia de `mapView.onDetach()` en `onDestroy` (ya se llama en el Fragmento).
- Asegurar que `initConfiguration` no sobreescriba configuraciones globales de forma innecesaria en cada recreación.
- Implementar un chequeo de seguridad para `getExternalFilesDir(null)`.

#### [MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- Añadir `mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)` como medida preventiva de estabilidad.
- Corregir el orden de inicialización para asegurar que los eventos del mapa se registren solo después de que el `MapManager` esté listo.

## Verification Plan

### Automated Tests
- Ejecutar `gradle assembleDebug` para asegurar que los cambios no rompan la compilación.

### Manual Verification
1. **Entrada al Mapa**: Verificar que al abrir la pestaña de Mapa, la aplicación no se cierre.
2. **Cambio de Capa**: Alternar entre Mapa de Calles y Satélite; confirmar que los mosaicos carguen sin "congelar" la UI.
3. **Persistencia**: Añadir un punto, salir de la app, volver a entrar y verificar que el marcador se restaure correctamente.
