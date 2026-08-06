# Plan de Implementación - Activación de Mapas Offline (.mbtiles)

Este plan asegura que el archivo `bolivia.mbtiles` descargado sea reconocido por el motor de mapas y funcione correctamente en los modos **Offline** e **Híbrido**.

## Análisis Técnico
Actualmente, `MapManager` configura la caché de internet, pero osmdroid no escanea automáticamente la carpeta `Mapas` en busca de archivos de archivo (`.mbtiles` o `.sqlite`) a menos que se configure explícitamente el "Base Path" o se añadan manualmente los proveedores de archivos.

## Cambios Propuestos

### 1. Motor de Mapas (MapManager)

#### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- **Configuración de BasePath**: Configurar `Configuration.getInstance().setOsmdroidBasePath()` para que apunte a la carpeta donde se encuentra la subcarpeta `Mapas`.
- **Detección de Archivos**: Implementar una lógica en `initConfiguration` que:
    1.  Verifique si hay archivos `.mbtiles` o `.sqlite` en la carpeta `Mapas`.
    2.  Si existen, configurar el proveedor de mosaicos para que priorice estos archivos antes de intentar descargar de internet.
- **Soporte Offline Estricto**: En modo Offline (`mapMode == 1`), asegurar que el motor solo lea del archivo local.

### 2. Estructura de Carpetas (Ajuste Interno)
Para que osmdroid detecte automáticamente los archivos sin código complejo, la carpeta debe llamarse internamente `osmdroid`. Ajustaremos la lógica para que sea transparente para el usuario.

## Plan de Verificación

### Verificación en Dispositivo
1.  Copiar `bolivia.mbtiles` a la carpeta indicada.
2.  Entrar a **Ajustes** y seleccionar modo **Offline**.
3.  Ir a la pestaña **MAPA**.
4.  **Resultado esperado**: El mapa de Bolivia debe cargar instantáneamente sin necesidad de WiFi o Datos móviles.
5.  Repetir en modo **Híbrido**: El mapa debe mostrar los archivos locales y descargar las etiquetas (nombres de calles) de internet si hay conexión.

## Instrucción Crítica para el Usuario
Para asegurar el funcionamiento, el archivo debe estar en:
`Android/data/bo.com.factorcombinadotopo/files/osmdroid/bolivia.mbtiles`
(Ajustaremos la app para que use esta ruta estándar de osmdroid).
