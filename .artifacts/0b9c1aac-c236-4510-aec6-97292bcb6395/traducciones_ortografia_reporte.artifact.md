# Reporte de Auditoría: Ortografía y Traducciones

He realizado una revisión exhaustiva de todos los recursos de texto (`strings.xml`) y del código fuente para identificar errores ortográficos, inconsistencias en la nomenclatura y vacíos en las traducciones de los idiomas soportados (Español, Inglés, Francés y Portugués).

## 1. Errores Ortográficos e Inconsistencias (Español)

### Nomenclatura del Modelo Geoidal
- **Estado**: Se ha corregido la mayoría de las referencias a **MGBol08**.
- **Pendiente**: Asegurar que en todas las descripciones largas se use el nombre completo de forma uniforme.

### Errores en Recursos (`strings.xml`)
- `val_high_input`: Dice "1.5 o 2.3". Debería ser "1.5 ó 2.3" (opcional, pero consistente con el uso técnico).
- `val_low_input`: Dice "-1.5 o -0.8".
- `msg_weather_accuracy_tip`: Dice "poco" al final en la versión portuguesa, se debe revisar la gramática.

## 2. Estado de las Traducciones

### Vacíos Críticos (Keys faltantes)
Las siguientes llaves de texto añadidas recientemente **solo existen en Español** y necesitan ser traducidas al Inglés, Francés y Portugués para evitar que la app muestre texto en español a usuarios extranjeros:
- `title_confirm_cache_street`
- `msg_confirm_cache_street`
- `title_confirm_cache_sat`
- `msg_confirm_cache_sat`

### Errores de Traducción por "Copy-Paste"
En los archivos de otros idiomas, algunos valores se quedaron en español:
- **Francés (`values-fr`)**:
  - `val_high_input`: "1.5 o 2.3" (debe ser "1.5 ou 2.3").
  - `val_low_input`: "-1.5 o -0.8" (debe ser "-1.5 ou -0.8").
- **Portugués (`values-pt`)**:
  - `val_high_input`: "1.5 o 2.3" (debe ser "1.5 ou 2.3").
  - `val_low_input`: "-1.5 o -0.8" (debe ser "-1.5 ou -0.8").
  - `label_export_directory`: "Diretório de Exportación" (debe ser "Diretório de Exportação").

## 3. Textos "Hardcoded" (Escritos directamente en código)

Se han detectado textos que no están en `strings.xml`, lo que impide su traducción:
- **MapFragment.java**:
  - "Navegando a posición..."
  - "Información de Altura"
  - "Mapa visualmente limpio. Los registros permanecen seguros."
  - "No hay puntos registrados"
  - "Ingrese coordenadas válidas"
  - "Formato numérico inválido"
- **AutomaticFragment.java**:
  - Niveles de precisión: "Excelente", "Buena", "Media", "Baja".
- **Layouts**:
  - `dialog_save_point_map.xml`: "Altura GPS Disp.", "Altura online DEM".

## Recomendaciones de Corrección

1.  **Migración a Strings**: Mover todos los textos detectados en Java/XML a `strings.xml`.
2.  **Completar Traducciones**: Traducir las nuevas funciones de limpieza de caché a los 3 idiomas adicionales.
3.  **Corrección Gramatical**: Ajustar conectores ("o" por "ou", "ou" por "or") en las tablas técnicas de los idiomas correspondientes.

> [!IMPORTANT]
> Si no se corrigen los vacíos de traducción, los usuarios que usen la app en Inglés o Francés verán mensajes de error de sistema o textos en español en las nuevas funciones de mapas y caché.
