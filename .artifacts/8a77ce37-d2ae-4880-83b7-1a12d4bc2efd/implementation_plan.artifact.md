# Plan de Implementación: Botón de Información en la Vista de Mapa

Este plan describe la adición de un botón de información profesional en la esquina superior derecha del Mapa, permitiendo a los usuarios acceder a una guía rápida sobre las funciones y herramientas disponibles en esta vista.

## User Review Required

> [!IMPORTANT]
> Se añadirá un botón redondo azul con un icono blanco "i" en la parte superior derecha, siguiendo el estándar visual de las otras herramientas de ingeniería para mantener la coherencia.

---

## Proposed Changes

### 1. Recursos de Texto (Strings)

#### [MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values/strings.xml)
Añadir la guía detallada del mapa:
- `guide_map_functions_title`: "GUÍA DE HERRAMIENTAS DE MAPA"
- `guide_map_functions_body`: Explicación de los botones:
    - **Búsqueda (Lupa)**: Navegar a coordenadas específicas.
    - **Medición (Polígono/Regla)**: Cálculo de áreas y distancias con marcado de mira.
    - **Limpiar (Escoba)**: Reseteo de mediciones visuales.
    - **Marcado (Pin)**: Guardar el punto central en los registros.
    - **Capas (Mapa)**: Alternar entre vista Satelital y Calles.

### 2. Interfaz de Usuario (Layouts)

#### [MODIFY] [fragment_map.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/fragment_map.xml)
- Añadir un `ImageView` con el ID `btn_map_help`.
- **Estilo**: Redondo, fondo azul (`accent_primary`), icono blanco.
- **Posición**: Arriba a la derecha con un margen de 16dp.

### 3. Lógica de Fragmento (Java)

#### [MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- Vincular el botón en `onViewCreated`.
- Implementar la llamada a `UIUtils.showProInfoDialog` para mostrar la guía con formato HTML.

---

## Verification Plan

### Manual Verification
1.  Abrir la vista de **Mapa**.
2.  Verificar que aparezca el botón `(i)` en la esquina superior derecha.
3.  Pulsar el botón y confirmar que se abra el diálogo con el título naranja y la descripción técnica de los botones.
4.  Asegurar que el botón sea legible tanto en tema claro como oscuro.

**¿Deseas que proceda con la adición de esta guía de funciones en el mapa?**
