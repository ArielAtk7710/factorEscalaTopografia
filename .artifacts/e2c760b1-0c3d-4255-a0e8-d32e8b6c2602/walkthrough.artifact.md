# Unificación de Notificaciones (Toasts) Estilizadas

He unificado todos los mensajes de notificación de la aplicación para que utilicen el diseño personalizado y la paleta de colores de la marca, eliminando los Toasts genéricos de Android.

## Mejoras Implementadas

### 1. Centralización en `UIUtils`
- Creé métodos especializados en [UIUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/UIUtils.java) para cada tipo de interacción:
    - **Éxito (Verde):** Confirmación de guardado, eliminación y copiado.
    - **Error (Rojo):** Avisos de fallos técnicos o validaciones críticas.
    - **Información (Azul):** Detalles de ruta de archivos y avisos operativos.
    - **Advertencia (Naranja):** Avisos de GPS desactivado y campos incompletos.

### 2. Consistencia en toda la App
- **Automático:** Actualizado el aviso de activación de GPS y las alertas de estado.
- **Manual:** Los errores de validación de campos vacíos o datos no numéricos ahora son elegantes y visibles.
- **Registro:** Las confirmaciones de copiado al portapapeles y eliminación masiva ahora coinciden con el estilo visual.
- **Archivos:** Los avisos de ruta de exportación mantienen la duración extendida de 5 segundos con el nuevo estilo azul.

## Verificación Visual
- Se utiliza el layout [layout_custom_toast_pro.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/layout_custom_toast_pro.xml) en todos los mensajes.
- Los iconos informativos se ajustan automáticamente según el tipo de mensaje.
- El texto es legible sobre fondos contrastados según la paleta minimalista (Oscuro/Naranja/Azul).

render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/UIUtils.java)
render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/RegisterFragment.java)
