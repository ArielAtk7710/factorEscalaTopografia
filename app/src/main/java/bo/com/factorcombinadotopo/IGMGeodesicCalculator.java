package bo.com.factorcombinadotopo;

/**
 * Cálculos geodésicos: Bowring y Vincenty.
 * Fórmulas idénticas a calcular_1_004, calcular_2_004,
 * calcular_3_004 y calcular_4_004 del JS.
 */
public class IGMGeodesicCalculator {

    public static class GeoResult {
        public double lat;
        public double lon;
        public double azimuthInverse;
        public double distance;
        public double azimuthDirect;
    }

    /**
     * Bowring Directo (calcular_1_004 del JS).
     */
    public static GeoResult bowringDirect(double lat1, double lon1, double azimuth,
                                          double distance, IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double f = ellip.f;
        double b = ellip.b;

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double azRad = Math.toRadians(azimuth);

        double u1 = Math.atan((1.0 - f) * Math.tan(lat1Rad));
        double sigma1 = Math.atan(Math.tan(u1) / Math.cos(azRad));
        double sinAlpha = Math.cos(u1) * Math.sin(azRad);
        double cos2Alpha = 1.0 - sinAlpha * sinAlpha;
        double u2 = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + (u2 / 16384.0) * (4096.0 + u2 * (-768.0 + u2 * (320.0 - 175.0 * u2)));
        double B = (u2 / 1024.0) * (256.0 + u2 * (-128.0 + u2 * (74.0 - 47.0 * u2)));
        double sigma = distance / (b * A);

        for (int i = 0; i < 4; i++) {
            double sigmaM2 = 2.0 * sigma1 + sigma;
            double dSigma = B * Math.sin(sigma) * (Math.cos(sigmaM2)
                    + 0.25 * B * (Math.cos(sigma) * (-1.0 + 2.0 * Math.cos(sigmaM2) * Math.cos(sigmaM2))
                    - (B / 6.0) * Math.cos(sigmaM2) * (-3.0 + 4.0 * Math.sin(sigma) * Math.sin(sigma))
                    * (-3.0 + 4.0 * Math.cos(sigmaM2) * Math.cos(sigmaM2))));
            sigma = distance / (b * A) + dSigma;
        }

        double lat2Rad = Math.atan2(
                Math.sin(u1) * Math.cos(sigma) + Math.cos(u1) * Math.sin(sigma) * Math.cos(azRad),
                (1.0 - f) * Math.sqrt(sinAlpha * sinAlpha + Math.pow(Math.sin(u1) * Math.sin(sigma)
                        - Math.cos(u1) * Math.cos(sigma) * Math.cos(azRad), 2)));

        double dLon = Math.atan2(Math.sin(sigma) * Math.sin(azRad),
                Math.cos(u1) * Math.cos(sigma) - Math.sin(u1) * Math.sin(sigma) * Math.cos(azRad));
        double C = (f / 16.0) * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));
        double lon2Rad = lon1Rad + dLon - (1.0 - C) * f * sinAlpha * (sigma + C * Math.sin(sigma)
                * (Math.cos(2.0 * sigma1 + sigma) + C * Math.cos(sigma)
                * (-1.0 + 2.0 * Math.cos(2.0 * sigma1 + sigma) * Math.cos(2.0 * sigma1 + sigma))));

        double az2Rad = Math.atan2(sinAlpha, -Math.sin(u1) * Math.sin(sigma)
                + Math.cos(u1) * Math.cos(sigma) * Math.cos(azRad));

        GeoResult r = new GeoResult();
        r.lat = Math.toDegrees(lat2Rad);
        r.lon = Math.toDegrees(lon2Rad);
        r.azimuthInverse = Math.toDegrees(az2Rad);
        return r;
    }

    /**
     * Bowring Inverso (calcular_2_004 del JS).
     */
    public static GeoResult bowringInverse(double lat1, double lon1, double lat2, double lon2,
                                           IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double f = ellip.f;
        double b = ellip.b;

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double u1 = Math.atan((1.0 - f) * Math.tan(lat1Rad));
        double u2 = Math.atan((1.0 - f) * Math.tan(lat2Rad));
        double dLon = lon2Rad - lon1Rad;

        double sinU1 = Math.sin(u1), cosU1 = Math.cos(u1);
        double sinU2 = Math.sin(u2), cosU2 = Math.cos(u2);

        double sigma = 0.0, sinSigma = 0.0, cosSigma = 0.0;
        double sinAlpha = 0.0, cos2Alpha = 0.0;
        double cos2SigmaM = 0.0;
        double lambda = dLon;

        for (int i = 0; i < 4; i++) {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(Math.pow(cosU2 * sinLambda, 2)
                    + Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * cosLambda, 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1.0 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cos2Alpha;
            double C = (f / 16.0) * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));
            lambda = dLon + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
        }

        double u2b = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + (u2b / 16384.0) * (4096.0 + u2b * (-768.0 + u2b * (320.0 - 175.0 * u2b)));
        double B = (u2b / 1024.0) * (256.0 + u2b * (-128.0 + u2b * (74.0 - 47.0 * u2b)));
        double dSigma = B * sinSigma * (cos2SigmaM + 0.25 * B * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                - (B / 6.0) * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma)
                * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - dSigma);

        double az1 = Math.atan2(cosU2 * Math.sin(lambda), cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(lambda));
        double az2 = Math.atan2(cosU1 * Math.sin(lambda), -sinU1 * cosU2 + cosU1 * sinU2 * Math.cos(lambda));

        GeoResult r = new GeoResult();
        r.distance = dist;
        r.azimuthDirect = Math.toDegrees(az1);
        r.azimuthInverse = Math.toDegrees(az2);
        return r;
    }

    /**
     * Vincenty Directo (calcular_3_004 del JS).
     * Iterativo con while (sigma - lastSigma > 1e-15).
     */
    public static GeoResult vincentyDirect(double lat1, double lon1, double azimuth,
                                           double distance, IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double f = ellip.f;
        double b = ellip.b;

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double azRad = Math.toRadians(azimuth);

        double u1 = Math.atan((1.0 - f) * Math.tan(lat1Rad));
        double sigma1 = Math.atan(Math.tan(u1) / Math.cos(azRad));
        double sinAlpha = Math.cos(u1) * Math.sin(azRad);
        double cos2Alpha = 1.0 - sinAlpha * sinAlpha;
        double u2 = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + (u2 / 16384.0) * (4096.0 + u2 * (-768.0 + u2 * (320.0 - 175.0 * u2)));
        double B = (u2 / 1024.0) * (256.0 + u2 * (-128.0 + u2 * (74.0 - 47.0 * u2)));

        double sigma = distance / (b * A);
        double lastSigma;
        int iter = 0;
        do {
            lastSigma = sigma;
            double sigmaM2 = 2.0 * sigma1 + sigma;
            double dSigma = B * Math.sin(sigma) * (Math.cos(sigmaM2)
                    + 0.25 * B * (Math.cos(sigma) * (-1.0 + 2.0 * Math.cos(sigmaM2) * Math.cos(sigmaM2))
                    - (B / 6.0) * Math.cos(sigmaM2) * (-3.0 + 4.0 * Math.sin(sigma) * Math.sin(sigma))
                    * (-3.0 + 4.0 * Math.cos(sigmaM2) * Math.cos(sigmaM2))));
            sigma = distance / (b * A) + dSigma;
        } while (Math.abs(sigma - lastSigma) > 1e-15 && ++iter < 100);

        double lat2Rad = Math.atan2(
                Math.sin(u1) * Math.cos(sigma) + Math.cos(u1) * Math.sin(sigma) * Math.cos(azRad),
                (1.0 - f) * Math.sqrt(sinAlpha * sinAlpha + Math.pow(Math.sin(u1) * Math.sin(sigma)
                        - Math.cos(u1) * Math.cos(sigma) * Math.cos(azRad), 2)));

        double dLon = Math.atan2(Math.sin(sigma) * Math.sin(azRad),
                Math.cos(u1) * Math.cos(sigma) - Math.sin(u1) * Math.sin(sigma) * Math.cos(azRad));
        double C = (f / 16.0) * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));
        double lon2Rad = lon1Rad + dLon - (1.0 - C) * f * sinAlpha * (sigma + C * Math.sin(sigma)
                * (Math.cos(2.0 * sigma1 + sigma) + C * Math.cos(sigma)
                * (-1.0 + 2.0 * Math.cos(2.0 * sigma1 + sigma) * Math.cos(2.0 * sigma1 + sigma))));

        double az2Rad = Math.atan2(sinAlpha, -Math.sin(u1) * Math.sin(sigma)
                + Math.cos(u1) * Math.cos(sigma) * Math.cos(azRad));

        GeoResult r = new GeoResult();
        r.lat = Math.toDegrees(lat2Rad);
        r.lon = Math.toDegrees(lon2Rad);
        r.azimuthInverse = Math.toDegrees(az2Rad);
        return r;
    }

    /**
     * Vincenty Inverso (calcular_4_004 del JS).
     */
    public static GeoResult vincentyInverse(double lat1, double lon1, double lat2, double lon2,
                                            IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double f = ellip.f;
        double b = ellip.b;

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double u1 = Math.atan((1.0 - f) * Math.tan(lat1Rad));
        double u2 = Math.atan((1.0 - f) * Math.tan(lat2Rad));
        double dLon = lon2Rad - lon1Rad;

        double sinU1 = Math.sin(u1), cosU1 = Math.cos(u1);
        double sinU2 = Math.sin(u2), cosU2 = Math.cos(u2);

        double sigma = 0.0, sinSigma = 0.0, cosSigma = 0.0;
        double sinAlpha = 0.0, cos2Alpha = 0.0;
        double cos2SigmaM = 0.0;
        double lambda = dLon;
        double lastLambda;
        int iter = 0;

        do {
            lastLambda = lambda;
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(Math.pow(cosU2 * sinLambda, 2)
                    + Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * cosLambda, 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1.0 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cos2Alpha;
            double C = (f / 16.0) * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));
            lambda = dLon + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lastLambda) > 1e-15 && ++iter < 100);

        double u2b = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + (u2b / 16384.0) * (4096.0 + u2b * (-768.0 + u2b * (320.0 - 175.0 * u2b)));
        double B = (u2b / 1024.0) * (256.0 + u2b * (-128.0 + u2b * (74.0 - 47.0 * u2b)));
        double dSigma = B * sinSigma * (cos2SigmaM + 0.25 * B * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                - (B / 6.0) * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma)
                * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - dSigma);

        double az1 = Math.atan2(cosU2 * Math.sin(lambda), cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(lambda));
        double az2 = Math.atan2(cosU1 * Math.sin(lambda), -sinU1 * cosU2 + cosU1 * sinU2 * Math.cos(lambda));

        GeoResult r = new GeoResult();
        r.distance = dist;
        r.azimuthDirect = Math.toDegrees(az1);
        r.azimuthInverse = Math.toDegrees(az2);
        return r;
    }
}
