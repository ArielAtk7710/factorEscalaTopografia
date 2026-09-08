# Walkthrough: Guía de Funciones Integrada en el Mapa

Se ha añadido un sistema de ayuda técnica directamente en la vista de Mapa, permitiendo a los usuarios comprender rápidamente el propósito de cada herramienta profesional disponible.

## Mejoras Realizadas

### 1. Botón de Ayuda Premium
- Se integró un botón redondo de información `(i)` en la esquina superior derecha del mapa.
- **Diseño**: Fondo Azul Pro (`accent_primary`) con icono blanco, manteniendo la estética de las herramientas de ingeniería.
- **Accesibilidad**: Ubicado estratégicamente para no interferir con los controles de navegación, pero ser accesible al instante.

### 2. Documentación Técnica del Mapa
Se redactó una guía detallada (disponible en **Español e Inglés**) que explica las funciones clave:
- **Lupa**: Navegación precisa por coordenadas.
- **Escoba**: Reseteo de capas visuales.
- **Mediciones**: Uso del nuevo botón **MARCAR** con la mira naranja.
- **Gestión de Puntos**: Cómo guardar y visualizar el historial de registros sobre la cartografía.
- **Capas**: Alternancia entre los motores de OSM (Calles) y Esri (Satélite).

### 3. Diálogo de Información Unificado
- La guía se muestra utilizando el motor `showProInfoDialog`, asegurando que el diseño (bordes de 24dp, título naranja) sea idéntico al resto de la aplicación, reforzando la identidad de marca "FactorEscalaTop".

## Verificación Visual

> [!TIP]
> Al pulsar el botón `(i)` en el mapa, notarás que la explicación utiliza negritas y colores para resaltar los iconos físicos que el usuario ve en pantalla, facilitando el aprendizaje de la herramienta en segundos.

### Resultados:
- [x] Botón de ayuda visible y funcional.
- [x] Contenido técnico redactado y traducido (Bilingüe).
- [x] Coherencia visual con el estándar de diálogos de la app.
- [x] Mejora en la curva de aprendizaje para nuevos usuarios.
