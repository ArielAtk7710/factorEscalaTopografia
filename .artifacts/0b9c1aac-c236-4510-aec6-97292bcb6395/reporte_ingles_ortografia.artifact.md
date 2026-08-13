# Reporte de Auditoría: Inglés y Ortografía (Punto 1)

Este reporte detalla la verificación del idioma **Inglés** y las correcciones ortográficas necesarias en el idioma **Español** (disyunción técnica "ó" y acentos).

## 1. Verificación de Idioma Inglés (`values-en`)

### Estado de Sincronización
- **Total de llaves**: 100% sincronizadas con el archivo base (`values/strings.xml`).
- **Traducciones faltantes**: Ninguna detectada. Todas las nuevas funcionalidades (Detalles de puntos, Limpieza de caché, Mensajes de carga) han sido traducidas correctamente al inglés.

### Calidad de la Traducción
- Se han revisado términos técnicos:
    - "Ellipsoidal Altitude" (Alt. Elipsoidal) - **Correcto**.
    - "Ortho. Altitude" (Alt. Ortométrica) - **Correcto**.
    - "Scale Factor" (Factor de Escala) - **Correcto**.
- **Mejora Sugerida**:
    - `label_height_gps_disp`: Actualmente dice "GPS Height Device". Sugerencia: "**Device GPS Height**" para sonar más natural.

## 2. Ortografía en Español (`values`) - Punto 1

### Disyunción Técnica entre Números ("ó")
Aunque la RAE eliminó la obligatoriedad de tildar la "o" entre números, en el ámbito de la topografía y cartografía se mantiene como **buena práctica** para evitar confusiones visuales con el dígito "0" (cero).

**Hallazgos**:
- `val_high_input`: "1.5 ó 2.3" -> **Corregido** (ya aplicado en el paso anterior).
- `val_low_input`: "-1.5 ó -0.8" -> **Corregido** (ya aplicado en el paso anterior).

### Acentos y Gramática
- Se ha verificado el uso de acentos en palabras críticas: "Latitud", "Longitud", "Proyección", "Geodésicas", "Automático", "Caché", "Términos". Todos presentan la acentuación correcta.
- **Inconsistencia detectada**:
    - `msg_exit_app`: "¿Está seguro de que desea salir...?" -> **Correcto**.
    - `msg_map_delete_confirm`: "¿Está seguro de que desea eliminar...?" -> **Correcto**.
    - **Revisión**: Buscar si hay alguna descripción larga en "Acerca de" o "Términos" que use "esta" (demostrativo) en lugar de "está" (verbo). Tras revisar `terms_body`, el uso es gramaticalmente correcto.

## 3. Conclusiones y Acción

| Punto | Estado | Acción |
| :--- | :---: | :--- |
| **Inglés** | Sincronizado | Refinar `label_height_gps_disp`. |
| **Ortografía ES** | Muy Bueno | Aplicar tilde en "ó" en casos de ayuda técnica si faltara alguno. |
| **Consistencia** | Alta | La app presenta una imagen profesional. |

> [!TIP]
> El archivo `values-pt` (Portugués) sigue teniendo errores de "copy-paste" del español que no fueron corregidos totalmente en el paso anterior (ej: "às veces" en lugar de "às vezes"). Estos se abordarán en el siguiente paso de Traducciones FR/PT.

---
**¿Deseas que aplique los refinamientos de inglés y ortografía final antes de pasar a los idiomas FR/PT?**
