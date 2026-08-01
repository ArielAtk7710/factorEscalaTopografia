# Plan de Refactorización de Paquete y Nombre de App

Este plan detalla los pasos para realizar un cambio completo del nombre del paquete y actualizar el nombre público de la aplicación, manteniendo la base de datos existente.

## Cambios Solicitados

### 1. Nombre de la Aplicación
#### [MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/values/strings.xml)
- Cambiar el valor de la cadena `app_name` de `"Factor de Escala"` a `"FactorEscalaTop"`.

### 2. Refactorización de Paquete
Se cambiará el paquete de `bo.com.solucionesit.factorcombinado` a `bo.com.factorcombinadotopo`.

#### [MOVE] Estructura de Directorios
- Mover todos los archivos de `app/src/main/java/bo/com/solucionesit/factorcombinado/` a `app/src/main/java/bo/com/factorcombinadotopo/`.
- Eliminar la carpeta intermedia `solucionesit`.

#### [MODIFY] Código Fuente (Java)
- Actualizar la declaración `package` en todos los archivos `.java`.
- Actualizar todas las referencias e imports que utilicen el nombre antiguo.

#### [MODIFY] Configuración del Proyecto
- **[build.gradle](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/build.gradle)**: Actualizar `namespace` y `applicationId`.
- **[AndroidManifest.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/AndroidManifest.xml)**: Actualizar el atributo `package` y las referencias a las Activities.

### 3. Base de Datos
- **Se mantiene como `puntos.db`** (sin cambios según lo solicitado).

## Consideraciones Críticas
> [!IMPORTANT]
> El cambio de `applicationId` hará que el dispositivo reconozca la aplicación como una **nueva app**. Si tienes instalada la versión anterior, esta se instalará como una aplicación separada.

## Plan de Verificación

### Compilación y Ejecución
1. Ejecutar `./gradlew clean` para eliminar archivos generados con el paquete antiguo.
2. Ejecutar `./gradlew :app:assembleDebug` para verificar que el nuevo `namespace` genere la clase `R` correctamente.
3. Verificar que el nombre de la app en el lanzador sea "FactorEscalaTop".

---
¿Deseas que proceda con esta refactorización?
