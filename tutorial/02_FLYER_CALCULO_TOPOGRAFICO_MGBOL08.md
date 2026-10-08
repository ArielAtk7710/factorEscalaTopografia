# 📐 Flyer 02: Cálculos Topográficos y Factor Combinado

Aprende a transformar coordenadas UTM y Geodésicas y calcular los **Factores de Escala (k)**, **Factores de Altura (Kh)** y **Factor Combinado (K)** con el motor **MGBol08**.

---

## 📸 Vista de Cálculo Topográfico

![Cálculo Topográfico](capturas/02_calculo_topografico.png)

---

## 🔢 Modos de Cálculo

### 1. Cálculo Automático por GPS
1. Activa el GPS de tu dispositivo.
2. Presiona **"Obtener Coordenadas GPS"**.
3. La aplicación capturará tu posición (Latitud, Longitud, Altura Elipsoidal) y calculará de forma instantánea:
   - Coordenadas UTM (Este, Norte, Zona y Hemisferio).
   - Ondulación Geoidal $N$ del modelo MGBol08.
   - Altura Ortométrica $H = h - N$.
   - **Factor de Escala UTM (k)**.
   - **Factor de Altura (Kh)**.
   - **Factor Combinado $K = k \times Kh$**.

### 2. Cálculo Manual
1. Ingresa manualmente las Coordenadas UTM o Geodésicas de tu punto de control.
2. Presiona **"CALCULAR FACTOR COMBINADO"**.
3. Revisa la Ficha Técnica con los resultados precisos.

---

> [!IMPORTANT]
> **Fórmula del Factor Combinado**  
> El Factor Combinado ($K$) te permite multiplicar la distancia de terreno para obtener la distancia proyectada en el plano UTM de manera matemáticamente rigurosa.
