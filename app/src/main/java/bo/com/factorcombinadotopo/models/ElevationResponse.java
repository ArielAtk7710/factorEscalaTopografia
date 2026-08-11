package bo.com.factorcombinadotopo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo de datos para la respuesta de la API de Elevación de Open-Meteo.
 */
public class ElevationResponse {
    
    @SerializedName("elevation")
    public List<Double> elevation;

    /**
     * Retorna la primera elevación de la lista o 0.0 si está vacía.
     */
    public double getFirstElevation() {
        if (elevation != null && !elevation.isEmpty()) {
            return elevation.get(0);
        }
        return 0.0;
    }
}
