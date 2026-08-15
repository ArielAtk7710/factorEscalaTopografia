# Reporte de Auditoría: Unificación de Paleta de Colores (Naranja y Azul)

Se ha realizado un análisis exhaustivo de los recursos de color en layouts XML, estilos y código Java para identificar desviaciones de la paleta oficial establecida: **@color/accent_orange** y **@color/accent_primary**.

## 1. Desviaciones de Color Naranja

| Ubicación | Color Detectado | Propuesta | Razón |
| :--- | :--- | :--- | :--- |
| `colors.xml` (Default) | `colorAccent` (#FF9800) | **@color/accent_orange** | El naranja por defecto de Android difiere de tu naranja Premium. |
| `fragment_compass.xml` | `app:tint="#F97316"` | **@color/accent_orange** | Uso de hexadecimal fijo en lugar de recurso semántico. |
| `TopoCalculoManager.java` | *N/A (Lógica)* | N/A | No se detectan colores hardcoded en fórmulas. |

## 2. Desviaciones de Color Azul

| Ubicación | Color Detectado | Propuesta | Razón |
| :--- | :--- | :--- | :--- |
| `activity_main.xml` | `tabIndicatorColor="#3B82F6"` | **@color/accent_primary** | Se usa un azul "Azure" que no pertenece a la paleta principal. |
| `fragment_map.xml` | `app:backgroundTint="#00BFFF"` | **@color/accent_primary** | El botón de polígono usa un azul celeste brillante externo a la paleta. |
| `colors.xml` (Default) | `colorPrimary` (#2196F3) | **@color/accent_primary** | El azul estándar de Android debe ser sustituido por tu azul de marca. |
| `fragment_compass.xml` | `app:tint="#223B82F6"` | **@color/accent_primary** | Transparencia aplicada sobre un azul no oficial. |

## 3. Otros Hallazgos (Limpieza de Estilo)

- **Consistencia en Diálogos**: Se han detectado algunos textos en blanco puro (`#FFFFFF`) y gris suave (`#E0E0E0`) que deberían usar `@color/text_white_pure` y `@color/text_gray_light` respectivamente para facilitar el mantenimiento.
- **Sincronización Dark Mode**: En `values-night/colors.xml`, algunos acentos como `state_info` todavía apuntan a `#5D98DB` (Azul claro), lo cual genera inconsistencia visual con el resto de la interfaz.

---

## 4. Conclusión

La aplicación presenta un "desvío estético" principalmente en los componentes de medición del mapa y en la barra de pestañas (Tabs), donde se han introducido azules y naranjas genéricos de Android o hexadecimales fijos.

**¿Deseas que proceda con la unificación total a @color/accent_orange y @color/accent_primary?**
