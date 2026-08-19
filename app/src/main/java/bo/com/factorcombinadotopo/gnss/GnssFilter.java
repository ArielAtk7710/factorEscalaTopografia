package bo.com.factorcombinadotopo.gnss;

import android.location.Location;
import java.util.LinkedList;

/**
 * Capa de procesamiento GNSS avanzado para factorEscala.
 * Implementa detección de outliers por velocidad y promedio ponderado por precisión.
 */
public class GnssFilter {

    private static final int BUFFER_SIZE = 10;
    private static final double MAX_VELOCITY_MPS = 30.0; // 108 km/h (Máximo para drones)
    private static final double MIN_ACCURACY_FLOOR = 0.5; // Evitar división por cero

    private final LinkedList<Location> buffer = new LinkedList<>();
    private Location lastValidLocation = null;

    /**
     * Procesa una ubicación cruda de Android y devuelve una versión estabilizada.
     */
    public GnssMeasurement filter(Location raw) {
        if (raw == null) return null;

        boolean isOutlier = detectOutlier(raw);
        
        if (!isOutlier) {
            buffer.add(new Location(raw));
            if (buffer.size() > BUFFER_SIZE) {
                buffer.removeFirst();
            }
            lastValidLocation = raw;
        }

        // Si el buffer está vacío (primera medición o todos outliers), devolvemos el raw como outlier
        if (buffer.isEmpty()) {
            return new GnssMeasurement(raw.getLatitude(), raw.getLongitude(), raw.getAltitude(), 
                    raw.getAccuracy(), raw.getTime(), isOutlier, false);
        }

        // Calcular Promedio Ponderado: X = sum(w_i * X_i) / sum(w_i)
        // donde w_i = 1 / (accuracy_i ^ 2)
        double sumWeight = 0;
        double sumLat = 0;
        double sumLon = 0;
        double sumAlt = 0;
        double sumAcc = 0;

        for (Location loc : buffer) {
            double acc = Math.max(loc.getAccuracy(), MIN_ACCURACY_FLOOR);
            double w = 1.0 / (acc * acc);

            sumWeight += w;
            sumLat += loc.getLatitude() * w;
            sumLon += loc.getLongitude() * w;
            sumAlt += loc.getAltitude() * w;
            sumAcc += loc.getAccuracy() * w;
        }

        return new GnssMeasurement(
                sumLat / sumWeight,
                sumLon / sumWeight,
                sumAlt / sumWeight,
                sumAcc / sumWeight, // Accuracy estimada del promedio
                raw.getTime(),
                isOutlier,
                buffer.size() > 1 // Marcamos como filtrado si hay más de 1 muestra
        );
    }

    private boolean detectOutlier(Location current) {
        if (lastValidLocation == null) return false;

        double distance = current.distanceTo(lastValidLocation);
        double timeDeltaSec = (current.getTime() - lastValidLocation.getTime()) / 1000.0;

        if (timeDeltaSec <= 0) return false;

        double velocity = distance / timeDeltaSec;
        return velocity > MAX_VELOCITY_MPS;
    }

    public void reset() {
        buffer.clear();
        lastValidLocation = null;
    }
}
