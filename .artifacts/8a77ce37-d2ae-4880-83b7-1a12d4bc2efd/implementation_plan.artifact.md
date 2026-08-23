# Plan de Implementación: Activación de Herramientas de Ingeniería Topográfica

Este plan detalla la activación de una serie de motores de cálculo "dormidos" (clases IGM) para crear nuevas herramientas profesionales en el menú lateral de la aplicación.

## User Review Required

> [!IMPORTANT]
> Se habilitarán 4 nuevas secciones en el menú lateral bajo la categoría "HERRAMIENTAS DE INGENIERÍA". Estas herramientas utilizan algoritmos avanzados del IGM (Instituto Geográfico Militar) para cálculos de alta precisión.

## Proposed Changes

### 1. Activación de Motores de Cálculo (Desbloqueo de Clases)
Se eliminarán los comentarios de bloque en las siguientes clases técnicas para que el compilador pueda utilizarlas:
- `IGMGeodesicCalculator.java`: Algoritmos de Bowring y Vincenty para distancias geodésicas.
- `IGMDistanceReducer.java`: Reducción de distancias inclinadas al horizonte y elipsoide.
- `IGMLineCalculator.java`: Cálculos entre dos puntos UTM (Distancia, Azimut, Convergencia).
- `IGMLambertConverter.java`: Conversión entre coordenadas geográficas y la proyección Lambert (específica para Bolivia).
- `IGMSurveyCalculator.java`: Cálculos de topografía plana y rumbos.

### 2. Nuevas Vistas (Fragments)
Se crearán 4 nuevos fragmentos con interfaces de usuario profesionales:
- `GeodesicFragment.java`: Interfaz para problemas geodésicos directos e inversos.
- `DistanceReductionFragment.java`: Calculadora de reducción de distancias.
- `LineCalculatorFragment.java`: Herramienta para cálculo entre dos coordenadas UTM.
- `LambertFragment.java`: Conversor específico para la proyección oficial de Bolivia.

### 3. Actualización de Navegación

#### [MODIFY] [drawer_menu.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/menu/drawer_menu.xml)
- Añadir una nueva categoría `<item android:title="Herramientas de Ingeniería">`.
- Incluir los 4 nuevos accesos directos con iconos técnicos.

#### [MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)
- Implementar los métodos `showGeodesic()`, `showDistanceReduction()`, `showLineCalculator()` y `showLambert()` para gestionar el intercambio de fragmentos.

## Verification Plan

### Manual Verification
1.  Desplegar la app y abrir el menú lateral.
2.  Verificar que aparezca la nueva sección con las 4 herramientas.
3.  Entrar a cada herramienta y realizar un cálculo de prueba comparando con los resultados esperados (ej. Vincenty para distancias largas).
4.  Asegurar que la navegación de regreso al inicio (ViewPager) funcione correctamente.
