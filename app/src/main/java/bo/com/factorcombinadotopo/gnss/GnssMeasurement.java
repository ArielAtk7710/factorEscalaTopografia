package bo.com.factorcombinadotopo.gnss;

/**
 * Representa una medición de posición procesada por la capa GNSS.
 * Mantiene la precisión double absoluta requerida por factorEscala.
 */
public class GnssMeasurement {
    public final double latitude;
    public final double longitude;
    public final double altitude;
    public final double accuracy;
    public final long timestamp;
    public final boolean isOutlier;
    public final boolean isFiltered;

    public GnssMeasurement(double latitude, double longitude, double altitude, double accuracy, long timestamp, boolean isOutlier, boolean isFiltered) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.isOutlier = isOutlier;
        this.isFiltered = isFiltered;
    }
}
