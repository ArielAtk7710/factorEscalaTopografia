package bo.com.factorcombinadotopo;

/**
 * Conversión UTM <-> Geográficas (WGS84).
 * Fórmulas idénticas a calcular_B05, calcular_B06 y calcular5_2 del JS.
 * Usa la serie de Redfearn (USGS Bulletin 1532) — equivalente matemático al JS.
 */
public class IGMUtmConverter {

    public static double getCentralMeridian(int zone) {
        return (zone * 6.0) - 183.0;
    }

    public static int getZone(double lon) {
        return (int) Math.floor((lon + 180.0) / 6.0) + 1;
    }

    public static char getHemisphere(double lat) {
        return lat >= 0 ? 'N' : 'S';
    }

    /**
     * Forward: Geo -> UTM (calcular_B05 del JS).
     * Serie de Redfearn con coeficientes exactos.
     */
    public static IGMCoordinate.UtmPoint forward(double lat, double lon,
                                                  IGMConstants.Ellipsoid ellip) {
        int zone = getZone(lon);
        char hemisphere = getHemisphere(lat);
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0 = Math.toRadians(getCentralMeridian(zone));

        double a = ellip.a;
        double e = ellip.eSq;
        double e2 = ellip.ePrimeSq;
        double ko = IGMConstants.K0;

        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double tanLat = Math.tan(latRad);

        double n = a / Math.sqrt(1.0 - e * sinLat * sinLat);
        double t = tanLat * tanLat;
        double c = e2 * cosLat * cosLat;
        double aa = cosLat * (lonRad - lon0);

        double m = a * ((1.0 - e / 4.0 - 3.0 * e * e / 64.0 - 5.0 * Math.pow(e, 3) / 256.0) * latRad
                - (3.0 * e / 8.0 + 3.0 * e * e / 32.0 + 45.0 * Math.pow(e, 3) / 1024.0) * Math.sin(2.0 * latRad)
                + (15.0 * e * e / 256.0 + 45.0 * Math.pow(e, 3) / 1024.0) * Math.sin(4.0 * latRad)
                - (35.0 * Math.pow(e, 3) / 3072.0) * Math.sin(6.0 * latRad));

        double easting = ko * n * (aa + (1.0 - t + c) * Math.pow(aa, 3) / 6.0
                + (5.0 - 18.0 * t + t * t + 14.0 * c - 58.0 * t * c) * Math.pow(aa, 5) / 120.0)
                + IGMConstants.FALSE_EASTING;

        double northing = ko * (m + n * tanLat * (aa * aa / 2.0
                + (5.0 - t + 9.0 * c + 4.0 * c * c) * Math.pow(aa, 4) / 24.0
                + (61.0 - 58.0 * t + t * t + 270.0 * c - 330.0 * t * c) * Math.pow(aa, 6) / 720.0));

        if (lat < 0) northing += IGMConstants.FALSE_NORTHING_S;

        return new IGMCoordinate.UtmPoint(easting, northing, zone, hemisphere);
    }

    /**
     * Inverse: UTM -> Geo (calcular_B06 del JS).
     * Serie de Redfearn inversa (USGS).
     */
    public static IGMCoordinate.GeoPoint inverse(double easting, double northing,
                                                  int zone, char hemisphere,
                                                  IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double e = ellip.eSq;
        double e2 = ellip.ePrimeSq;
        double ko = IGMConstants.K0;

        double x = easting - IGMConstants.FALSE_EASTING;
        double y = northing;
        if (hemisphere == 'S' || hemisphere == 's') y -= IGMConstants.FALSE_NORTHING_S;

        double m = y / ko;
        double mu = m / (a * (1.0 - e / 4.0 - 3.0 * e * e / 64.0 - 5.0 * Math.pow(e, 3) / 256.0));

        double e1 = (1.0 - Math.sqrt(1.0 - e)) / (1.0 + Math.sqrt(1.0 - e));

        double phi1 = mu + (3.0 * e1 / 2.0 - 27.0 * Math.pow(e1, 3) / 32.0) * Math.sin(2.0 * mu)
                + (21.0 * e1 * e1 / 16.0 - 55.0 * Math.pow(e1, 4) / 32.0) * Math.sin(4.0 * mu)
                + (151.0 * Math.pow(e1, 3) / 96.0) * Math.sin(6.0 * mu)
                + (1097.0 * Math.pow(e1, 4) / 512.0) * Math.sin(8.0 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);

        double n1 = a / Math.sqrt(1.0 - e * sinPhi1 * sinPhi1);
        double r1 = a * (1.0 - e) / Math.pow(1.0 - e * sinPhi1 * sinPhi1, 1.5);
        double d = x / (n1 * ko);

        double t1 = tanPhi1 * tanPhi1;
        double c1 = e2 * cosPhi1 * cosPhi1;

        double lat = phi1 - (n1 * tanPhi1 / r1) * (d * d / 2.0
                - (5.0 + 3.0 * t1 + 10.0 * c1 - 4.0 * c1 * c1 - 9.0 * e2) * Math.pow(d, 4) / 24.0
                + (61.0 + 90.0 * t1 + 298.0 * c1 + 45.0 * t1 * t1 - 252.0 * e2 - 3.0 * c1 * c1) * Math.pow(d, 6) / 720.0);

        double lon0 = Math.toRadians(getCentralMeridian(zone));
        double lon = lon0 + (d - (1.0 + 2.0 * t1 + c1) * Math.pow(d, 3) / 6.0
                + (5.0 - 2.0 * c1 + 28.0 * t1 - 3.0 * c1 * c1 + 8.0 * e2 + 24.0 * t1 * t1) * Math.pow(d, 5) / 120.0) / cosPhi1;

        IGMCoordinate.GeoPoint geo = new IGMCoordinate.GeoPoint();
        geo.lat = Math.toDegrees(lat);
        geo.lon = Math.toDegrees(lon);
        return geo;
    }
}
