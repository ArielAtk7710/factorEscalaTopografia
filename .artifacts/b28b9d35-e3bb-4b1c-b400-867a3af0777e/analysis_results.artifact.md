# Informe de Auditoría de Calidad y Coherencia de Datos (QA)

**Proyecto**: bo.com.factorcombinadotopo (factorEscala)
**Auditor**: QA Lead & Software Architect

## 1. Vulnerabilidades de Precisión y Formato

### 1.1 Inconsistencia de Separador Decimal (Crítico para GIS)
- **Hallazgo**: El uso de `DecimalFormat` sin especificar un `Locale` hereda la configuración del sistema. En dispositivos configurados en español, los factores y coordenadas se están formateando con comas (`,`).
- **Impacto**: Los archivos TXT exportados no pueden ser procesados correctamente por software profesional (AutoCAD, ArcGIS, Civil3D) que requiere el punto (`.`) como estándar decimal internacional para coordenadas.
- **Localización**: `AutomaticFragment.java`, `ManualFragment.java`, `CompassFragment.java`.

### 1.2 "Datos Sucios" en Base de Datos (Integridad)
- **Hallazgo**: Se están guardando cadenas de texto combinadas en la base de datos, por ejemplo: `0.99909005 ( -910 PPM)`.
- **Impacto**: Imposibilita el uso de esos datos para re-cálculos internos o análisis estadísticos futuros. El almacenamiento de datos técnicos debe ser atómico (solo el valor numérico).
- **Localización**: `DatabaseHelper.java`, `AutomaticFragment.java`.

### 1.3 Discrepancia en Conversión de Presión
- **Hallazgo**: `CompassFragment` usa una constante manual (`1.33322`) para convertir mmHg a hPa, mientras que `TopoCalculoManager` usa una relación de constantes (`P0_HPA / P0_MMHG`).
- **Impacto**: Variación de ~0.01 hPa entre pantallas para el mismo sensor, restando profesionalismo a la herramienta.
- **Localización**: `CompassFragment.java` vs `TopoCalculoManager.java`.

## 2. Coherencia en Flujo de Datos

### 2.1 Latitud/Longitud en Registro
- **Hallazgo**: Se guarda el formato DMS (`16º 29' ...`) como fuente de verdad en la DB.
- **Impacto**: Si el usuario necesita exportar a CSV para mapeo masivo, el formato DMS es difícil de procesar comparado con el Grado Decimal (DD).

### 2.2 Sincronización de Sensores
- **Hallazgo**: Los intervalos de actualización de GPS varían entre 1s (Automático) y 2s (Brújula).
- **Impacto**: Al saltar entre pestañas, los datos UTM parecen "saltar" o estar desfasados temporalmente.

## 3. Vulnerabilidades de Software (Excepciones)

### 3.1 Codificación de Caracteres
- **Hallazgo**: `FileUtils.savePublicTxtFile` usa `content.getBytes()` sin especificar Charset.
- **Impacto**: Posibles errores de lectura en caracteres especiales (º, ', ") al abrir reportes en diferentes sistemas operativos (Windows vs Linux).

### 3.2 Posible NPE en WeatherFragment
- **Hallazgo**: El `updateUI` del clima usa `requireContext()` dentro de un callback asíncrono que puede ejecutarse cuando el fragmento ya no está adjunto. (Corregido parcialmente en auditorías previas, pero requiere revisión final).

---

## Recomendaciones Técnicas
1. **Forzar Locale.US** en todas las conversiones `double -> String` de datos técnicos.
2. **Atomicidad**: Guardar solo valores numéricos en la DB; delegar el formato (PPM, unidades, símbolos) exclusivamente a la capa de UI y Reportes.
3. **Unificación de Constantes**: Crear un método estático en `GeoUtils` o `TopoCalculoManager` para la conversión de unidades de presión.
