# Plan de Ajuste: Limpieza Manual y Ruta de Exportación

Este plan detalla la optimización de la pestaña **Manual** para centrarse exclusivamente en la entrada de datos del usuario y la mejora del panel de **Ajustes** para informar sobre la ubicación de los archivos generados.

## 1. Limpieza en Pestaña Manual
Eliminaremos la barra de estado superior (GPS) de esta pestaña, ya que su propósito es el ingreso de datos externos o teóricos.

### [MODIFY] [fragment_manual.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_manual.xml)
- Eliminar el bloque de "BARRA DE ESTADO GPS".
- Asegurar que el formulario de coordenadas sea lo primero visible para el usuario.

## 2. Información de Exportación en Ajustes
Añadiremos una sección informativa en el diálogo de ajustes para que el usuario sepa exactamente dónde encontrar sus reportes.

### [MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/values/strings.xml)
- Añadir etiquetas: `label_export_path`, `text_export_path_value`.

### [MODIFY] [dialog_settings.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/dialog_settings.xml)
- Añadir sección "**Directorio de Exportación**".
- Mostrar la ruta: `Almacenamiento Interno > Documents > FactorEscalaTop`.
- Estilo minimalista coherente con el resto del diálogo.

## Plan de Verificación
1. **Pestaña Manual:** Confirmar que la pantalla inicia directamente con el "Sistema de Referencia WGS84" sin los datos de satélites.
2. **Ajustes:** Abrir el menú de ajustes y verificar que la ruta de guardado sea claramente legible y correcta.

---
## Usuario, ¿Deseas que proceda con estos cambios de limpieza e información?
