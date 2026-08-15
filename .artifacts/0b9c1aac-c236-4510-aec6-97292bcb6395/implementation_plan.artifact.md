# Plan Maestro: v2.5.5 - Estabilidad Final y Experiencia Offline

Este plan detalla el "toque final" de ingeniería para asegurar que **FactorEscalaTop** sea una herramienta impecable en campo, con un manejo de red proactivo y una estética 100% coherente.

## 1. Gestión Proactiva de Red (Offline Mode)

### Indicador de Estado
- **[MODIFY] [activity_main.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/activity_main.xml)**:
    - Añadir un pequeño icono de "Nube Tachada" (Offline) al lado del título "Top" en la barra superior.
    - Este icono solo será visible cuando el dispositivo no tenga internet.
- **[MODIFY] [MainActivity.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/MainActivity.java)**:
    - Implementar un observador de red en tiempo real.
    - Actualizar la visibilidad del icono offline según el estado de la conexión.

### Mensajería de Respaldo
- **[MODIFY] [strings.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/values/strings.xml)**:
    - Crear `msg_offline_warning`: "Función Online no disponible. Trabajando en Modo Offline (Solo Sensores Local)."
- **[MODIFY] [ManualFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/ManualFragment.java)**:
    - Al presionar "Calcular", si no hay red, mostrar un Toast informativo antes de proceder con el cálculo local (MGBol08/EGM96).

## 2. Blindaje de Interfaz (Cero Bloqueos)

### Estabilidad de Diálogos
- **[MODIFY] [UIUtils.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/UIUtils.java)**:
    - Asegurar que todas las llamadas a `Toast` y `Dialog` ocurran dentro de un `Looper.getMainLooper()` (Hilo Principal) para evitar el crash accidental en hilos de fondo.
- **[MODIFY] [WeatherFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/WeatherFragment.java)**:
    - Añadir protecciones `isAdded()` extras en los adaptadores de la lista semanal y horaria.

## 3. Unificación Estética Final

### Sincronización de Naranja y Azul
- **[MODIFY] [fragment_compass.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_compass.xml)**:
    - Revisar el color de los textos secundarios para asegurar que no sean negros puros en modo oscuro (usar `@color/text_secondary`).

---

## Plan de Verificación

1.  **Simulacro de Campo**:
    - Iniciar la app con internet.
    - Activar Modo Avión.
    - El icono "Offline" debe aparecer arriba.
    - Entrar a Clima: Debe mostrar un aviso de "Requiere internet".
    - Guardar punto en el mapa: Debe usar el sensor GPS sin crashear.
2.  **Verificación de Tema**:
    - Alternar entre Modo Claro y Oscuro: Confirmar que el icono offline y todos los textos de la brújula sigan siendo legibles.

---
**¿Deseas que inicie con este blindaje final de la v2.5.5?**
