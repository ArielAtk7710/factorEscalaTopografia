# 🌤️ Flyer 05: Clima, Presión Barométrica y Seguridad para Drones

Aprende a interpretar el **Módulo Meteorológico** de FesTop para controlar la presión atmosférica, humedad, altímetro y el **Índice Solar Kp** para operaciones seguras con Drones/UAV.

---

## 📸 Vista de Módulo Clima y Seguridad Drones

![Módulo Clima y Drones](capturas/05_modulo_clima_drones.png)

---

## 🛰️ Parámetros Monitoreados

### 1. Presión Atmosférica y Altímetro
- Monitoreo en hectopascales (hPa) en tiempo real mediante el sensor barométrico del dispositivo.

### 2. Índice Geomagnético Kp (Seguridad Solar para Drones)
- Mide la actividad solar y tormentas geomagnéticas que afectan la señal GNSS/RTK de los drones:
  - 🟢 **Kp 0 - 3 (Verde)**: Condiciones óptimas para vuelo fotogramétrico y RTK.
  - 🟡 **Kp 4 (Amarillo)**: Moderado; posible fluctuación leve de satélites.
  - 🔴 **Kp >= 5 (Rojo)**: Alerta; tormenta solar activa. Riesgo de pérdida de FIX RTK en Drones.

---

> [!NOTE]
> **Planificación de Vuelos Fotogramétricos**  
> Revisa siempre la barra de seguridad Kp antes de despegar tu dron para garantizar levantamientos aerofotogramétricos sin pérdida de satélites.
