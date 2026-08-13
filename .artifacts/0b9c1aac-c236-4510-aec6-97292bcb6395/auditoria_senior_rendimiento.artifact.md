# Informe de Auditoría Senior: Rendimiento, Estabilidad y QA

**Proyecto:** FactorEscalaTop
**Rol:** Senior Android Engineer & QA Specialist
**Fecha:** 12 de Agosto, 2026

Este informe presenta un análisis profundo de la arquitectura técnica, detectando riesgos críticos de estabilidad y proporcionando una estrategia de pruebas exhaustiva para asegurar una versión de producción robusta.

---

## 1. Análisis de Riesgos Críticos Detectados

### A. Riesgo de ANR (Application Not Responding)
*   **Operaciones de Base de Datos**: En `MapFragment.java` y `AutomaticFragment.java`, la inserción de puntos (`db.insertarPunto`) se realiza en el hilo principal. Si la base de datos crece o el I/O del disco es lento, la UI se bloqueará.
*   **Gestión de Archivos (Caché)**: En `MainActivity.java`, el método `updateCacheSizeUI()` utiliza `FileUtils.getFolderSize()`, que recorre recursivamente carpetas en el hilo principal. Con miles de mosaicos (tiles), esto provocará un lag severo al abrir Ajustes.
*   **Inicialización Síncrona**: En `SurveyApplication.java`, la carga de `MGBEngine` y el sembrado de la base de datos se realizan mediante un `ExecutorService`, lo cual es correcto, pero `MapManager` intenta acceder a la configuración de `osmdroid` antes de asegurar que la carpeta base esté lista.

### B. Fugas de Memoria (Memory Leaks)
*   **MapManager Context**: `MapManager` mantiene una referencia final al `Context`. Si este contexto es una `Activity` y el `MapManager` persiste (ej. en una variable estática o hilo de fondo), la actividad nunca será liberada.
*   **Lifecycle en MapFragment**: Se observa el uso de `mapView.onDetach()` en `onDestroyView`, lo cual es correcto para `osmdroid`. Sin embargo, los `BroadcastReceiver` de estado de GPS deben ser desregistrados meticulosamente en todos los fragmentos para evitar fugas del `Context`.

### C. Concurrencia y Race Conditions
*   **Cambio de Modo GPS/Online**: Al alternar rápidamente entre "Altura GPS" y "Altura Online", se disparan peticiones asíncronas al repositorio. Si el usuario presiona "Guardar" mientras ambas están en vuelo, podría persistirse un dato inconsistente o dispararse dos diálogos de progreso.
*   **Callbacks Post-Destrucción**: En `TopographyRepository.java`, los callbacks de red no siempre verifican si el fragmento que los llamó sigue "vivo" (`isAdded()`) antes de intentar inflar vistas o mostrar Toasts, lo que causaría un `IllegalStateException`.

---

## 2. Plan de Pruebas de Estrés y Rendimiento

### Escenario 1: Stress de Ciclo de Vida (Fugas de Memoria)
*   **Objetivo**: Verificar que el mapa no sature la RAM.
*   **Pasos**:
    1. Abrir la pestaña Mapa.
    2. Navegar rápidamente a la pestaña Registro y volver al Mapa (repetir 20 veces).
    3. Usar **LeakCanary** o el **Memory Profiler** de Android Studio para observar si el heap crece indefinidamente.
*   **Riesgo**: El `MapView` de osmdroid es propenso a retener memoria si no se llama a `onPause/onResume/onDetach` coordinadamente.

### Escenario 2: Carga Pesada de Marcadores
*   **Objetivo**: Evaluar fluidez del UI Thread.
*   **Pasos**:
    1. Inyectar 500 puntos de prueba en la base de datos.
    2. Usar la función "Ver Lista" en el mapa y seleccionar "Mostrar Todo".
    3. Realizar desplazamientos (scroll) y zoom rápidos sobre los marcadores.
*   **Resultado Esperado**: El mapa debe mantener al menos 60 FPS. Si hay "jank", se debe implementar *Marker Clustering*.

---

## 3. Pruebas de Casos Límite (Edge Cases)

### Escenario 3: Interrupción de Conectividad (Modo Avión/Túnel)
*   **Objetivo**: Probar la robustez de "Altura Online".
*   **Pasos**:
    1. Iniciar una petición de "Altura Online DEM".
    2. Activar Modo Avión inmediatamente después de hacer clic.
*   **Riesgo**: Si el timeout no está bien configurado o el callback de error no maneja el `null`, la app lanzará un NPE (NullPointerException).

### Escenario 4: Desactivación de GPS en Caliente
*   **Objetivo**: Verificar el manejo de permisos y sensores.
*   **Pasos**:
    1. Con la app en la pestaña "Automático", bajar la cortina de notificaciones y apagar el GPS.
    2. Volver a la app.
*   **Resultado Esperado**: El borde del panel de coordenadas debe cambiar a ROJO instantáneamente y los valores deben pasar a "--" sin crashear.

---

## 4. Matriz de Criterios de Aceptación (Checklist QA)

- [ ] **Estabilidad**: No crasheos al rotar la pantalla en ninguna vista.
- [ ] **Consistencia**: El nombre del modelo geoidal es siempre "MGBol08".
- [ ] **Rendimiento**: El diálogo de Ajustes abre en menos de 300ms (Cálculo de caché asíncrono).
- [ ] **Seguridad de Datos**: Los registros se guardan correctamente incluso si se sale de la app inmediatamente después de dar a "Guardar".
- [ ] **Offline**: Los mapas en caché son visibles después de apagar el Wi-Fi/Datos.

---

## 5. Recomendaciones de Ingeniería (Acción Inmediata)

1.  **Thread Isolation**: Mover `db.insertarPunto` a un hilo de fondo en el `TopographyRepository`.
2.  **Safe Callbacks**: Implementar una verificación de `WeakReference` o `isAdded()` en todos los callbacks de red.
3.  **Bitmap Recycling**: Asegurar que los iconos de los marcadores (`ic_map_needle_pin`) se carguen una sola vez y se reutilicen, en lugar de decodificarlos en cada actualización de GPS.

---
**¿Deseas que proceda con la implementación de estas correcciones de estabilidad y rendimiento?**
