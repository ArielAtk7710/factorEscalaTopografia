# Plan de Unificación de Notificaciones (Toasts)

El objetivo es asegurar que todas las notificaciones de la aplicación utilicen el diseño personalizado y la paleta de colores establecida, eliminando los Toasts estándar de Android que rompen con la estética minimalista de la app.

## Cambios Propuestos

### 1. Centralización en UIUtils
#### [MODIFY] [UIUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/UIUtils.java)
- Ampliar la clase para incluir métodos específicos para cada tipo de mensaje:
    - `showSuccessToast(Context, String message)`
    - `showErrorToast(Context, String message)`
    - `showInfoToast(Context, String message)` (y la versión `Long` para rutas)
    - `showWarningToast(Context, String message)`
- Todos los métodos utilizarán el layout `layout_custom_toast_pro.xml` con los fondos y colores correspondientes (`bg_toast_success`, `bg_toast_error`, etc.).

### 2. Actualización de Fragmentos y Utilidades
#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java)
- Eliminar el método local `showProToast`.
- Reemplazar todas las llamadas internas por `UIUtils.show...Toast`.

#### [MODIFY] [ManualFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/ManualFragment.java)
- Reemplazar todos los `Toast.makeText` (mensajes de validación y errores de cálculo) por la versión estilizada.

#### [MODIFY] [RegisterFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/RegisterFragment.java)
- Reemplazar los `Toast.makeText` (eliminación, copiado, selección) por la versión estilizada.

#### [MODIFY] [FileUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/FileUtils.java)
- Asegurar que los errores de escritura usen `UIUtils.showErrorToast`.

## Plan de Verificación

### Pruebas de Interfaz
1. **Éxito:** Guardar un punto y verificar el Toast verde.
2. **Error:** Intentar calcular sin datos y verificar el Toast rojo.
3. **Info:** Exportar y verificar el Toast azul de 5 segundos.
4. **Advertencia:** Desactivar GPS y verificar el Toast naranja recurrente.

---
¿Deseas que proceda con la unificación de todos los Toasts de la app?
