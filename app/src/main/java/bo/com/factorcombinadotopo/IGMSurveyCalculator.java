package bo.com.factorcombinadotopo;

/**
 * Topografía plana: azimut, rumbo, problema directo/inverso.
 * Fórmulas idénticas a calcular_1_007 .. calcular_7_007 del JS.
 */
public class IGMSurveyCalculator {

    /**
     * Azimut inverso (±180°).
     */
    public static double inverseAzimuth(double azimuth) {
        double inv = azimuth + 180.0;
        return inv >= 360.0 ? inv - 360.0 : inv;
    }

    /**
     * Rumbo con dirección cardinal (NE/NW/SE/SW).
     */
    public static String toBearing(double azimuth) {
        if (azimuth < 0 || azimuth > 360) {
            azimuth = ((azimuth % 360.0) + 360.0) % 360.0;
        }
        double angle = 0.0;
        String dir = "";
        if (azimuth <= 90.0) {
            angle = 90.0 - azimuth;
            dir = "N" + String.format(java.util.Locale.US, "%.2f", angle) + "°E";
        } else if (azimuth <= 180.0) {
            angle = azimuth - 90.0;
            dir = "S" + String.format(java.util.Locale.US, "%.2f", angle) + "°E";
        } else if (azimuth <= 270.0) {
            angle = 270.0 - azimuth;
            dir = "S" + String.format(java.util.Locale.US, "%.2f", angle) + "°W";
        } else {
            angle = azimuth - 270.0;
            dir = "N" + String.format(java.util.Locale.US, "%.2f", angle) + "°W";
        }
        return dir;
    }

    /**
     * Problema directo: punto + distancia + azimut -> nuevo punto.
     */
    public static double[] direct(double x0, double y0, double dist, double azimuth) {
        double azRad = Math.toRadians(azimuth);
        double x1 = x0 + dist * Math.sin(azRad);
        double y1 = y0 + dist * Math.cos(azRad);
        return new double[]{x1, y1};
    }

    /**
     * Problema inverso: 2 puntos -> distancia + azimut.
     */
    public static InverseResult inverse(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double az = Math.toDegrees(Math.atan2(dx, dy));
        if (az < 0) az += 360.0;

        InverseResult r = new InverseResult();
        r.distance = dist;
        r.azimuth = az;
        return r;
    }

    public static class InverseResult {
        public double distance;
        public double azimuth;
    }
}
