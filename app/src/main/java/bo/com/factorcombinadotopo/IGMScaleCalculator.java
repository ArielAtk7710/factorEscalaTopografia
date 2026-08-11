package bo.com.factorcombinadotopo;

/**
 * Factor de escala puntual en proyección UTM.
 * Fórmulas idénticas a calcular5_1 y calcular5_2 del JS.
 */
public class IGMScaleCalculator {

    /**
     * Factor de escala k desde coordenadas geográficas (fórmula exacta del JS).
     */
    public static double calculateScaleFactor(double latRad, double lonRad, double lon0Rad, double ko,
                                               IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double b = ellip.b;
        double e_2_2 = (a * a - b * b) / (b * b);  // Segunda excentricidad al cuadrado

        double cesc = e_2_2 * Math.cos(latRad) * Math.cos(latRad);
        double tesc = Math.tan(latRad) * Math.tan(latRad);
        double Aesc = (lon0Rad - lonRad) * Math.cos(latRad);

        double k = ko * (1.0 + (1.0 + cesc) * (Math.pow(Aesc, 2) / 2.0)
                + (5.0 - 4.0 * tesc + 42.0 * cesc + 13.0 * Math.pow(cesc, 2) - 23.0 * e_2_2) * (Math.pow(Aesc, 4) / 24.0)
                + (61.0 - 148.0 * tesc + 16.0 * Math.pow(tesc, 2)) * (Math.pow(Aesc, 6) / 720.0));

        return k;
    }

    /**
     * Aproximación rápida usando distancia X al meridiano central (idéntica al JS).
     */
    public static double calculateScaleFactorApprox(double x, double rm, double ko) {
        double ratio = x / rm;
        return ko * (1.0 + ratio * ratio / 2.0 + Math.pow(ratio, 4) / 24.0);
    }

    /**
     * Koh = (R + h) / R (recíproco del factor de elevación, del JS del mapa).
     */
    public static double calculateKoh(double meanRadius, double altitude) {
        return (meanRadius + altitude) / meanRadius;
    }
}
