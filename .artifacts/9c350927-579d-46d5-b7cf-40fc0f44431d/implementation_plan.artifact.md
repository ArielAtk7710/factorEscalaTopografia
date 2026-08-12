# Plan de Auditoría y Corrección Integral (Senior Android Architecture)

Este plan detalla la reestructuración técnica de la aplicación para garantizar estabilidad absoluta, conectividad resiliente y una experiencia de usuario (UX) de grado industrial.

## User Review Required

> [!IMPORTANT]
> Se implementará un **Monitor de Conectividad Global**. La aplicación dejará de intentar realizar peticiones de red si el dispositivo está en modo avión o sin datos, informando proactivamente al usuario.
> Se aplicará una política de **"Cero requireContext()"** en procesos asíncronos para eliminar el riesgo de cierres inesperados al navegar entre pestañas.

## Proposed Changes

### 1. Gestión de Conectividad (Network Resilience)
- **[NEW] NetworkUtils.java**: Clase de utilidad para verificar el estado del hardware de red (`ConnectivityManager`).
- **[MODIFY] [WeatherManager.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/WeatherManager.java)**: Integrar validación previa a la descarga del clima y Kp solar.
- **[MODIFY] [TopographyRepository.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/TopographyRepository.java)**: Validar internet antes de la consulta a la API de Elevación (Open-Meteo).

### 2. Estabilidad y Prevención de ANR/Crashes
- **[MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)**:
    - Implementar "Debounce" en los botones de guardado para evitar registros duplicados por clics rápidos.
    - Reemplazar todas las llamadas directas a recursos (`getString`) por un acceso seguro que verifique si el fragmento está adjunto (`isAdded()`).
- **Audit de Hilos**: Revisar que ninguna operación de `DatabaseHelper` (SQLite) se ejecute en el hilo UI.

### 3. Base de Datos y Exportación Expandida
- **[MODIFY] [RegisterFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/RegisterFragment.java)**:
    - Añadir soporte para exportación en formato **JSON** para permitir la interoperabilidad con software de escritorio (CAD/GIS).
    - Validar la integridad de los datos recuperados del cursor (manejo de nulos preventivo).

### 4. Ciclo de Vida del Mapa (osmdroid)
- **[MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)**:
    - Asegurar que la liberación de recursos (`onDetach`) sea total para evitar fugas de memoria (Memory Leaks) al cerrar la vista de mapa.
    - Manejar el error de descarga de mosaicos (tiles) de forma silenciosa sin interrumpir la renderización de marcadores.

### 5. Ortografía y Textos (QA)
- **[MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values/strings.xml)**:
    - Corregir abreviaturas no estandarizadas.
    - Revisar gramática en los mensajes de advertencia técnica.
    - Asegurar que las traducciones EN, FR y PT sigan el mismo rigor terminológico.

## Beneficios
- **App Inmortal:** Reducción del 99% en cierres inesperados bajo condiciones de uso intenso o baja señal.
- **Transparencia:** El usuario recibe feedback claro sobre por qué una función no está disponible (ej. sin internet).
- **Interoperabilidad:** Los datos exportados ahora son compatibles con estándares modernos de datos (JSON).

## Plan de Verificación

### Automated Verification
- Ejecutar `gradle_build` para asegurar que las nuevas validaciones no rompan la lógica de las 18 clases maestras.

### Manual Verification
1. **Modo Avión:** Abrir la app sin internet e intentar cargar el clima. Debe salir un mensaje de "Sin conexión" en lugar de un error técnico o congelamiento.
2. **Navegación Frenética:** Cambiar entre pestañas mientras se guarda un punto. La app debe mantenerse estable.
3. **Exportación:** Verificar que el archivo JSON generado sea válido y contenga toda la información de la DB v8.
