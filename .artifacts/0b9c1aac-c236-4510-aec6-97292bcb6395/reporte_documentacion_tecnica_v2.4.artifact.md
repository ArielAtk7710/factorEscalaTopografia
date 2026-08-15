# Reporte de Documentación Técnica: FactorEscalaTop v2.4

Este documento proporciona una visión detallada de la arquitectura, módulos, clases e interfaces gráficas de la aplicación, sirviendo como guía técnica para el mantenimiento y futuras migraciones.

---

## 1. Arquitectura General del Proyecto

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** coordinado por un repositorio central para garantizar la reactividad y la estabilidad de los datos técnicos.

- **Lenguaje**: Java 17.
- **UI Framework**: Material Design Components 3.
- **Gestión de Estado**: Android Architecture Components (LiveData, ViewModel).
- **Motores Gráficos**: osmdroid (Mapas) y Canvas nativo (Brújula).

---

## 2. Módulos de Interfaz Gráfica (Vistas)

### 2.1 Pantallas Principales (Activities)
- **`SplashScreen.java`**: Gestiona la inicialización asíncrona de los motores técnicos (MGBEngine, SQLite) durante 4.5 segundos. Implementa observación reactiva de `isReady`.
- **`MainActivity.java`**: Host central que contiene el `ViewPager2` para los fragmentos, el `NavigationDrawer` (menú lateral) y el diálogo global de **Ajustes del Sistema**.

### 2.2 Módulos Funcionales (Fragments)
- **`AutomaticFragment.java`**:
    - Realiza cálculos topográficos en tiempo real usando el sensor GPS.
    - Integra el **Diagnóstico de Satélites GNSS** (desglose de ID, señal y constelación).
- **`MapFragment.java`**:
    - Interfaz cartográfica avanzada con soporte para capas ArcGIS (Satelital) y OSM.
    - Herramientas: Medición de Áreas (Celeste), Medición de Distancias (Rojo), Marcado de Puntos.
    - Interactividad: Fichas técnicas (BottomSheet) al tocar nombres de puntos.
- **`ManualFragment.java`**: Simulación técnica de factores ingresando coordenadas fijas (útil para oficina).
- **`RegisterFragment.java`**:
    - Historial técnico de puntos y Libreta de Campo.
    - Exportación masiva o selectiva a formatos **TXT** (Reporte humano) y **JSON** (Intercambio GIS).
- **`CompassFragment.java`**: Brújula de alta precisión con cálculo de Azimut magnético/verdadero y nivel de burbuja digital.
- **`WeatherFragment.java`**: Analizador de seguridad para drones. Evalúa viento, lluvia, visibilidad e índice solar (Kp) en un semáforo de riesgo.
- **`FieldNotebookFragment.java`**: Digitalización de la libreta de campo clásica (Estación, Punto Atrás, Radiación).

---

## 3. Lógica de Negocio y Fórmulas (Core)

### 3.1 Motores Geodésicos (Master Formulas)
- **`TopoCalculoManager.java`**: Clase maestra de cálculo.
    - `calculateAll()`: Calcula Factor de Escala (k), Factor de Altura (ha) y Factor Combinado (K) simultáneamente.
    - Gestiona conversiones UTM y reconstrucciones de altura elipsoidal.
- **`MGBEngine.java`**: Implementación del **Modelo Geoidal Bolivia 2008 (MGBol08)**.
    - Utiliza **NIO (MappedByteBuffer)** para lectura ultra rápida de la grilla binaria de 841x841 puntos.
    - Realiza interpolación bilineal para obtener la ondulación geoidal (N) con precisión centimétrica.
- **`EGM96Engine.java`**: Fallback global para cálculos fuera del territorio boliviano.
- **`GeoUtils.java`**: Utilidades técnicas para formateo de coordenadas (GMS), cálculo de áreas geodésicas y distancias sobre la esfera WGS84.

### 3.2 Analizadores y Conversores
- **`FlightSafetyAnalyzer.java`**: Motor de puntuación (Score-based) que determina la viabilidad de vuelo RPAS/Dron basándose en umbrales técnicos (`FlightSafetyThresholds.java`).
- **`IGM* (Conversores)`**: Familia de clases especializadas en transformaciones de coordenadas (UTM, TM, Lambert, ECEF, ENU).

---

## 4. Gestión de Datos y Concurrencia

### 4.1 Capa de Datos
- **`TopographyRepository.java`**: Singleton que centraliza las peticiones de datos (Elevación API, Temperatura, Cálculos asíncronos). Expone el pool de hilos `runOnBackground()`.
- **`SurveyViewModel.java`**: Almacena el estado de la ubicación y los resultados de cálculo para que sobrevivan a cambios de configuración (rotación).
- **`DatabaseHelper.java`**: Gestor de SQLite local. Maneja las tablas de Puntos y Libreta de Campo con sembrado inicial de datos de ejemplo.

### 4.2 Utilidades de Soporte
- **`MapManager.java`**: Controlador de bajo nivel para `osmdroid`. Gestiona cachés independientes (`tiles_street`, `tiles_sat`), sincronización de Overlays y estilos de dibujo.
- **`FileUtils.java`**: Manejo de exportación de archivos al almacenamiento público (`Documents/FactorEscalaTop`) y cálculo de tamaño de carpetas.
- **`NetworkUtils.java`**: Validación técnica de conectividad (Wi-Fi, Datos, VPN).

---

## 5. Resumen de Flujos Técnicos

1.  **Arranque**: `SplashScreen` carga `MGBol08` en RAM -> `SurveyApplication` configura osmdroid.
2.  **Cálculo**: GPS entrega Lat/Lon -> `TopographyRepository` obtiene N (Grilla local) -> `TopoCalculoManager` aplica fórmulas de reducción al elipsoide -> `SurveyViewModel` actualiza la UI.
3.  **Medición**: `MapFragment` captura clics -> `GeoUtils` aplica fórmula de Haversine o Área Esférica -> `MapManager` dibuja el polígono celeste transparente.
