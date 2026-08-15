package bo.com.factorcombinadotopo;

import java.util.List;
import org.osmdroid.util.GeoPoint;

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
     * Formatea un factor topográfico (9 decimales EXACTOS).
     */
    public static String formatFactor(double value) {
        return String.format(java.util.Locale.US, "%.9f", value);
    }

    /**
     * Convierte una coordenada decimal a formato GMS (Grados, Minutos, Segundos).
     * Delegando el cálculo a la clase estandarizada de la IGM.
     */
    public static String toDMS(double decimal, boolean isLatitude) {
        IGMCoordinate.DmsCoordinate dms = IGMCoordinateFormatter.toDms(decimal, isLatitude);
        // Formato visual profesional: XXº XX' XX.XXX''
        return (decimal < 0 ? "-" : "") + dms.degrees + "º " + dms.minutes + "' " + formatCoord(dms.seconds) + "''";
    }

    /**
     * Formatea una coordenada geográfica en grados decimales (6 decimales).
     */
    public static String formatLatLon(double value) {
        return String.format(java.util.Locale.US, "%.6f", value);
    }

    /**
     * Calcula el área geodésica de un polígono en metros cuadrados.
     * Basado en el algoritmo de área sobre una esfera (aprox. WGS84).
     */
    public static double calculateArea(List<GeoPoint> points) {
        if (points.size() < 3) return 0.0;
        double area = 0.0;
        double radius = 6378137.0; // Radio ecuatorial WGS84 en metros
        
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % points.size());
            
            double lat1 = Math.toRadians(p1.getLatitude());
            double lon1 = Math.toRadians(p1.getLongitude());
            double lat2 = Math.toRadians(p2.getLatitude());
            double lon2 = Math.toRadians(p2.getLongitude());
            
            area += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        
        area = Math.abs(area * radius * radius / 2.0);
        return area;
    }

    /**
     * Formatea el área para mostrar m² o Hectáreas (ha).
     */
    public static String formatArea(double areaM2) {
        if (areaM2 < 10000.0) {
            return String.format(java.util.Locale.US, "Área: %.2f m²", areaM2);
        } else {
            return String.format(java.util.Locale.US, "Área: %.3f ha", areaM2 / 10000.0);
        }
    }

    /**
     * Formatea la distancia para mostrar metros (m) o Kilómetros (km).
     */
    public static String formatDistance(double meters) {
        if (meters < 1000.0) {
            return String.format(java.util.Locale.US, "%.2f m", meters);
        } else {
            return String.format(java.util.Locale.US, "%.3f km", meters / 1000.0);
        }
    }
}
