# Plan de Implementación: Etiquetas de Nombre en Pines del Mapa

Este plan detalla la adición de etiquetas visuales (cuadros naranjas con el nombre) sobre los pines manuales del mapa para identificar los puntos guardados de forma inmediata.

## Proposed Changes

### 1. Diseño de la Etiqueta (`layout_marker_label.xml`)
- **[NEW] [layout_marker_label.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/layout_marker_label.xml)**:
    - Crear un diseño minimalista consistente en un `TextView` con:
        - Fondo: Naranja (`@color/accent_orange`).
        - Texto: Blanco, negrita, tamaño pequeño (10sp - 11sp).
        - Bordes: Redondeados (4dp - 6dp).
        - Padding: Ajustado para que parezca un cuadro pequeño.

### 2. Motor de Gestión de Etiquetas (`MapManager.java`)
- **[MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)**:
    - Actualizar `addManualMarker(IGeoPoint point)` $\rightarrow$ `addManualMarker(IGeoPoint point, String name)`.
    - Implementar una clase personalizada que extienda de `MarkerInfoWindow` para usar el nuevo layout.
    - Configurar el marcador para que muestre su ventana de información (`showInfoWindow()`) automáticamente al ser creado.

### 3. Sincronización de Guardado (`MapFragment.java`)
- **[MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)**:
    - Actualizar la llamada final en `persistirPuntoMapa` para pasar el nombre del punto al método del gestor de mapas.

## Beneficios
- **Identificación Inmediata:** El topógrafo puede ver el nombre de sus puntos de control sin necesidad de hacer clic en cada uno.
- **Claridad Visual:** El cuadro naranja resalta sobre el mapa de fondo (especialmente en modo satélite).

## Plan de Verificación

### Manual Verification
1.  **Guardar Punto:** Marcar un punto en el mapa, ponerle el nombre "PC-01" y guardarlo.
2.  **Visualización:** Verificar que aparezca el pin naranja y, justo encima de él, un recuadro naranja con el texto "PC-01" en blanco.
3.  **Navegación:** Desplazar el mapa y verificar que la etiqueta se mueve solidariamente con el pin.
4.  **Múltiples Etiquetas:** Guardar varios puntos seguidos y confirmar que cada uno mantiene su etiqueta con el nombre correspondiente.
