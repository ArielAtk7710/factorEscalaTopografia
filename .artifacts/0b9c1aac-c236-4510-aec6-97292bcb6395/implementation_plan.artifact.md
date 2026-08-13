# Plan de Optimización Técnica: Rendimiento y Estabilidad Senior

Este plan aborda las vulnerabilidades críticas detectadas en la auditoría de rendimiento y estabilidad, enfocándose en la descarga del hilo principal (UI Thread), la robustez de los mapas y el manejo seguro del ciclo de vida.

## 1. Optimización de Hilos (Background Processing)

### [TopographyRepository](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/TopographyRepository.java)
- Exponer el `ExecutorService` o crear un método `runOnBackground(Runnable)` para centralizar las tareas asíncronas de la app.

### [AutomaticFragment](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java) & [MapFragment](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- Envolver las llamadas a `db.insertarPunto(v)` en hilos secundarios para evitar bloqueos durante el guardado.

### [MainActivity](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- Refactorizar `updateCacheSizeUI()`: El cálculo de tamaño de carpetas se moverá a un hilo de fondo. Se usará un estado de "Calculando..." en la UI mientras termina.
- Refactorizar `clearMapCache()`: La eliminación física de archivos se realizará en segundo plano.

---

## 2. Robustez en Mapas

### [MapManager](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- **Sincronización**: Envolver el acceso a `mapView.getOverlays()` en bloques sincronizados o usar iteradores seguros en `removeVisualMarkers` y `restoreMarkers` para evitar `ConcurrentModificationException`.
- **Estado de Carga**: Añadir una bandera `isMapInitialized` para ignorar clics o actualizaciones antes de que la configuración base termine.

---

## 3. Manejo del Ciclo de Vida (Lifecycle Safety)

### [MapFragment](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- **Gestión de Diálogos**: Mantener una referencia a `progressDialog` y asegurar su cierre en `onDestroyView()` o `onPause()`. Esto evita que la app intente cerrar un diálogo de una actividad que ya no existe (BadTokenException).

---

## Plan de Verificación

### Pruebas de Estrés
- Abrir Ajustes y presionar "Limpiar" repetidamente mientras se navega el mapa.
- Rotar la pantalla 10 veces seguidas durante una operación de guardado.

### Monitoreo de Logs
- Verificar en Logcat que no aparezcan mensajes de "Skipped frames" relacionados con I/O de archivos o base de datos.
