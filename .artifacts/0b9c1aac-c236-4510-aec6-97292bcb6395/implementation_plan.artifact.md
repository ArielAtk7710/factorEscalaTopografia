# Plan de Implementación: Medición de Áreas y Polígonos en el Mapa

Este plan detalla la adición de una herramienta técnica para dibujar polígonos en el mapa, calcular su área superficial de forma precisa y visualizarla con un estilo profesional (celeste transparente).

## Análisis de Factibilidad
- **Librería osmdroid**: Soporta la clase `Polygon` con personalización de color de relleno y borde.
- **Cálculo de Área**: Implementaremos una función geodésica en `GeoUtils` que utilice el radio de la tierra para calcular el área en metros cuadrados (m²) y hectáreas (ha), asegurando precisión técnica.
- **Interacción**: Se requiere un modo de "Dibujo" donde los toques en el mapa agreguen vértices al polígono actual.

## Cambios Propuestos

### 1. Utilidades Geográficas (GeoUtils)
- **[MODIFY] [GeoUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/GeoUtils.java)**:
    - Añadir método `calculateGeodesicArea(List<GeoPoint> points)` para obtener el área en m².
    - Añadir método `formatArea(double areaM2)` para mostrar el resultado en m² o hectáreas (si supera los 10,000 m²).

### 2. Gestión de Mapas (MapManager)
- **[MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)**:
    - Añadir soporte para gestionar una lista de polígonos activos.
    - Método `addPolygon(List<GeoPoint> points, String areaText)`: Crea el overlay con color celeste transparente (`#4000BFFF`) y coloca un marcador de texto en el centroide con el valor del área.

### 3. Interfaz de Usuario (Layout)
- **[MODIFY] [fragment_map.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_map.xml)**:
    - Añadir un nuevo botón flotante (FAB) con un icono de "Regla" o "Polígono" (Azul Celeste).
    - Añadir un pequeño panel flotante o botones de "Finalizar" y "Cancelar" que solo aparezcan durante el modo de dibujo.

### 4. Lógica del Mapa (MapFragment)
- **[MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)**:
    - Implementar el estado `isDrawingArea`.
    - Capturar clics largos o toques directos (vía un `MapEventsReceiver`) para ir recolectando los puntos del polígono.
    - Al presionar "Finalizar", invocar el cálculo y delegar el dibujo al `MapManager`.

## Plan de Verificación

### Pruebas de Precisión
- Dibujar un cuadrado conocido (ej: una manzana urbana de 100m x 100m) y verificar que el área reportada sea cercana a 10,000 m² (1 ha).

### Pruebas de UI
- Verificar que el polígono sea transparente y permita ver los detalles del mapa (mosaicos ArcGIS) debajo.
- Asegurar que el texto del área sea legible en modo satelital y callejero.

---
**¿Deseas que proceda con la implementación de esta herramienta de medición de polígonos?**
