# Walkthrough - Sistema de Registro y Exportación Dual

Se ha implementado un sistema completo de persistencia que permite guardar, visualizar y exportar los puntos topográficos registrados desde la pestaña **Automático**.

## Cambios Realizados

### 1. Base de Datos Local (`DatabaseHelper.java`)
- Se creó una base de datos SQLite (`puntos.db`) para almacenar de forma permanente:
    - Nombre del punto.
    - Coordenadas Geodésicas y UTM completas.
    - Factores de Escala, Altura y Combinado.
    - Fecha y hora automática del registro.

### 2. Guardado Dual en Automático (`AutomaticFragment.java`)
- El botón "**Guardar Punto**" ahora realiza dos acciones simultáneas:
    - **Registro en DB**: Guarda toda la información del cálculo actual en la base de datos interna.
    - **Exportación TXT**: Genera automáticamente un archivo de reporte en la carpeta de **Documentos**, igual que en la pestaña Manual.
- Se implementó un diálogo minimalista para ingresar el nombre del punto antes de guardar.

### 3. Pestaña de Registro Actualizada (`RegisterFragment.java`)
- **Lista Histórica**: Se reemplazó el texto estático por un `RecyclerView` que muestra todos los puntos guardados.
- **Interfaz Minimalista**: Cada fila (`item_punto.xml`) muestra el nombre, la fecha y destaca el Factor Combinado en naranja.
- **Gestión de Registros**: Se añadió un icono de eliminar (X) en cada fila para borrar registros individuales de la base de datos.
- **Estado Vacío**: Se incluyó un mensaje que aparece automáticamente cuando no hay puntos registrados.

## Verificación Realizada
- **Persistencia**: Se confirmó que los datos se mantienen guardados incluso al cerrar y volver a abrir la aplicación.
- **Sincronización**: La pestaña de Registro se actualiza automáticamente cada vez que se guarda un punto nuevo en la pestaña Automático.
- **Construcción**: El proyecto compila y se ejecuta sin errores.

> [!TIP]
> ¡Tu historial está listo! Cada vez que guardes un punto en "Automático", podrás verlo inmediatamente en la pestaña "**Registro**" y encontrar el archivo TXT correspondiente en tu carpeta de Documentos.
