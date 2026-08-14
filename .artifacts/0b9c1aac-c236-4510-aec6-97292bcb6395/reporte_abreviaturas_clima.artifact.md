# Reporte de Auditoría: Abreviaturas en Módulo Clima Vuelo Dron

Este reporte identifica todas las palabras abreviadas detectadas en el módulo de Asistente de Vuelo y Clima, proponiendo su expansión para una mayor claridad técnica.

## 1. Abreviaturas Críticas Detectadas (Español)

| Clave de Recurso | Texto Actual | Propuesta de Expansión | Ubicación / Contexto |
| :--- | :--- | :--- | :--- |
| `safety_cause_vis_critical` | "Visibilidad crítica para **VLOS**" | "Visibilidad crítica para **VUELOS**" | Diálogo de Seguridad |
| `label_wind_direction` | "**Dir.** Viento:" | "**Dirección** Viento:" | Panel de Detalles |
| `label_rain_prob_v` | "**Prob.** Lluvia:" | "**Probabilidad** Lluvia:" | Panel de Detalles |
| `label_wind_precip` | "VIENTOS Y **PRECIPITACIÓN**" | (Ya es palabra completa) | Título de Sección |
| `label_kp_solar` | "Índice **Kp** Solar:" | "Índice de **Actividad** Solar (Kp):" | Panel de Seguridad |
| `safety_cause_pdop_high` | "...(**PDOP** elevado)" | "...(Geometría Satelital pobre)" | Diálogo de Seguridad |

## 2. Abreviaturas Técnicas (Inglés)

| Clave de Recurso | Texto Actual | Propuesta de Expansión |
| :--- | :--- | :--- |
| `label_wind_direction` | "**Wind Direction:**" | (Ya es palabra completa) |
| `label_rain_prob_v` | "**Rain Prob.:**" | "**Rain Probability:**" |
| `label_kp_solar` | "**Solar Kp Index:**" | "**Solar Activity Index (Kp):**" |

## 3. Unidades de Medida (Se mantienen por estándar)

Las siguientes abreviaturas se consideran estándar internacional y no se recomienda expandirlas para no saturar la interfaz visual:
- **km/h**: Kilómetros por hora.
- **mm**: Milímetros.
- **hPa / mmHg**: Unidades de presión.
- **dB-Hz**: Potencia de señal satelital.
- **m / km**: Metros y Kilómetros.

---

## 4. Conclusiones y Próximos Pasos

La expansión de "**VLOS**" a "**VUELOS**" es la prioridad principal. Adicionalmente, expandir "Dir." y "Prob." mejorará la estética profesional de la aplicación sin comprometer el espacio en pantalla.

**¿Deseas que aplique estas expansiones de texto ahora?**
