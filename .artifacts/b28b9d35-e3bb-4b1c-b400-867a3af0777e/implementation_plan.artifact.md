# Plan de Implementación - Cuadros Informativos Adaptables

Este plan asegura que todas las ventanas informativas (popups) de la aplicación se adapten automáticamente al tamaño de la pantalla de cualquier teléfono, evitando que el texto se corte y mejorando la legibilidad.

## Cambios Propuestos

### 1. Flexibilidad de Ancho en Layouts

#### [MODIFY] [layout_barometer_info.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/layout_barometer_info.xml)
- Cambiar el ancho fijo de `320dp` a `wrap_content`.
- Añadir un ancho máximo (`app:cardMaxWidth="340dp"`) para que en tablets o pantallas muy anchas no se estire demasiado, pero en teléfonos pequeños se reduzca lo necesario.

#### [MODIFY] [layout_map_guide.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/layout_map_guide.xml)
- Cambiar el ancho fijo de `320dp` a `wrap_content`.
- Asegurar que el contenedor interno permita que el texto largo salte de línea automáticamente.

#### [MODIFY] [layout_custom_toast_pro.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/layout_custom_toast_pro.xml)
- Ajustar el `TextView` del mensaje para que use todo el ancho disponible del contenedor (`match_parent` dentro del layout con peso), obligando al texto a envolverse (wrap) en lugar de cortarse.

### 2. Lógica de Centrado y Seguridad

#### [MODIFY] [UIUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/UIUtils.java)
- Verificar que la creación de los `PopupWindow` permita el redimensionamiento dinámico basado en el contenido inflado.

## Plan de Verificación

### Verificación de Adaptabilidad
1. Abrir el cuadro de **Barómetro**.
2. Abrir el cuadro de **Mapas**.
3. **Resultado esperado**: Los cuadros deben verse centrados. Si el texto es largo, el cuadro debe crecer hacia abajo (salto de línea) y no hacia los lados fuera de la pantalla.

### Verificación Visual
- Comprobar que no hay espacios en blanco excesivos y que el diseño se mantiene profesional y compacto.
