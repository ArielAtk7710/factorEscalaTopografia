# Nuevo Diseño de Diálogo para MAPA (Selector de Datos)

He completado la creación del nuevo diseño de diálogo específico para la pestaña de mapas, incorporando el selector de captura de datos solicitado.

## Mejoras y Adiciones

### 1. Nuevo Layout: `dialog_save_point_map.xml`
Se ha creado una variante del diálogo de guardado estándar optimizada para cartografía:
- **Selector de GPS:** Se añadió un grupo de botones debajo del nombre del punto que permite elegir entre las opciones **"GPS"** y **"SIN GPS"**.
- **Jerarquía Visual:** El selector utiliza un estilo moderno de "Toggle Button" que facilita la decisión rápida en campo.

### 2. Sistema de Estados Inteligente
He implementado selectores de color personalizados para cumplir con tus requisitos estéticos exactos:
- **Bordes:** Se mantienen permanentemente en **Naranja** (`accent_orange`) para resaltar el componente.
- **Estado ACTIVO:** El botón seleccionado cambia a fondo naranja con texto blanco.
- **Estado INACTIVO:** El botón no seleccionado permanece con fondo blanco y texto naranja.

### 3. Recursos Técnicos Incorporados
Para lograr este diseño, se crearon los siguientes archivos de soporte:
- `bg_toggle_selector_map.xml`: Gestiona el intercambio de fondos naranja/blanco.
- `color_toggle_text_map.xml`: Gestiona el intercambio de color de fuente blanco/naranja.

> [!SUCCESS]
> El diseño está listo para ser inflado desde `MapFragment.java`. Los botones utilizan tipografía en negrita (`bold`) y esquinas redondeadas de 8dp para un acabado profesional y ergonómico.

render_diffs(file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/dialog_save_point_map.xml)
