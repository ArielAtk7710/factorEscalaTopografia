# Plan de Adición de Botón de Marcador Azul

El usuario desea agregar un nuevo botón de acción flotante (FAB) en la vista del mapa, situado encima del botón de limpieza (magenta). Este botón utilizará una imagen personalizada de un "pin azul".

## Cambios Propuestos

### 1. Preparación de Recursos
- **Imagen**: Se requiere que la imagen `pin azul.png` esté disponible en la carpeta `res/drawable` como `ic_blue_pin.png` para poder ser referenciada en el layout XML.
- *Nota*: Como asistente, configuraré el layout para buscar `@drawable/ic_blue_pin`. El usuario debe asegurarse de que el archivo se encuentre en esa ruta.

### 2. Interfaz de Usuario (Layout)

#### [MODIFY] [fragment_map.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_map.xml)
- Agregar un nuevo `com.google.android.material.floatingactionbutton.FloatingActionButton` al inicio del `LinearLayout` vertical.
- Atributos:
    - `android:id="@+id/fab_blue_pin"`
    - `app:fabSize="mini"`
    - `android:src="@drawable/ic_blue_pin"` (o similar)
    - `android:layout_marginBottom="12dp"`
    - `app:tint="@null"` (para mantener los colores originales de la imagen PNG)

## Plan de Verificación

### Pruebas Manuales
1.  Abrir la pestaña **Mapa**.
2.  Verificar que aparezca un nuevo botón encima del botón magenta de limpieza.
3.  Confirmar que el tamaño sea idéntico al del botón de limpieza (tamaño mini).
4.  Asegurar que la imagen del pin azul se visualice correctamente.

---
**¿Deseas que proceda con la modificación del layout?**
