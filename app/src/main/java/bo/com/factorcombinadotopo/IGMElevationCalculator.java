package bo.com.factorcombinadotopo;

public class IGMElevationCalculator {

    /** Radio de curvatura en el primer vertical (N) */
    public static double radiusPrimeVertical(double latRad) {
        double a = IGMConstants.WGS84_A;
        double eSq = IGMConstants.WGS84_E_SQ;
        return a / Math.sqrt(1.0 - eSq * Math.sin(latRad) * Math.sin(latRad));
    }

    /** Radio de curvatura meridional (M) */
    public static double radiusMeridional(double latRad) {
        double a = IGMConstants.WGS84_A;
        double eSq = IGMConstants.WGS84_E_SQ;
        return a * (1.0 - eSq) / Math.pow(1.0 - eSq * Math.sin(latRad) * Math.sin(latRad), 1.5);
    }

    /** Radio medio de Gauss: Rm = √(M·N) */
    public static double calculateMeanRadius(double latRad) {
        return Math.sqrt(radiusMeridional(latRad) * radiusPrimeVertical(latRad));
    }

    /** Factor de elevación: Kh = Rm / (Rm + h) */
    public static double calculateElevationFactor(double meanRadius, double altitude) {
        return meanRadius / (meanRadius + altitude);
    }
}