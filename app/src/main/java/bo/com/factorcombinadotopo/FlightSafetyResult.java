package bo.com.factorcombinadotopo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el resultado completo del análisis de seguridad de vuelo.
 */
public class FlightSafetyResult {
    public FlightSafetyLevel nivel;
    public int colorRes;
    public int mainIconRes; // Icono representativo del riesgo principal
    public String titulo;
    public String mensajeBreve;
    public String detalle;
    public String recomendacion;
    public List<String> causas = new ArrayList<>();
    
    // Información de ventana óptima
    public String ventanaOptima;

    public FlightSafetyResult() {
        this.nivel = FlightSafetyLevel.VERDE;
    }
}
