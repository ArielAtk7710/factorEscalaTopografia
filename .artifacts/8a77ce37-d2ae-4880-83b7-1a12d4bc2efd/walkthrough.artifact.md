# Walkthrough: Blindaje Integral de Estabilidad y Seguridad de Hilos

Se ha completado una reingeniería de la gestión de tareas de fondo y del ciclo de vida de la aplicación para eliminar los cierres inesperados (crasheos) en la vista de **MAPA** y mejorar la robustez general del sistema.

## Mejoras de Estabilidad Implementadas

### 1. Eliminación de Hilos "Huérfanos"
- Se reemplazaron todos los usos de `new Thread` por el pool de hilos controlado del `TopographyRepository`.
- **Beneficio**: Evita la saturación de recursos y permite un apagado ordenado de las tareas cuando el usuario sale de la aplicación.

### 2. Seguridad en el Ciclo de Vida (Lifecycle)
- Se añadieron verificaciones estrictas de `isAdded()` y `getContext() != null` en todos los fragmentos (**Mapa**, **Registro**, **Automático**).
- **Resultado**: Si una tarea de fondo termina después de que el usuario cambió de pestaña, la aplicación ya no intentará actualizar una interfaz inexistente, eliminando la causa principal de los cierres.

### 3. Blindaje del Motor de Mapas (`MapManager`)
- **Protección de Ventanas**: Ahora se verifica que el mapa esté realmente "pegado" a la pantalla (`isAttachedToWindow`) antes de intentar abrir etiquetas de información.
- **Gestión de Bitmaps**: Se añadieron salvaguardas para evitar errores de memoria al crear las etiquetas naranjas de los puntos, validando dimensiones antes de procesar imágenes.
- **Sincronización**: Se reforzó el bloqueo de recursos para evitar colisiones cuando el GPS y el usuario interactúan con el mapa al mismo tiempo.

### 4. Captura de Contexto Seguro
- Las tareas de base de datos ahora capturan una referencia local y segura del `Context` al inicio, evitando errores de puntero nulo si el fragmento se desvincula durante la ejecución.

## Verificación Realizada

> [!TIP]
> Puedes realizar una prueba de "estrés" cambiando entre pestañas muy rápidamente. Notarás que la aplicación ahora ignora las actualizaciones de las pestañas que ya no son visibles, manteniendo una navegación fluida y sin interrupciones.

### Checkbox de Robustez:
- [x] Unificación de hilos en `TopographyRepository`.
- [x] Validación de visibilidad en `MapFragment`.
- [x] Prevención de excepciones de Bitmap en `MapManager`.
- [x] Limpieza de observadores en `RegisterFragment`.
