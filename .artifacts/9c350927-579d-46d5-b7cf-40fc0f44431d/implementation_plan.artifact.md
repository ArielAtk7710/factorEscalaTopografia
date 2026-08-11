# Plan de Creación: Diálogo de Guardado para MAPA (Selector GPS)

Este plan detalla la creación de un nuevo diseño de diálogo específico para la pestaña de mapas, el cual incluirá un selector profesional para decidir si el punto se guarda con o sin información de GPS.

## Proposed Changes

### 1. Recursos Visuales (Estilo del Selector)
- **[NEW] [bg_toggle_selector_map.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/drawable/bg_toggle_selector_map.xml)**:
    - Selector de estado para el fondo de los botones.
    - **Activo:** Fondo Naranja (`@color/accent_orange`).
    - **Inactivo:** Fondo Blanco (`#FFFFFF`).
- **[NEW] [color_toggle_text_map.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/color/color_toggle_text_map.xml)**:
    - Selector de estado para el color del texto.
    - **Activo:** Texto Blanco.
    - **Inactivo:** Texto Naranja.

### 2. Diseño del Diálogo (`dialog_save_point_map.xml`)
- **[NEW] [dialog_save_point_map.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/dialog_save_point_map.xml)**:
    - Basado en el diseño actual de `dialog_save_point.xml`.
    - **Adición:** Debajo del campo "Nombre del Punto", se añadirá un `MaterialButtonToggleGroup` con dos botones: **"GPS"** y **"SIN GPS"**.
    - **Estilo:** Bordes naranjas permanentes, esquinas redondeadas y tipografía técnica.

### 3. Sincronización de Identificadores
- Se mantendrán los IDs `et_point_name`, `et_point_notes`, `btn_dialog_save` y `btn_dialog_cancel` para facilitar su futura implementación en Java.
- Se añadirá el ID `toggle_gps_selection` para el nuevo grupo de botones.

## Plan de Verificación

### Manual Verification
1.  **Renderizado:** Previsualizar el XML y verificar que el selector de GPS aparezca con bordes naranjas.
2.  **Estados:** Verificar visualmente (vía preview) que el botón seleccionado se vea naranja con letras blancas y el no seleccionado blanco con letras naranjas.
3.  **Alineación:** Confirmar que el nuevo componente no rompa la estructura del `CardView`.
