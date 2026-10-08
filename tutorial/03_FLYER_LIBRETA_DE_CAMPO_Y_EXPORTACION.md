# 📋 Flyer 03: Libreta de Campo y Exportación DXF/CSV/TXT

Gestiona el registro de tus puntos topográficos y expórtalos directamente a proyectos de **AutoCAD (DXF)**, planillas Excel (**CSV**) o colectoras de datos (**TXT**).

---

## 📸 Vista de Libreta de Campo y Exportación

![Libreta de Campo](capturas/03_libreta_campo_dxf.png)

---

## 🛠️ Procedimiento de Registro y Exportación

### 1. Guardar Puntos en Campo
- Luego de realizar un cálculo, presiona **"GUARDAR EN LIBRETA"**.
- Asigna un nombre al punto (ej. `P-001`, `EST-1`, `BM-02`), código de atributos y descripción.

### 2. Exportación Multiformato
En el menú de la Libreta de Campo, presiona **"EXPORTAR PUNTOS"** y elige tu formato preferido:

- **DXF (AutoCAD)**:
  - Genera automáticamente un archivo de dibujo con capas independientes de Puntos, Nombres, Elevaciones y Descripciones listo para abrir en AutoCAD o Civil3D.
- **CSV (Excel)**:
  - Formato separado por comas ideal para abrir en Microsoft Excel o Google Sheets.
- **TXT (PNEZD)**:
  - Formato plano estándar `Punto, Norte, Este, Z (Elevación), Descripción` listo para colectoras de datos o Estaciones Totales.

---

> [!TIP]
> **Compatibilidad AutoCAD Directa**  
> Al exportar en DXF, los textos de altura y nombre se ubican automáticamente al lado del punto en capas etiquetadas `PUNTOS`, `NOMBRES` y `ELEVACIONES`.
