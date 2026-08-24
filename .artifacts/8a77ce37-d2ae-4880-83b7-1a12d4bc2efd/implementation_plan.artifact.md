# Plan de Implementación: Optimización y Limpieza Integral de la App

Este plan describe las acciones para eliminar código muerto, recursos redundantes y optimizar la lógica interna para mejorar la eficiencia y el rendimiento de **FactorEscalaTop**.

## User Review Required

> [!WARNING]
> Se eliminarán permanentemente las clases técnicas del IGM que no han sido vinculadas a ninguna herramienta del menú lateral (`PlateVelocity`, `RasterProcessor`, `EnuConverter`, `TmConverter`, `DatumTransformer`, `EcefConverter`). Esto simplificará enormemente el mantenimiento del proyecto.

## Proposed Changes

### 1. Eliminación de Clases Inactivas (Java)

Se eliminarán los siguientes archivos por no tener referencias activas ni funcionalidad vinculada a la UI:
- [DELETE] `IGMPlateVelocityCalculator.java`
- [DELETE] `IGMRasterProcessor.java`
- [DELETE] `IGMEnuConverter.java`
- [DELETE] `IGMTmConverter.java`
- [DELETE] `IGMDatumTransformer.java`
- [DELETE] `IGMEcefConverter.java`

### 2. Eliminación de Recursos Obsoletos (XML)

Se eliminarán archivos de diseño y gráficos que han sido superados por nuevas versiones:
- [DELETE] `item_weather_detail.xml` (reemplazado por `item_weather_detail_pro.xml`)
- [DELETE] `layout_weather_weekly_card.xml` (reemplazado por `item_daily_weather.xml`)
- [DELETE] `ic_info_white.xml` (sin uso actual)

### 3. Refactorización y Limpieza de Código Muerto

#### [MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- Eliminar el método `getGeoidUndulationForCurrentSetting` y sus variables relacionadas que ya no se usan tras la centralización en el ViewModel.

#### [MODIFY] [SurveyViewModel.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/SurveyViewModel.java)
- Eliminar el método `shouldRefreshWeatherAuto` y todas las constantes de tiempo/distancia de clima que quedaron obsoletas con la carga bajo demanda.

#### [MODIFY] [CompassFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/CompassFragment.java)
- Eliminar la variable `geocoderThread` y optimizar la limpieza de tareas en `onPause` utilizando el pool de hilos.

### 4. Mejora de Eficiencia en layouts

#### [MODIFY] [layout_dialog_guide.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/layout_dialog_guide.xml)
- Optimizar la jerarquía de vistas (eliminar anidamientos innecesarios) para mejorar la velocidad de renderizado.

## Verification Plan

### Automated Tests
- Ejecutar `gradlew assembleDebug` para asegurar que no se rompieron dependencias.

### Manual Verification
1.  **Navegación**: Verificar que todas las herramientas sigan funcionando sin las clases eliminadas.
2.  **Clima**: Confirmar que la carga bajo demanda sigue siendo la única forma de obtener datos.
3.  **Memoria**: Verificar que la app ocupe ligeramente menos espacio tras la limpieza.
