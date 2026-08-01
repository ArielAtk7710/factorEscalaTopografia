# Plan de Implementación - Persistencia de Puntos, Registro y Exportación TXT

El objetivo es implementar un sistema completo de gestión de puntos topográficos que incluya: guardado en base de datos local, visualización en una lista histórica y exportación automática a archivos de texto (.txt), replicando la funcionalidad de la pestaña Manual.

## Cambios Propuestos

### [Componente: Datos - Base de Datos]

#### [NUEVO] [DatabaseHelper.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/solucionesit/factorcombinado/DatabaseHelper.java)
- Clase `SQLiteOpenHelper` para gestionar la tabla `puntos`.
- Campos: `id`, `nombre`, `latitud`, `longitud`, `altura`, `este`, `norte`, `zona`, `hemisferio`, `factor_escala`, `factor_altura`, `factor_combinado`, `fecha`.

### [Componente: Código - Lógica de Guardado Dual]

#### [MODIFICAR] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/solucionesit/factorcombinado/AutomaticFragment.java)
- **Guardado en DB**: Al confirmar en el diálogo, los datos se insertan en SQLite.
- **Exportación TXT**: Se integrará la lógica de `guardarResultados()` (similar a Manual) para que, al mismo tiempo que se guarda en la base de datos, se genere el archivo `.txt` en la carpeta de **Documentos**.
- Se capturarán los valores actuales de los `TextView` antes de abrir el diálogo.

### [Componente: UI - Pestaña Registro]

#### [MODIFICAR] [fragment_register.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/fragment_register.xml)
- Añadir un `RecyclerView` para la lista de puntos.
- Añadir un botón flotante (FAB) o botón superior para "**EXPORTAR TODO A TXT**" si se desea exportar el historial completo.

#### [NUEVO] [item_punto.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/item_punto.xml)
- Fila con diseño minimalista: Nombre del punto en negrita, fecha a la derecha, y el valor del Factor Combinado destacado.

#### [MODIFICAR] [RegisterFragment.java](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/java/bo/com/solucionesit/factorcombinado/RegisterFragment.java)
- Lógica para cargar la lista desde la DB.
- Implementar el adaptador para el `RecyclerView`.
- Añadir funcionalidad para borrar registros individuales (deslizar o icono de papelera).

## Plan de Verificación

### Verificación Manual
1. Guardar un punto desde la pestaña **Automático**.
2. Confirmar que aparece el Toast: "Guardado en DB y archivo TXT generado".
3. Verificar la carpeta **Documentos** para encontrar el nuevo archivo `.txt`.
4. Ir a la pestaña **Registro** y verificar que el nuevo punto aparece al principio de la lista.
5. Confirmar que los datos del registro coinciden exactamente con los exportados en el archivo.
