# Plan de Trabajo: Capa de Procesamiento GNSS Avanzado

Este plan describe la implementación de una capa de filtrado y estabilización de posición GPS/GNSS que actuará como intermediario entre los datos crudos del sistema operativo Android y el motor de cálculos topográficos de **factorEscala**.

## REGLA DE INTEGRIDAD
Se confirma que las siguientes clases de cálculo geodésico permanecerán **INTACTAS**:
- `IGMUtmConverter`, `TopoCalculoManager`, `IGMConstants`, `IGMScaleCalculator`, `IGMElevationCalculator`, `IGMPressureCalculator`, `MGBEngine`, `EGM96Engine`, `GeoUtils`, y todas las clases con prefijo `IGM`.

## Objetivos Técnicos
1. **Control de Calidad**: Analizar Accuracy y satélites antes del procesamiento.
2. **Detección de Outliers**: Filtrar saltos de posición mediante análisis de velocidad cinemática ($v = d / \Delta t$).
3. **Estabilización (Promedio Ponderado)**: Implementar un buffer de posiciones donde el peso sea inversamente proporcional al cuadrado de la incertidumbre ($1/\sigma^2$).
4. **Filtro de Estabilidad**: Implementar un filtro de suavizado para reducir el ruido en coordenadas estáticas.
5. **Diferenciación RAW vs FILTERED**: Mantener ambos flujos de datos para comparación y auditoría.

---

## Proposed Changes

### 1. Nuevo Módulo de Procesamiento GNSS

#### [NEW] [GnssFilter.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/gnss/GnssFilter.java)
- Clase principal encargada de gestionar el buffer de posiciones.
- Mantendrá una lista circular de las últimas $n$ posiciones válidas.
- Implementará el método `filter(Location rawLocation)` que retorna un objeto con la posición suavizada.

#### [NEW] [GnssMeasurement.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/gnss/GnssMeasurement.java)
- Estructura de datos para almacenar la latitud, longitud, altitud y precisión filtrada, preservando el tipo `double`.

### 2. Integración en el Flujo de Datos

#### [MODIFY] [SurveyViewModel.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/SurveyViewModel.java)
- Instanciar `GnssFilter`.
- En `processNewLocation(Location loc)`, pasar la `loc` por el filtro.
- Crear nuevas `LiveData` para exponer la posición filtrada y la comparación de deltas ($\Delta E, \Delta N, \Delta H$).
- Enviar la posición **FILTRADA** al `TopographyRepository` para que los cálculos topográficos se realicen sobre la base estabilizada.

### 3. Interfaz de Comparación y Auditoría

#### [MODIFY] [fragment_automatic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_automatic.xml)
- Añadir una sección de "Auditoría de Precisión" (ocultable o en la parte inferior).
- Mostrar los valores RAW frente a los valores FILTERED.
- Mostrar los deltas en metros para verificar la efectividad del filtrado.

#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java)
- Vincular los nuevos campos de la UI.
- Implementar el botón "INICIAR MEDICIÓN ESTÁTICA" que resetee el filtro y acumule datos para un reporte de precisión.

---

## Verification Plan

### Prueba de Regresión (Integridad Matemática)
1. **Entrada Idéntica**: Se inyectará manualmente una coordenada fija a la app.
2. **Cálculo Topográfico**: Se verificará que el resultado (Factor de Escala, Combined Factor) sea **exactamente igual** (hasta el 9º decimal) antes y después de añadir la capa de filtrado, confirmando que la capa GNSS no altera la matemática topográfica.

### Prueba Cinemática (Detección de Outliers)
1. Simular un salto de coordenadas de 100 metros en 1 segundo.
2. Verificar que el `GnssFilter` marque la posición como `OUTLIER` y mantenga la posición anterior estable.

### Prueba Estática (Estabilización)
1. Dejar el dispositivo fijo.
2. Comparar la oscilación de los decimales de la coordenada Este/Norte RAW frente a la FILTERED. La versión filtrada debe mostrar una deriva significativamente menor.
