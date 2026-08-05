package bo.com.factorcombinadotopo;

public class IGMUtmConverter {

    public static class GeoPoint {
        public double lat;
        public double lon;
        public GeoPoint(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class UtmPoint {
        public double easting;
        public double northing;
        public int zone;
        public char hemisphere;
        public UtmPoint(double easting, double northing, int zone, char hemisphere) {
            this.easting = easting;
            this.northing = northing;
            this.zone = zone;
            this.hemisphere = hemisphere;
        }
    }

    public static double getCentralMeridian(int zone) {
        return (zone * 6.0) - 183.0;
    }

    /**
     * Directa: Geográficas (WGS84) → UTM (Redfearn - Precisión Profesional)
     */
    public static UtmPoint forward(double lat, double lon) {
        int zone = (int) Math.floor((lon + 180.0) / 6.0) + 1;
        char hemisphere = (lat >= 0) ? 'N' : 'S';
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0 = Math.toRadians(getCentralMeridian(zone));

        double a = IGMConstants.WGS84_A;
        double f = IGMConstants.WGS84_F;
        double eSq = IGMConstants.WGS84_E_SQ;
        double ePrimeSq = IGMConstants.WGS84_E_PRIME_SQ;
        double k0 = IGMConstants.K0;

        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double tanLat = Math.tan(latRad);

        double N = a / Math.sqrt(1.0 - eSq * sinLat * sinLat);
        double T = tanLat * tanLat;
        double C = ePrimeSq * cosLat * cosLat;
        double A = cosLat * (lonRad - lon0);

        // Arco de meridiano M (Fórmula de Krüger/Redfearn de 5to orden)
        double n = f / (2.0 - f);
        double n2 = n * n; double n3 = n2 * n; double n4 = n3 * n;
        double M = (a / (1.0 + n)) * ( (1.0 + n2/4.0 + n4/64.0) * latRad
                - (3.0/2.0*n - 3.0/16.0*n3) * Math.sin(2.0 * latRad)
                + (15.0/16.0*n2 - 15.0/128.0*n4) * Math.sin(4.0 * latRad)
                - (35.0/48.0*n3) * Math.sin(6.0 * latRad)
                + (315.0/512.0*n4) * Math.sin(8.0 * latRad) );

        double easting = k0 * N * (A + (1.0 - T + C) * Math.pow(A, 3) / 6.0
                + (5.0 - 18.0 * T + T * T + 72.0 * C - 58.0 * ePrimeSq) * Math.pow(A, 5) / 120.0) + 500000.0;

        double northing = k0 * (M + N * tanLat * (A * A / 2.0
                + (5.0 - T + 9.0 * C + 4.0 * C * C) * Math.pow(A, 4) / 24.0
                + (61.0 - 58.0 * T + T * T + 600.0 * C - 330.0 * ePrimeSq) * Math.pow(A, 6) / 720.0));

        if (lat < 0) northing += 10000000.0;

        return new UtmPoint(easting, northing, zone, hemisphere);
    }

    /**
     * Inversa: UTM → Geográficas (WGS84) (Redfearn - Precisión Profesional)
     */
    public static GeoPoint inverse(double easting, double northing, int zone, char hemisphere) {
        double a = IGMConstants.WGS84_A;
        double f = IGMConstants.WGS84_F;
        double eSq = IGMConstants.WGS84_E_SQ;
        double ePrimeSq = IGMConstants.WGS84_E_PRIME_SQ;
        double k0 = IGMConstants.K0;

        double x = easting - 500000.0;
        double y = northing;
        if (hemisphere == 'S' || hemisphere == 's') y -= 10000000.0;

        double m = y / k0;
        double n = f / (2.0 - f);
        double n2 = n * n; double n3 = n2 * n; double n4 = n3 * n;
        double A_rect = (a / (1.0 + n)) * (1.0 + n2/4.0 + n4/64.0);
        double mu = m / A_rect;

        double e1 = (1.0 - Math.sqrt(1.0 - eSq)) / (1.0 + Math.sqrt(1.0 - eSq));

        double phi1 = mu + (3.0 * e1 / 2.0 - 27.0 * Math.pow(e1, 3) / 32.0) * Math.sin(2.0 * mu)
                + (21.0 * e1 * e1 / 16.0 - 55.0 * Math.pow(e1, 4) / 32.0) * Math.sin(4.0 * mu)
                + (151.0 * Math.pow(e1, 3) / 96.0) * Math.sin(6.0 * mu)
                + (1097.0 * Math.pow(e1, 4) / 512.0) * Math.sin(8.0 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);

        double n1 = a / Math.sqrt(1.0 - eSq * sinPhi1 * sinPhi1);
        double r1 = a * (1.0 - eSq) / Math.pow(1.0 - eSq * sinPhi1 * sinPhi1, 1.5);
        double d = x / (n1 * k0);

        double t1 = tanPhi1 * tanPhi1;
        double c1 = ePrimeSq * cosPhi1 * cosPhi1;

        double lat = phi1 - (n1 * tanPhi1 / r1) * (d * d / 2.0
                - (5.0 + 3.0 * t1 + 10.0 * c1 - 4.0 * c1 * c1 - 9.0 * ePrimeSq) * Math.pow(d, 4) / 24.0
                + (61.0 + 90.0 * t1 + 298.0 * c1 + 45.0 * t1 * t1 - 252.0 * ePrimeSq - 3.0 * c1 * c1) * Math.pow(d, 6) / 720.0);

        double lon0 = Math.toRadians(getCentralMeridian(zone));
        double lon = lon0 + (d - (1.0 + 2.0 * t1 + c1) * Math.pow(d, 3) / 6.0
                + (5.0 - 2.0 * c1 + 28.0 * t1 - 3.0 * c1 * c1 + 8.0 * ePrimeSq + 24.0 * t1 * t1) * Math.pow(d, 5) / 120.0) / cosPhi1;

        return new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon));
    }
}
