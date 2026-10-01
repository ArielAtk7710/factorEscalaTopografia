# Plan de Implementación: Auditoría y Verificación de Modo Claro y Modo Oscuro

Este plan detalla la revisión de la aplicación **factorEscala** para garantizar que todas las vistas, tarjetas, diálogos y textos utilicen los colores semánticos dinámicos (`bg_main`, `bg_surface`, `text_primary`, `text_secondary`, `divider`) configurados en `values/colors.xml` y `values-night/colors.xml`.

---

## 1. Alcance de la Revisión

### A. Paleta de Colores Semánticos
- **Modo Claro (`values/colors.xml`):** Fondos claros (`#F3F4F6`, `#FFFFFF`), texto oscuro (`#111827`, `#4B5563`).
- **Modo Oscuro (`values-night/colors.xml`):** Fondos oscuros (`#1B1D20`, `#25282C`), texto claro (`#E8E8E8`, `#9FA2A3`).

### B. Auditoría de Layouts XML
- Revisar que los layouts principales (`activity_main.xml`, fragmentos y diálogos) utilicen `@color/bg_main`, `@color/bg_surface`, `@color/text_primary` y `@color/text_secondary` en lugar de colores hardcodeados (como `@android:color/white` o `#000000`) cuando sea necesario adaptarse al tema.
- Verificar el comportamiento por defecto en `MainActivity.java` (lectura de preferencias de tema `KEY_THEME` al iniciar).

---

## 2. Plan de Verificación

### Compilación y Build
- Ejecutar `gradle_build("app:assembleDebug")` para validar la correcta integración de recursos y estilos.
