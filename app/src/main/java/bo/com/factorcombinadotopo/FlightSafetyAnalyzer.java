package bo.com.factorcombinadotopo;

import android.content.Context;
import java.util.List;
import java.util.Locale;

/**
 * Motor de análisis multivariable para seguridad de vuelo de drones.
 * Utiliza un sistema de puntuación y reglas de prioridad técnica.
 */
public class FlightSafetyAnalyzer {

    private final Context context;

    public FlightSafetyAnalyzer(Context context) {
        this.context = context;
    }

    public FlightSafetyResult analyze(WeatherManager.SafetyStatus weatherStatus, List<WeatherManager.HourlyStatus> hourlyList, double pdop) {
        FlightSafetyResult result = new FlightSafetyResult();
        int totalRiskScore = 0;
        boolean hasCriticalDanger = false;
        
        // Icono por defecto (Escudo o Info)
        result.mainIconRes = R.drawable.ic_shield_pro;

        // 1. ANALIZAR VIENTO Y RÁFAGAS
        if (weatherStatus.wind120 > FlightSafetyThresholds.WIND_DANGER || weatherStatus.gusts > FlightSafetyThresholds.GUSTS_DANGER) {
            result.causas.add(context.getString(R.string.safety_cause_wind_critical));
            totalRiskScore += 10;
            hasCriticalDanger = true;
            result.mainIconRes = R.drawable.ic_wind_pro;
        } else if (weatherStatus.wind120 > FlightSafetyThresholds.WIND_CAUTION || weatherStatus.gusts > FlightSafetyThresholds.GUSTS_CAUTION) {
            result.causas.add(context.getString(R.string.safety_cause_wind_high));
            totalRiskScore += 5;
            result.mainIconRes = R.drawable.ic_wind_pro;
        } else if (weatherStatus.wind120 > FlightSafetyThresholds.WIND_SAFE) {
            totalRiskScore += 2;
        }

        // 2. ANALIZAR LLUVIA Y PROBABILIDAD
        if (weatherStatus.rain > FlightSafetyThresholds.RAIN_ACTUAL_DANGER || weatherStatus.rainProbability > FlightSafetyThresholds.RAIN_PROB_DANGER) {
            result.causas.add(context.getString(R.string.safety_cause_rain_imminent));
            totalRiskScore += 8;
            if (weatherStatus.rain > 0) hasCriticalDanger = true;
            result.mainIconRes = R.drawable.ic_rain_drop_pro;
        } else if (weatherStatus.rainProbability > FlightSafetyThresholds.RAIN_PROB_CAUTION) {
            result.causas.add(context.getString(R.string.safety_cause_rain_mod));
            totalRiskScore += 3;
            result.mainIconRes = R.drawable.ic_rain_drop_pro;
        }

        // 3. ANALIZAR VISIBILIDAD
        if (weatherStatus.visibility < FlightSafetyThresholds.VISIBILITY_CRITICAL) {
            result.causas.add(context.getString(R.string.safety_cause_vis_critical));
            totalRiskScore += 10;
            hasCriticalDanger = true;
            result.mainIconRes = R.drawable.ic_visibility_pro;
        } else if (weatherStatus.visibility < FlightSafetyThresholds.VISIBILITY_MIN_SAFE) {
            result.causas.add(context.getString(R.string.safety_cause_vis_low));
            totalRiskScore += 4;
            result.mainIconRes = R.drawable.ic_visibility_pro;
        }

        // 4. ANALIZAR ACTIVIDAD SOLAR (KP)
        if (weatherStatus.kp >= FlightSafetyThresholds.KP_DANGER) {
            result.causas.add(context.getString(R.string.safety_cause_solar_high, weatherStatus.kp));
            totalRiskScore += 6;
            result.mainIconRes = R.drawable.ic_shield_pro;
        } else if (weatherStatus.kp >= FlightSafetyThresholds.KP_CAUTION) {
            result.causas.add(context.getString(R.string.safety_cause_solar_mod));
            totalRiskScore += 2;
        }

        // 5. ANALIZAR GEOMETRÍA SATELITAL (PDOP)
        if (pdop > 0) { // Solo si el dato es válido
            if (pdop > FlightSafetyThresholds.PDOP_DANGER) {
                result.causas.add(context.getString(R.string.safety_cause_pdop_high));
                totalRiskScore += 7;
                result.mainIconRes = R.drawable.ic_map_pin; // O un icono de satélite si existiera
            } else if (pdop > FlightSafetyThresholds.PDOP_CAUTION) {
                result.causas.add(context.getString(R.string.safety_cause_pdop_def));
                totalRiskScore += 3;
            }
        }

        // 6. DETERMINAR NIVEL FINAL BASADO EN SCORE Y PELIGROS CRÍTICOS
        if (hasCriticalDanger || totalRiskScore >= FlightSafetyThresholds.SCORE_RED) {
            result.nivel = FlightSafetyLevel.ROJO;
            result.colorRes = R.color.flight_red;
            
            if (weatherStatus.rain > 0 || weatherStatus.rainProbability > FlightSafetyThresholds.RAIN_PROB_DANGER) {
                result.titulo = context.getString(R.string.safety_storm_title);
                result.detalle = context.getString(R.string.safety_storm_desc);
                result.recomendacion = context.getString(R.string.safety_storm_rec);
            } else {
                result.titulo = context.getString(R.string.safety_wind_title);
                result.detalle = context.getString(R.string.safety_wind_desc);
                result.recomendacion = context.getString(R.string.safety_wind_rec);
            }
            result.mensajeBreve = determineShortMessage(result.causas, context.getString(R.string.safety_short_msg_danger));
            
        } else if (totalRiskScore >= FlightSafetyThresholds.SCORE_ORANGE) {
            result.nivel = FlightSafetyLevel.NARANJA;
            result.colorRes = R.color.flight_orange;
            
            if (weatherStatus.kp >= FlightSafetyThresholds.KP_CAUTION) {
                result.titulo = context.getString(R.string.safety_solar_title);
                result.detalle = context.getString(R.string.safety_solar_desc);
                result.recomendacion = context.getString(R.string.safety_solar_rec);
            } else if (pdop > FlightSafetyThresholds.PDOP_CAUTION) {
                result.titulo = context.getString(R.string.safety_geometry_title);
                result.detalle = context.getString(R.string.safety_geometry_desc);
                result.recomendacion = context.getString(R.string.safety_geometry_rec);
            } else {
                result.titulo = context.getString(R.string.safety_risk_combined_title);
                result.detalle = context.getString(R.string.safety_risk_combined_desc);
                result.recomendacion = context.getString(R.string.safety_risk_combined_rec);
            }
            result.mensajeBreve = determineShortMessage(result.causas, context.getString(R.string.safety_short_msg_caution));

        } else if (totalRiskScore >= FlightSafetyThresholds.SCORE_YELLOW) {
            result.nivel = FlightSafetyLevel.AMARILLO;
            result.colorRes = R.color.flight_yellow;
            result.titulo = context.getString(R.string.label_warning); 
            result.mensajeBreve = determineShortMessage(result.causas, context.getString(R.string.safety_short_msg_variable));
            result.detalle = context.getString(R.string.msg_weather_caution);
            result.recomendacion = context.getString(R.string.safety_msg_kp_high); // Reusing a sensible rec
        } else {
            result.nivel = FlightSafetyLevel.VERDE;
            result.colorRes = R.color.flight_green;
            result.titulo = context.getString(R.string.safety_optimal_title);
            result.mensajeBreve = context.getString(R.string.safety_short_msg_safe);
            result.detalle = context.getString(R.string.safety_optimal_desc);
            result.recomendacion = context.getString(R.string.safety_optimal_rec);
            result.mainIconRes = R.drawable.ic_success_toast;
        }

        // 7. DETECTAR VENTANA ÓPTIMA EN EL PRONÓSTICO
        result.ventanaOptima = findOptimalWindow(hourlyList);

        return result;
    }

    private String determineShortMessage(List<String> causas, String defaultMsg) {
        if (causas.isEmpty()) return defaultMsg;
        return context.getString(R.string.label_warning) + ". " + causas.get(0) + ".";
    }

    private String findOptimalWindow(List<WeatherManager.HourlyStatus> list) {
        if (list == null || list.isEmpty()) return context.getString(R.string.safety_optimal_window_loading);
        
        int bestStart = -1;
        int count = 0;
        int maxWindow = 0;
        int finalStart = -1;
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).level == WeatherManager.SafetyLevel.GREEN) {
                if (bestStart == -1) bestStart = i;
                count++;
            } else {
                if (count > maxWindow) {
                    maxWindow = count;
                    finalStart = bestStart;
                }
                bestStart = -1;
                count = 0;
            }
        }
        if (count > maxWindow) {
            maxWindow = count;
            finalStart = bestStart;
        }

        if (finalStart != -1 && maxWindow >= 2) {
            return String.format(context.getString(R.string.safety_optimal_window_prefix), 
                    list.get(finalStart).time, 
                    list.get(finalStart + maxWindow - 1).time);
        } else {
            return context.getString(R.string.safety_optimal_window_none);
        }
    }
}
