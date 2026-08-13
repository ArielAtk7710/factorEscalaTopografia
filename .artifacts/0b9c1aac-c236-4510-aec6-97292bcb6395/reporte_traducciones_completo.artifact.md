# Reporte Completo de Auditoría: Ortografía y Traducciones

Este reporte detalla el estado actual de los textos de la aplicación en sus 4 idiomas soportados: **Español (ES)**, **Inglés (EN)**, **Francés (FR)** y **Portugués (PT)**.

## 1. Resumen de Consistencia (Keys por Idioma)

| Estado | Español | Inglés | Francés | Portugués |
| :--- | :---: | :---: | :---: | :---: |
| **Total de Strings** | 100% | 100% | 100% | 100% |
| **Sincronización** | Base | Sincronizado | Sincronizado | Sincronizado* |

> [!NOTE]
> Se han sincronizado todas las llaves técnicas recientemente añadidas para la gestión de caché y detalles de puntos.

## 2. Hallazgos de Ortografía y Gramática (Por Corregir)

### 🇪🇸 Español (Principal)
- **`val_high_input` / `val_low_input`**: Se recomienda usar la tilde diacrítica en la disyunción "ó" cuando va entre números (ej: "1.5 ó 2.3") para evitar confusión con el cero en entornos técnicos, aunque la RAE ya no lo exige, en topografía es una buena práctica de legibilidad.
- **`app_name`**: "FactorEscalaTop" (Verificar si se desea espacio: "Factor Escala Top").

### 🇵🇹 Portugués (Crítico)
- **`msg_weather_accuracy_tip`**: Actualmente dice "...às veces podem variar um poco." -> Debe ser "...às vezes podem variar um **pouco**."
- **`label_export_directory`**: Decía "Exportación" (Español) -> Corregido a "Exportação".
- **`label_unknown_location`**: Dice "Localização Desconhecida". Correcto.
- **`msg_gps_toggle_info`**: Contiene la palabra "basadas" (Español) -> Debe ser "**baseadas**".

### 🇫🇷 Francés
- **`label_map_export_path`**: Dice "Ruta de datos exportados" (Español) -> Debe ser "**Chemin des données exportées**".
- **`val_high_input` / `val_low_input`**: Usan el conector "o" (Español) -> Debe ser "**ou**".
- **`msg_gps_toggle_info`**: Contiene "basadas" -> Debe ser "**basées**".

## 3. Textos Pendientes de Migración (Hardcoded en Código)

Se han identificado los siguientes textos que aún viven en el código Java y deben moverse a `strings.xml` para poder ser traducidos:

| Archivo | Texto Detectado | Acción Sugerida |
| :--- | :--- | :--- |
| `MapFragment.java` | "Guardando datos..." | Crear `@string/msg_saving_data` |
| `MapFragment.java` | "Error técnico: " | Crear `@string/err_technical_prefix` |
| `MapFragment.java` | "Información de Altura" | Usar `@string/label_height_info` |
| `AutomaticFragment.java` | "Excelente", "Buena", "Media", "Baja" | Usar `@string/precision_...` |
| `MainActivity.java` | "Alta Precisión" | Crear `@string/label_high_precision_status` |

## 4. Inconsistencias de Nomenclatura

- **Modelo Geoidal**: Se ha unificado a **MGBol08** en la mayoría de las vistas, pero se debe verificar que en las descripciones de los términos de uso se mencione igual y no como "MGBol" a secas.

## Recomendación Final

Se procederá a:
1.  **Limpiar** los archivos XML de otros idiomas de restos de español (ou/or/ou, baseada/based/basée).
2.  **Extraer** los últimos 5-6 textos hardcoded detectados en las pestañas de Mapa y Automático.
3.  **Corregir** el error de "poco" por "pouco" en la versión portuguesa que falló en el intento anterior.

---
**¿Deseas que ejecute estas correcciones finales basadas en este reporte?**
