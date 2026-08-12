# Plan de Estilo Unificado: Diálogos de Selección y Mensajes

Este plan detalla la reestructuración estética de los diálogos de la aplicación para adoptar el estándar "Premium Transparente" (basado en el diseño de Términos y Condiciones), unificando todos los mensajes y menús bajo una misma identidad visual.

## Proposed Changes

### 1. Interfaz de Diálogo de Selección (`layout_dialog_point_selection.xml`)
- **[NEW] [layout_dialog_point_selection.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/layout_dialog_point_selection.xml)**:
    - **Fondo:** Negro transparente (`#CC000000`).
    - **Título:** Blanco puro, 20sp, negrita, centrado ("Seleccionar Puntos").
    - **Lista:** Uso de `RecyclerView` o `ListView` con items tematizados (texto blanco/gris).
    - **Botonera:**
        - Botón "MOSTRAR": Naranja sólido con texto blanco.
        - Botón "LIMPIAR TODO": Borde naranja (Outlined), texto blanco.
        - Botón "CERRAR": Estilo minimalista.

### 2. Lógica del Fragmento de Mapa (`MapFragment.java`)
- **[MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)**:
    - Reemplazar `showPointSelectionDialog()` por una implementación que infle el nuevo layout transparente.
    - Asegurar que el diálogo ocupe el tamaño adecuado y permita la interacción con la lista de puntos históricos.

### 3. Estandarización de Estilo (Futuros Mensajes)
- Se establece como norma de diseño para el proyecto el uso del fondo `#CC000000` y tipografía blanca para cualquier ventana emergente (Popups, Diálogos de Alerta, etc.), eliminando definitivamente los estilos por defecto de Android.

## Beneficios
- **Uniformidad Total:** El usuario percibirá la app como una suite de alta gama, sin cambios bruscos de estilo entre funciones.
- **Identidad Técnica:** El contraste de blanco sobre negro transparente resalta el carácter de "herramienta de precisión".

## Plan de Verificación

### Manual Verification
1.  **Activación:** Presionar el botón verde (Listado) en el Mapa.
2.  **Visualización:** Confirmar que el fondo es negro transparente y los títulos son blancos, idénticos a los Términos y Condiciones.
3.  **Funcionalidad:** Seleccionar varios puntos, presionar "MOSTRAR" y verificar que se proyecten las agujas naranjas en el mapa.
4.  **Limpieza:** Usar "LIMPIAR TODO" en el nuevo diálogo y confirmar que las marcas desaparecen del mapa.
