# Plan de Corrección de Traducción al Francés y Ortografía Técnica

Este plan aborda los errores detectados en el idioma **Francés** (`values-fr`) y asegura la consistencia ortográfica en la ayuda técnica del idioma **Español** (`values`).

## Cambios Propuestos

### 1. Correcciones en Francés (`values-fr/strings.xml`)

Se eliminarán los restos de texto en español y se ajustarán los términos técnicos al francés correcto:

- **`label_map_export_path`**: Cambiar "Ruta de datos exportados" por "**Chemin des données exportées**".
- **`val_high_input` / `val_low_input`**: Cambiar el conector "o" por "**ou**" (ej: "1.5 ou 2.3").
- **`label_no_obs_list`**: Cambiar "Sem observaciones" por "**Aucune observation.**".
- **`msg_gps_toggle_info`**: Corregir la descripción técnica para usar "**basées**" en lugar de "basadas".

### 2. Ortografía Técnica en Español (`values/strings.xml`)

- Se verificará que en todas las tablas de ayuda barométrica se utilice "**ó**" (con tilde) cuando se encuentre entre números, para garantizar la máxima legibilidad y evitar confusiones con el número cero (0).
    - Ya aplicado en `val_high_input` y `val_low_input`. Se revisará si existen otros casos similares en el archivo.

## Plan de Verificación

### Pruebas de Sistema
- Cambiar el idioma de la aplicación a **Francés** en los ajustes.
- Navegar a la sección de **Ajustes** y verificar la ruta de exportación.
- Abrir el mapa, marcar un punto y revisar la información de ayuda de altura (icono info).
- Verificar que en el historial no aparezcan términos en otros idiomas cuando no hay observaciones.

### Compilación
- Ejecutar un build de prueba para asegurar que los archivos XML mantienen la estructura correcta.
