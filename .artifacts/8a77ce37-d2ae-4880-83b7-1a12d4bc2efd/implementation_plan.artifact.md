# Plan de Unificación Visual: Iconos de Información

Este plan detalla la unificación de los iconos de información en los módulos de **Modo Replanteo** y **Automático** para que coincidan con el estilo premium utilizado en los Ajustes del Sistema.

## User Review Required

> [!IMPORTANT]
> Los iconos de información pasarán de ser simples imágenes a botones con el estilo "Box" (fondo gris suave redondeado y marca de agua) similar al que se usa junto al Modelo Geoidal en Ajustes. Esto mejorará la accesibilidad táctil.

## Estilo a Replicar (Referencia: Ajustes)
- **Fondo**: `@drawable/bg_spinner_with_arrow`
- **Tinte de Fondo**: `@color/bg_surface_secondary`
- **Tinte de Icono**: `@color/accent_light` (Azul Suave)
- **Dimensiones**: Se ajustarán a **32dp x 32dp** para armonizar con los botones de acción (`MaterialButton`) de los fragmentos, manteniendo la proporción visual.

## Proposed Changes

### 1. Interfaz de Usuario (UI)

#### [MODIFY] [fragment_automatic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_automatic.xml)
- Cambiar `btn_gnss_info` de `ImageView` a `ImageButton` (o aplicar el estilo al actual).
- Aplicar:
    - `android:layout_width="32dp"`
    - `android:layout_height="32dp"`
    - `android:background="@drawable/bg_spinner_with_arrow"`
    - `android:backgroundTint="@color/bg_surface_secondary"`
    - `app:tint="@color/accent_light"`
    - `android:padding="6dp"` (para que el icono se vea centrado y de buen tamaño).

#### [MODIFY] [fragment_stakeout.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_stakeout.xml)
- Replicar exactamente la misma configuración para `btn_stakeout_info`.
- Esto reemplazará el icono azul plano actual por el botón estilizado.

### 2. Lógica de Código

#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/AutomaticFragment.java)
#### [MODIFY] [StakeoutFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/StakeoutFragment.java)
- Asegurar que los bindings se manejen correctamente si se cambia el tipo de vista de `ImageView` a `ImageButton` (aunque `View` suele ser suficiente).

## Verification Plan

### Manual Verification
1. **Consistencia**: Comparar lado a lado el botón de información de "Modelo Geoidal" (en Ajustes) con el nuevo botón en "Modo Replanteo". Deben tener el mismo color de fondo y diseño de borde.
2. **Interactividad**: Confirmar que el área de toque es cómoda y que el diálogo se abre correctamente al pulsar.
3. **Alineación**: Verificar que el nuevo tamaño de 32dp se alinea perfectamente con los títulos y botones vecinos en los card headers.
