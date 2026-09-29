# Plan de Implementación: Exportación de Puntos Topográficos a Google Maps, QGIS, ArcGIS y CAD (KML, CSV, GeoJSON, DXF y GPX)

## 1. Visión General y Objetivos
Añadir capacidades avanzadas de exportación de datos topográficos en **factorEscala** para permitir la compatibilidad nativa con:
1. **Google Maps / Google Earth:** Formato **KML** (Keyhole Markup Language) con globos de información formateados (Nombre, Coordenadas UTM, Altura, Factores de Escala/Combinado, Modelo Geoidal, Precisión y Fecha).
2. **QGIS / ArcGIS / Microsoft Excel:** Formato **CSV** con encabezados estandarizados de topografía (`ID`, `Nombre`, `Latitud`, `Longitud`, `Este`, `Norte`, `Zona`, `Hemisferio`, `Altura_Elipsoidal`, `Altura_Ortometrica`, `Factor_Escala`, `Factor_Altura`, `Factor_Combinado`, `Modelo_Geoidal`, `Precision`, `Fecha`, `Notas`).
3. **WebGIS / Sistemas de Información Geográfica Modernos:** Formato **GeoJSON** estandarizado con propiedades geográficas completas.
4. **AutoCAD / Civil 3D / Global Mapper:** Formato **DXF 3D** (Drawing Exchange Format) con entidades `POINT` en coordenadas planas UTM ($X = \text{Este}$, $Y = \text{Norte}$, $Z = \text{Cota}$) y textos descriptivos en capas separadas (`PUNTOS_NOMBRE`, `PUNTOS_COTA`).
5. **GPS Garmin / Navegadores:** Formato **GPX** (GPS Exchange Format) para waypoints.

---

## 2. Componentes a Crear y Modificar

### A. Clase Helper de Exportación (`ExportUtils.java`)
- **[NEW] [ExportUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/ExportUtils.java)**
  - `generateKml(List<Punto> puntos)`: Genera estructura XML KML con placemarks, estilos de pines y tabla HTML descriptiva de parámetros topográficos.
  - `generateCsv(List<Punto> puntos)`: Genera archivo CSV con separadores por coma o punto y coma y delimitado seguro para Excel/QGIS/ArcGIS.
  - `generateGeoJson(List<Punto> puntos)`: Genera objeto FeatureCollection GeoJSON para GIS.
  - `generateDxf(List<Punto> puntos)`: Genera archivo de dibujo AutoCAD DXF 3D R12/2000 legible sin librerías externas.
  - `generateGpx(List<Punto> puntos)`: Genera XML GPX estándar para navegación GPS.

### B. Gestor de Almacenamiento de Archivos (`FileUtils.java`)
- **[MODIFY] [FileUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/FileUtils.java)**
  - Añadir soporte para guardar archivos con diferentes extensiones y tipos MIME (`application/vnd.google-earth.kml+xml`, `text/csv`, `application/geo+json`, `image/vnd.dxf`, `application/gpx+xml`, `application/json`, `text/plain`).

### C. Interfaz de Usuario y Diálogo de Exportación (`dialog_export_options.xml` & `RegisterFragment.java`)
- **[MODIFY] [dialog_export_options.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/dialog_export_options.xml)**
  - Rediseñar el diálogo para ofrecer botones claros con íconos para cada formato:
    - 📍 Google Maps / Earth (.KML)
    - 📊 QGIS / ArcGIS / Excel (.CSV)
    - 🌍 GIS Vectorial (.GeoJSON)
    - 📐 AutoCAD / Civil 3D (.DXF)
    - 🛰️ GPS Garmin / Navegador (.GPX)
    - 📄 Texto plano (.TXT)
    - 💻 Estructurado (.JSON)
- **[MODIFY] [RegisterFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/RegisterFragment.java)**
  - Conectar los eventos del nuevo diálogo para invocar `ExportUtils` según el formato seleccionado tanto para exportar todo el historial como para exportar puntos seleccionados.

---

## 3. Plan de Verificación

### Pruebas Automatizadas y de Compilación
- Ejecutar `gradle_build("app:assembleDebug")` para validar la compilación limpia del proyecto.

### Pruebas de Formato y Estructura
- Probar la generación de cada formato (KML, CSV, GeoJSON, DXF, GPX, TXT, JSON) con puntos de prueba.
- Validar que los archivos exportados se guarden en `Documents/FactorEscalaTop/` y se puedan abrir en Google Maps, Google Earth, QGIS, ArcGIS y AutoCAD.
