# Plan de Implementación - Corrección de Inicialización de Mapa (Primer Inicio)

Este plan soluciona el problema donde el mapa aparece en blanco (rejilla gris) tras la primera instalación y requiere una configuración manual para activarse. El error se debe a que la configuración crítica de `osmdroid` (User-Agent y rutas) se está aplicando después de que la interfaz de usuario ya ha intentado cargar el mapa.

## User Review Required

> [!IMPORTANT]
> **Cambio de Inicialización**: Moveré la configuración de `osmdroid` al inicio absoluto de `MainActivity.onCreate`. Esto garantiza que desde el primer segundo la app tenga "permiso" de los servidores de mapas para descargar datos.

## Cambios Propuestos

### 1. Inicialización Global y Temprana

#### [MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- **User-Agent Único**: Configurar el `User-Agent` con el nombre del paquete antes de `setContentView`. Sin esto, los servidores de OpenStreetMap bloquean la conexión por seguridad en el primer intento.
- **Rutas Consolidadas**: Establecer las rutas de base y caché de forma global para que todos los fragmentos (Automático y Mapa) compartan la misma configuración.
- **Modo Online por Defecto**: Asegurar que si no hay una preferencia guardada, se fuerce explícitamente el modo Online.

### 2. Sincronización del Motor de Mapas

#### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- **Consistencia de Modos**: Sincronizar los códigos de modo (0: Online, 1: Offline, 2: Híbrido) con los de `MainActivity`.
- **Carga Inteligente**: Al iniciarse, el `MapManager` detectará si el modo es Híbrido y activará la capa satelital automáticamente, o mantendrá Mapnik si es Online.

### 3. Actualización de Rutas en Ajustes

#### [MODIFY] [dialog_settings.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/dialog_settings.xml)
- Actualizar el texto informativo de la ruta para que coincida exactamente con la ubicación técnica: `Android > data > bo.com.factorcombinadotopo > files > osmdroid`.

## Plan de Verificación

### Prueba de "Primera Ejecución" (Simulada)
1.  Limpiar datos de la aplicación o desinstalar/reinstalar.
2.  Abrir la aplicación por primera vez.
3.  Navegar directamente a la pestaña **MAPA**.
4.  **Resultado esperado**: El mapa debe cargar las calles (Online) inmediatamente sin tocar los ajustes.

### Prueba de Robustez
- Verificar que los botones de **Importar** y **Eliminar** mapa en los ajustes siguen apuntando a la carpeta correcta y actualizan la interfaz al instante.
