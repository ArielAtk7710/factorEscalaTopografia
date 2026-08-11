package bo.com.factorcombinadotopo;

/*
/**
 * Transversal Mercator general configurable.
 * Fórmulas idénticas a calcular6_1 del JS.
 * /
public class IGMTmConverter {

    /**
     * TM Directa configurable (calcular6_1 del JS).
     *
     * @param lat latitud (grados)
     * @param lon longitud (grados)
     * @param centralMeridian meridiano central (grados)
     * @param k0 factor de escala
     * @param falseEasting falso este
     * @param falseNorthing falso norte
     * @param ellip elipsoide
     * /
    public static IGMCoordinate.UtmPoint forward(double lat, double lon,
                                                  double centralMeridian, double k0,
                                                  double falseEasting, double falseNorthing,
                                                  IGMConstants.Ellipsoid ellip) {
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0 = Math.toRadians(centralMeridian);

        double a = ellip.a;
        double e = ellip.eSq;
        double e2 = ellip.ePrimeSq;

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

        double easting = k0 * n * (aa + (1.0 - t + c) * Math.pow(aa, 3) / 6.0
                + (5.0 - 18.0 * t + t * t + 14.0 * c - 58.0 * t * c) * Math.pow(aa, 5) / 120.0)
                + falseEasting;

        double northing = k0 * (m + n * tanLat * (aa * aa / 2.0
                + (5.0 - t + 9.0 * c + 4.0 * c * c) * Math.pow(aa, 4) / 24.0
                + (61.0 - 58.0 * t + t * t + 270.0 * c - 330.0 * t * c) * Math.pow(aa, 6) / 720.0))
                + falseNorthing;

        char hemisphere = lat >= 0 ? 'N' : 'S';
        return new IGMCoordinate.UtmPoint(easting, northing, 0, hemisphere);
    }
}
*/
