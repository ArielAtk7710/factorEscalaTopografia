# Plan de Limpieza, Verificación y Exportación Pública

Este plan consolida la migración de archivos a la memoria pública del celular, la notificación de ruta mediante Toasts y la limpieza profunda del proyecto para eliminar archivos basura.

## Cambios en Funcionalidades

### 1. Centralización de Exportación (FileUtils)
#### [NEW] [FileUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/FileUtils.java)
- Implementar `savePublicTxtFile` usando `MediaStore` para que los archivos sean visibles en `Documents/FactorEscalaTop`.
- Retornar la ruta amigable para mostrar en el Toast.

### 2. Actualización de Pestañas
#### [MODIFY] [RegisterFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/RegisterFragment.java)
#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java)
#### [MODIFY] [ManualFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/ManualFragment.java)
- Usar `FileUtils.savePublicTxtFile`.
- Mostrar Toast: `"Guardado en: Almacenamiento Interno > Documents > FactorEscalaTop > [archivo].txt"`.

## Limpieza del Proyecto (Archivos no usados)

He detectado varias carpetas que no pertenecen a la estructura estándar de Android y contienen archivos duplicados o basura:

#### [DELETE] `/resources/`
- Contiene layouts antiguos que ya fueron migrados a `app/src/main/res`.

#### [DELETE] `/sources/`
- Contiene código fuente redundante o de respaldo que no es utilizado por el compilador de Gradle.

#### [DELETE] `/build/` (Raíz)
- Los archivos de compilación deben estar dentro de cada módulo. El `/build` de la raíz se puede regenerar si es necesario.

#### [CLEANUP] `.artifacts/`
- Eliminar subcarpetas de interacciones pasadas, manteniendo solo la actual para liberar espacio y reducir ruido.

## Plan de Verificación

1. **Sincronización:** Ejecutar `Gradle Sync`.
2. **Compilación:** Ejecutar `clean build` para asegurar que el proyecto está íntegro sin los archivos eliminados.
3. **Prueba de Exportación:** Verificar que el Toast muestre la ruta y que el archivo aparezca en la memoria pública.

---
¿Deseas que proceda con la limpieza y la actualización de almacenamiento?
