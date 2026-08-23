# Reporte Técnico: Análisis de Crasheo en Vista MAPA

Tras un análisis exhaustivo del flujo de navegación y del ciclo de vida de los componentes `MapFragment` y `MapManager`, se han identificado múltiples puntos críticos que podrían estar provocando el cierre inesperado de la aplicación al interactuar o navegar hacia la vista de mapa.

## Diagnóstico de Causas Probables

### 1. Inicialización Prematura de Sensores (SecurityException)
> [!CAUTION]
> **Riesgo:** ALTO
> El `MapManager` inicializa `GpsMyLocationProvider` en su constructor. Debido a que el `ViewPager2` en `MainActivity` tiene un `offscreenPageLimit` de 1, el fragmento de mapa se instancia e intenta acceder al GPS incluso cuando el usuario está en la pestaña "Manual" o "Automático", posiblemente antes de que se hayan concedido los permisos de ubicación o antes de que el usuario acepte los términos y condiciones.

### 2. Violación de Hilo en Popups (BadTokenException)
> [!WARNING]
> **Riesgo:** MEDIO-ALTO
> En el método `restoreMarkers()`, que se ejecuta en `onResume()`, se llama a `marker.showInfoWindow()`. Si el `MapView` aún no está completamente "adjunto" (attached) a la ventana de Android o no ha terminado de calcular su layout, el sistema operativo rechaza la apertura del popup de información, provocando un crash por token de ventana inválido.

### 3. Fuga de Memoria y Colisión de Base de Datos
> [!NOTE]
> **Riesgo:** MEDIO
> `MapManager` recrea el `TileProvider` cada vez que se cambia a modo Satélite. `osmdroid` utiliza una base de datos SQLite interna para la caché (`SqlTileWriter`). Si la instancia anterior no se cerró correctamente al destruir la vista del fragmento, el sistema puede bloquear el archivo de caché, provocando un fallo nativo al intentar abrirlo desde la nueva instancia.

### 4. Condición de Carrera en Aplicación (SurveyApplication)
> [!IMPORTANT]
> El servicio `MGBEngine` (Motor Geoidal) se carga de forma asíncrona en un `ExecutorService`. Si el usuario navega rápidamente al mapa o activa una función de cálculo antes de que `_isReady` sea `true`, el repositorio topográfico podría intentar acceder a una grilla nula.

## Puntos de Mejora Propuestos (Sin Modificar Aún)

1.  **Diferir la inicialización del GPS:** No activar el `MyLocationNewOverlay` hasta que el fragmento sea realmente visible y los permisos estén confirmados.
2.  **Protección de Popups:** Asegurar que `marker.showInfoWindow()` solo se llame si `mapView.isAttachedToWindow()`.
3.  **Gestión de Ciclo de Vida:** Limpiar explícitamente los observadores y cerrar el proveedor de mosaicos en `onDestroyView`.
4.  **Sincronización de Servicios:** Bloquear la interacción con el mapa hasta que `SurveyApplication.isReady` emita `true`.

**¿Deseas que proceda con la creación de un plan de corrección basado en estos hallazgos?**
