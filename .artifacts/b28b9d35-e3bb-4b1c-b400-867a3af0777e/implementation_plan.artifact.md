# Plan de Implementación - Mejoras de Precisión y Rendimiento Visual (v2.2)

Este plan detalla dos mejoras específicas para elevar la calidad técnica y la experiencia de usuario (UX) en el trabajo de campo, ajustando el umbral de precisión a 10 metros.

## Cambios Propuestos

### 1. Semáforo de Precisión GPS

#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java)
Implementaremos una lógica visual en el campo de precisión para alertar al topógrafo sobre la calidad de la señal:
- **Excelente (< 5m)**: Texto en color Verde (`state_success`).
- **Buena (5m - 10m)**: Texto en color Amarillo/Naranja (`state_warning`).
- **Baja (> 10m)**: Texto en color Rojo (`state_error`).
- Se añadirá una etiqueta descriptiva junto al valor (ej: "± 3m - Excelente").

### 2. Optimización de Carga Instantánea del Mapa

#### [MODIFY] [MapFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapFragment.java)
- **Eliminación del Delay**: Quitaremos el retraso de 300ms en `onResume`.
- **Sincronización Directa**: Llamaremos a `refreshMap()` de forma directa al entrar en la pestaña.

#### [MODIFY] [MapManager.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MapManager.java)
- Ajustar `refreshMap()` para una respuesta inmediata.

## Plan de Verificación

### Verificación de Precisión
1. Simular diferentes niveles de precisión GPS.
2. Confirmar que el color y la etiqueta cambian correctamente al cruzar los umbrales de 5m y 10m.

### Verificación de Rendimiento
1. Cambiar rápidamente entre pestañas.
2. Confirmar que el mapa carga sin retrasos perceptibles.
