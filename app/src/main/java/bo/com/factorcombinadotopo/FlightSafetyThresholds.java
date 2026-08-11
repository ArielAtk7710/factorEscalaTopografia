package bo.com.factorcombinadotopo;

/**
 * Umbrales técnicos para la seguridad de vuelo de drones (RPAS).
 * Valores basados en estándares operativos para drones multirrotores.
 */
public class FlightSafetyThresholds {
    
    // VIENTO (km/h)
    public static final double WIND_SAFE = 15.0;
    public static final double WIND_CAUTION = 25.0;
    public static final double WIND_DANGER = 35.0;
    
    // RÁFAGAS (km/h)
    public static final double GUSTS_SAFE = 25.0;
    public static final double GUSTS_CAUTION = 35.0;
    public static final double GUSTS_DANGER = 45.0;
    
    // PRECIPITACIÓN (mm o %)
    public static final double RAIN_PROB_CAUTION = 10.0;
    public static final double RAIN_PROB_DANGER = 30.0;
    public static final double RAIN_ACTUAL_DANGER = 0.1; // Cualquier lluvia es peligrosa
    
    // VISIBILIDAD (km)
    public static final double VISIBILITY_MIN_SAFE = 5.0;
    public static final double VISIBILITY_CRITICAL = 2.0;
    
    // GEOMETRÍA SATELITAL (PDOP)
    public static final double PDOP_SAFE = 3.0;
    public static final double PDOP_CAUTION = 6.0;
    public static final double PDOP_DANGER = 8.0;
    
    // ACTIVIDAD SOLAR (Kp)
    public static final double KP_CAUTION = 4.0;
    public static final double KP_DANGER = 5.0;
    
    // SISTEMA DE PUNTUACIÓN (Scoring)
    // Se asignan puntos de riesgo para determinar el nivel final si no hay un factor crítico directo.
    public static final int SCORE_YELLOW = 3;
    public static final int SCORE_ORANGE = 6;
    public static final int SCORE_RED = 10;
}
