package bo.com.factorcombinadotopo;

/**
 * Radios de curvatura y factor de elevación.
 * Fórmulas idénticas a calcular5_1 y calcular5_2 del JS.
 */
public class IGMElevationCalculator {

    /**
     * Radio primer vertical N (nn en el JS).
     */
    public static double radiusPrimeVertical(double latRad, IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double e = (a * a - ellip.b * ellip.b) / (a * a);  // e del JS
        return a / Math.sqrt(1.0 - e * Math.pow(Math.sin(latRad), 2));
    }

    /**
     * Radio meridional M (mm en el JS).
     */
    public static double radiusMeridional(double latRad, IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double e = (a * a - ellip.b * ellip.b) / (a * a);
        return (a * (1.0 - e)) / Math.pow(1.0 - e * Math.pow(Math.sin(latRad), 2), 1.5);
    }

    /**
     * Radio medio de Gauss Rm = sqrt(N * M) (rm en el JS).
     */
    public static double calculateMeanRadius(double latRad, IGMConstants.Ellipsoid ellip) {
        double nn = radiusPrimeVertical(latRad, ellip);
        double mm = radiusMeridional(latRad, ellip);
        return Math.sqrt(nn * mm);
    }

    /**
     * Factor de elevación Kh = Rm / (Rm + h) (idéntico al JS).
     */
    public static double calculateElevationFactor(double meanRadius, double altitude) {
        return meanRadius / (meanRadius + altitude);
    }
}
