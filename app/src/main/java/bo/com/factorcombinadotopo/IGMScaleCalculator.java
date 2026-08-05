package bo.com.factorcombinadotopo;

public class IGMScaleCalculator {

    /**
     * Factor de escala puntual k desde coordenadas geográficas.
     */
    public static double calculateScaleFactor(double latRad, double lonRad, double lon0Rad, double k0) {
        double cosLat = Math.cos(latRad);
        double eta2 = IGMConstants.WGS84_E_PRIME_SQ * cosLat * cosLat;
        double a = cosLat * (lonRad - lon0Rad);
        double t2 = Math.tan(latRad) * Math.tan(latRad);

        return k0 * (1.0 + (1.0 + eta2) * a * a / 2.0
                + (5.0 - 4.0 * t2 + 42.0 * eta2 + 13.0 * eta2 * eta2) * Math.pow(a, 4) / 24.0);
    }

    /**
     * Aproximación rápida usando solo la distancia X al meridiano central.
     */
    public static double calculateScaleFactorApprox(double x, double rm, double k0) {
        double ratio = x / rm;
        return k0 * (1.0 + ratio * ratio / 2.0 + Math.pow(ratio, 4) / 24.0);
    }
}
