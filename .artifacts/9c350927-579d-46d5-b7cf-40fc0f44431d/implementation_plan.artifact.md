# Plan de Implementación: Guía Rápida Pro

Este plan detalla la creación de la sección "Guía Rápida", proporcionando al usuario un manual breve, concreto y profesional sobre el uso de todas las herramientas de FactorEscalaTop, manteniendo la estética de la app.

## Proposed Changes

### 1. Recursos de Texto (`strings.xml`)
- **[MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values/strings.xml)**:
    - Añadir `title_quick_guide`: "📖 Guía Rápida de Uso"
    - Añadir los bloques de contenido para cada función:
        - **General:** Resumen de la app.
        - **Cálculos:** Automático vs Manual.
        - **Mapa:** Agujas, Lupa y Altura Online.
        - **Dron:** Semáforo y Ventanas de Vuelo.
        - **Herramientas:** Brújula, Calendario y Libreta.
        - **Datos:** Exportación TXT/JSON.

### 2. Diseño de la Interfaz (`layout_dialog_guide.xml`)
- **[NEW] [layout_dialog_guide.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/layout_dialog_guide.xml)**:
    - Siguiendo el formato de `layout_dialog_about.xml`.
    - **Cabecera:** Título en color **Naranja** (`@color/accent_orange`).
    - **Contenido:** Lista seccionada con títulos en **Azul** (`@color/accent_primary`) y cuerpo en color de texto estándar.
    - **Interactividad:** Uso de `NestedScrollView` para una navegación fluida.
    - **Botón de Cierre:** Estilo naranja con texto "ENTENDIDO".

### 3. Lógica de Activación (`MainActivity.java`)
- **[MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)**:
    - Implementar el método `showQuickGuideDialog()`.
    - Vincular este método al item `action_tutorial` del menú (que ahora se llama Guía Rápida).
    - Utilizar `BottomSheetDialog` para una apertura moderna y táctil.

## Beneficios
- **Onboarding Eficiente:** El usuario nuevo comprende el potencial de la app en menos de 1 minuto.
- **Soporte Técnico Integrado:** Reduce dudas sobre términos complejos (DEM, Kp, etc.).
- **Coherencia Visual:** Refuerza la identidad premium del software.

## Plan de Verificación

### Manual Verification
1.  **Acceso:** Abrir el menú lateral o de opciones y seleccionar "Guía Rápida".
2.  **Visualización:** Confirmar que los colores naranja y azul se aplican correctamente a los títulos.
3.  **Lectura:** Verificar que el texto no esté cortado y el scroll funcione bien.
4.  **Cierre:** Presionar "ENTENDIDO" y confirmar que regresa a la pantalla anterior.
