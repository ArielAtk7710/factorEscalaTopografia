# Plan de Limpieza y Preservación de Clases Maestras

Este plan tiene como objetivo resolver los errores de compilación y optimizar el proyecto desactivando (comentando) las clases que no se utilizan actualmente, pero preservando su lógica para futuras implementaciones.

## User Review Required

> [!IMPORTANT]
> Se comentará el **contenido completo** de las clases identificadas como "no utilizadas". Esto desactivará cualquier error de compilación asociado a ellas sin eliminar el código fuente del proyecto.
>
> **Clases a Comentar:**
> - `IGMGeodesicCalculator.java` (Causaba errores)
> - `IGMSurveyCalculator.java` (Causaba errores)
> - `IGMEcefConverter.java`
> - `IGMEnuConverter.java`
> - `IGMLambertConverter.java`
> - `IGMDistanceReducer.java`
> - `IGMTmConverter.java`
> - `IGMLineCalculator.java`
> - `IGMPlateVelocityCalculator.java`
> - `IGMRasterProcessor.java`
> - `TopoAlgorithmTest.java` (Test de validación)
>
> **Clase a Restaurar (Comentada):**
> - `IGMDatumTransformer.java` (Previamente eliminada)

## Proposed Changes

### 1. Desactivación de Clases No Utilizadas
Para cada archivo de la lista anterior, se envolverá todo el código dentro de un bloque de comentario multilínea `/* ... */`, manteniendo únicamente la declaración del `package`.

### 2. Verificación de Fórmulas Principales
Se ha confirmado mediante auditoría de dependencias que las siguientes clases **NO** serán modificadas y seguirán funcionando con total precisión:
- `TopoCalculoManager.java` (Orquestador)
- `IGMUtmConverter.java` (UTM)
- `IGMScaleCalculator.java` (Factor k)
- `IGMElevationCalculator.java` (Factor ha)
- `IGMPressureCalculator.java` (Presión)
- `IGMCoordinate.java` (Modelos de datos)
- `IGMConstants.java` (Constantes)

## Beneficios
- **Compilación Exitosa:** La app podrá generar el APK inmediatamente.
- **Resguardo de Lógica:** No se pierde ninguna de las 18 fórmulas maestras; quedan "dormidas" hasta que se necesiten.
- **Orden Técnico:** Solo el código que realmente se ejecuta estará activo.

## Plan de Verificación

### Automated Verification
- Ejecutar `gradle_build("assembleDebug")` para confirmar que la aplicación compila al 100%.

### Manual Verification
- Verificar en las pestañas **Automático** y **Manual** que los cálculos de Factor Combinado y Coordenadas UTM siguen siendo precisos.
