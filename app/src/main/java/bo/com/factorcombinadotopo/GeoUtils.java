package bo.com.factorcombinadotopo;

public class GeoUtils {

    /**
     * Calcula la zona UTM y el hemisferio basándose en coordenadas geográficas.
     * Retorna un formato profesional con identificación regional.
     */
    public static String getUtmZoneFormatted(double lat, double lon) {
        int zone = (int) Math.floor((lon + 180) / 6) + 1;
        char hemisphere = (lat >= 0) ? 'N' : 'S';
        
        String zoneStr = zone + String.valueOf(hemisphere);
        String region = getRegionLabel(lat, lon, zone, hemisphere);

        return "Zona " + zoneStr + (region.isEmpty() ? "" : " (" + region + ")");
    }

    /**
     * Identifica regiones de interés topográfico (Bolivia y Sudamérica).
     */
    private static String getRegionLabel(double lat, double lon, int zone, char hemisphere) {
        // Validación para Bolivia (Zonas 19S, 20S, 21S dentro de límites aproximados)
        if (hemisphere == 'S' && lat <= -9.6 && lat >= -22.9 && lon >= -69.6 && lon <= -57.4) {
            if (zone == 19 || zone == 20 || zone == 21) {
                return "Bolivia";
            }
        }

        // Validación para Sudamérica (Aproximada)
        if (lat <= 13.0 && lat >= -56.0 && lon >= -82.0 && lon <= -34.0) {
            return "Sudamérica";
        }

        return "";
    }

    /**
     * Retorna solo el número de zona UTM.
     */
    public static int getUtmZone(double lon) {
        return (int) Math.floor((lon + 180) / 6) + 1;
    }

    /**
     * Retorna solo el carácter del hemisferio ('N' o 'S').
     */
    public static char getUtmHemisphere(double lat) {
        return (lat >= 0) ? 'N' : 'S';
    }

    /**
     * Retorna la longitud del meridiano central para una zona dada.
     */
    public static double getCentralMeridian(int zone) {
        return (zone * 6) - 183;
    }

    /**
     * Formatea un factor y su equivalente en PPM (Partes por Millón).
     * Asegura el uso de punto decimal para compatibilidad con ingeniería.
     */
    public static String formatFactorWithPpm(double factor) {
        return formatFactor(factor) + " (" + Math.round((factor - 1.0) * 1000000.0) + " PPM)";
    }

    /**
     * Formatea la presión en formato dual para visualización y reportes.
     */
    public static String formatPressureDual(double mmHg) {
        double hPa = mmHg * (1013.25 / 759.99); // Relación estándar basada en IGMConstants
        return String.format(java.util.Locale.US, "%.3f mmHg | %.3f hPa", mmHg, hPa);
    }

    /**
     * Formatea una coordenada UTM o Altura (3 decimales).
     */
    public static String formatCoord(double value) {
        return String.format(java.util.Locale.US, "%.3f", value);
    }

    /**
     * Formatea un factor topográfico (9 decimales).
     */
    public static String formatFactor(double value) {
        return String.format(java.util.Locale.US, "%.9f", value);
    }

    /**
     * Formatea una coordenada geográfica en grados decimales (6 decimales).
     */
    public static String formatLatLon(double value) {
        return String.format(java.util.Locale.US, "%.6f", value);
    }
}
