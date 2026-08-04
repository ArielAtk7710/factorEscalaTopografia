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
}
