# Plan de Optimización de Experiencia Offline Total

Este plan detalla las medidas finales para garantizar que la aplicación factorEscala funcione de forma fluida, sin bloqueos y sin cierres repentinos cuando no hay conexión a internet.

## User Review Required

> [!IMPORTANT]
> Se desactivarán proactivamente los servicios de red en el motor de mapas y los buscadores de direcciones (Geocoder) cuando se detecte que el dispositivo está offline. Esto eliminará los "micro-cuelgues" de 1 o 2 segundos causados por el sistema operativo al intentar contactar servidores inexistentes.

## Análisis de Riesgos Offline

### 1. Geocodificador (Nombres de Ciudades)
- **Problema**: El `Geocoder` de Android suele bloquear el hilo durante varios segundos intentando conectar a los servidores de Google.
- **Solución**: Verificar `NetworkUtils.isNetworkAvailable` antes de llamar al servicio. Si no hay red, mostrar directamente "Ubicación Offline".

### 2. Motor de Mapas (osmdroid)
- **Problema**: `osmdroid` intenta resolver URLs de mosaicos incluso sin red, lo que consume batería y puede causar lentitud en la respuesta táctil.
- **Solución**: Configurar `mapView.setUseDataConnection(false)` dinámicamente cuando no haya internet detectado.

### 3. Cálculos de Elevación y Clima
- **Problema**: Las promesas de Retrofit pueden quedar pendientes si el cambio de estado de red es errático.
- **Solución**: Cancelar peticiones activas al detectar pérdida de señal y asegurar que los hilos de cálculo local (Topografía) tengan prioridad absoluta.

## Proposed Changes

### [MODIFY] [CompassFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/CompassFragment.java)
- Añadir guarda de red en `updateLocationName`.

### [MODIFY] [WeatherFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/WeatherFragment.java)
- Añadir guarda de red en `updateLocationName`.

### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- Implementar `updateNetworkState(boolean isOnline)` para activar/desactivar la conexión de datos del mapa en tiempo real.

### [MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- Notificar al `MapManager` sobre los cambios de red en el `networkCallback`.

## Verification Plan

### Manual Verification
1. **Modo Avión Estricto**: Abrir la app en modo avión. Navegar por todas las pestañas. El mapa debe cargar lo que tenga en caché al instante, y la brújula/replanteo deben mostrar datos GPS sin pausas.
2. **Reconexión en Caliente**: Activar Wifi mientras se usa el mapa; verificar que el mapa empieza a descargar nuevas zonas automáticamente.
3. **Estabilidad de Geocoder**: Verificar que en la Brújula aparece "Ubicación Offline" o similar de inmediato al estar sin red.
