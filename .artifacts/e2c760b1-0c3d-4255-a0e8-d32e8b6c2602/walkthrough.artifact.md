# Limpieza de Interfaz Manual e Información de Exportación

He optimizado la pestaña **Manual** para centrarla en la entrada de datos del usuario y he añadido información crucial sobre la ubicación de los archivos en el menú de **Ajustes**.

## Mejoras Implementadas

### 1. Pestaña Manual Minimalista
- **Eliminación de Sensores:** He quitado la barra superior que mostraba la precisión y los satélites en el modo manual. Como esta sección está destinada al ingreso de datos teóricos o de otros instrumentos, la información del GPS interno era innecesaria y ocupaba espacio vital.
- **Foco en el Formulario:** Ahora el sistema de referencia WGS84 es lo primero que verás al entrar, permitiéndote empezar a trabajar de inmediato sin distracciones visuales.

### 2. Visibilidad del Directorio de Exportación
Para resolver la duda de dónde se guardan los reportes, he añadido una nueva sección informativa en el diálogo de **Ajustes**:
- **Ruta Transparente:** Ahora puedes ver la ruta exacta: `Almacenamiento Interno > Documents > FactorEscalaTop`.
- **Internacionalización:** Esta información también ha sido traducida a los 4 idiomas soportados por la app (**Inglés, Portugués y Francés**).

## Verificación
- **Consistencia Visual:** El nuevo panel informativo en Ajustes mantiene la estética de tarjetas sobre fondo oscuro.
- **Compilación Exitosa:** El proyecto se ha construido correctamente (`BUILD SUCCESSFUL`).

> [!NOTE]
> Al exportar cualquier registro, la app te notificará la ruta en pantalla, pero ahora también puedes consultarla en cualquier momento desde el menú de Ajustes para mayor seguridad.

render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_manual.xml)
render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/dialog_settings.xml)
render_diffs(file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/values/strings.xml)
