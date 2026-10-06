# FactorEscala (FesTopBol) v2.4

Aplicación profesional avanzada para topografía, geodesia y cálculos de ingeniería civil en Bolivia y el mundo.

---

## 🚀 Características Principales

- **Modelos Geoidales Oficiales**: Soporte integrado para el modelo geoidal **MGBol08** (Bolivia, basado en EGM2008) y **EGM96** (Global).
- **Cálculos Topográficos Avanzados**:
  - Conversión de coordenadas UTM y Lambert.
  - Reducción de distancias y cálculo de factores de escala y altura.
  - Libreta de campo digital y registro automatizado de puntos.
  - Modo Replanteo (*Stakeout*) con guía visual de proximidad.
- **Módulo Meteorológico y de Seguridad**:
  - Monitoreo de condiciones atmosféricas y presión barométrica.
  - Análisis de índice Kp y seguridad solar/geomagnética para operaciones de campo.
- **Sistema de Licenciamiento Seguro**:
  - Soporte para licencias DEMO y profesionales con doble persistencia (SQLite + SharedPreferences) para garantizar que la activación permanezca intacta tras reinicios.

---

## 👥 Autores y Creadores

> [!NOTE]
> **Equipo de Desarrollo y Colaboradores**
> - **💻 Desarrollo de Software & Arquitectura:** Attack7710
> - **📐 Colaboradores Técnicos y Pruebas de Campo:** Profesionales del área de Topografía y Geodesia que contribuyeron mediante pruebas empíricas en terreno para validar los algoritmos geoidales y topográficos.
> - **© Derechos de Autor:** Todos los derechos de software, diseño de interfaz y arquitectura pertenecen a los creadores del proyecto **factorEscala**.

---

## 📱 Tecnologías Utilizadas
- **Lenguaje:** Java / Android SDK
- **Interfaz de Usuario:** Material Design 3 (XML & Compose interoperability)
- **Mapas y Geolocalización:** osmdroid / GPS integrado
- **Persistencia:** SQLite (`DatabaseHelper`) y SharedPreferences
