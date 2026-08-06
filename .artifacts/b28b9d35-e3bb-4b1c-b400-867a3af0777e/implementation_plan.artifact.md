# Plan de Implementación - Corrección de Persistencia en Vista de Mapa

Este plan soluciona el problema donde el mapa aparece vacío (rejilla gris) o pierde la ubicación al regresar a la pestaña de **MAPA** desde otras secciones. El error se debe a una pérdida de estado en los hilos de renderizado de `osmdroid` durante el ciclo de vida del `ViewPager2`.

## Cambios Propuestos

### 1. Robustez en el Ciclo de Vida del Fragmento

#### [MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- **Re-suscripción de Ubicación**: Asegurar que en cada `onResume`, el `locationHelper` fuerce una actualización inmediata de la última posición conocida.
- **Refresco de Capa**: Implementar una llamada al método `refresh()` del `MapManager` para forzar al motor del mapa a reconectarse a los servidores de mosaicos.
- **Manejo de Memoria**: Ajustar la limpieza en `onDestroyView` para evitar que el `onDetach()` bloquee futuras inicializaciones.

### 2. Optimización del Motor de Mapas

#### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- **Método `refresh()`**: Crear un método que re-asigne la fuente de mosaicos (`TileSource`) actual. Esto es un "truco" técnico efectivo en `osmdroid` para reiniciar los hilos de descarga que se hayan quedado inactivos por el cambio de pestañas.
- **Sincronización de Preferencias**: Asegurar que `initConfiguration` sea redundante y fuerce la política de conexión a datos cada vez que la vista se recrea.
- **Protección de Overlays**: Verificar que el `locationOverlay` se re-vincule correctamente si la instancia del mapa ha cambiado.

### 3. Ajuste de Navegación Global

#### [MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- **ViewPager Offscreen Limit**: Configurar `mViewPager.setOffscreenPageLimit(1)`. Esto mantendrá la pestaña de Mapa "viva" en memoria aunque el usuario esté en la pestaña de al lado, evitando que Android destruya la vista del mapa constantemente y mejorando la velocidad de respuesta.

## Plan de Verificación

### Prueba de Navegación
1. Abrir la app e ir a **MAPA** (Verificar que carga).
2. Ir a **AUTOMÁTICO** o **REGISTRO**.
3. Regresar a **MAPA**.
4. **Resultado esperado**: El mapa debe aparecer instantáneamente con los mismos niveles de zoom y la ubicación (punto azul) activa.

### Verificación de Estabilidad
- Cambiar de pestaña rápidamente varias veces y confirmar que el mapa no se queda bloqueado en blanco.
- Verificar que el consumo de batería no se eleve (el mapa se pausará correctamente al salir de la app, pero se mantendrá listo al cambiar de pestañas).
