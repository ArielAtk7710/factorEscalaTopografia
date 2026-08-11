package bo.com.factorcombinadotopo;

/*
/**
 * Conversión ECEF <-> Geográficas.
 * Fórmulas idénticas a calcular_B01 y calcular_B02 del JS.
 * /
public class IGMEcefConverter {

    /**
     * Geográficas -> ECEF (calcular_B02 del JS).
     * /
    public static IGMCoordinate.EcefPoint toEcef(double lat, double lon, double h,
                                                   IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double e = ellip.eSq;  // El JS usa 'e' como eSq en esta función
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double n = a / Math.sqrt(1.0 - e * Math.pow(Math.sin(latRad), 2));
        double x = (n + h) * Math.cos(latRad) * Math.cos(lonRad);
        double y = (n + h) * Math.cos(latRad) * Math.sin(lonRad);
        double z = (n * (1.0 - e) + h) * Math.sin(latRad);
        return new IGMCoordinate.EcefPoint(x, y, z);
    }

    /**
     * ECEF -> Geográficas (calcular_B01 del JS).
     * Método iterativo de Bowring con 4 iteraciones.
     * /
    public static IGMCoordinate.GeoPoint fromEcef(double x, double y, double z,
                                                   IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double b = ellip.b;
        double e = ellip.eSq;
        double p = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(z * a, p * b);
        double lat = Math.atan2(z + e * a * Math.pow(Math.sin(theta), 3) / Math.sqrt(1.0 - e),
                p - e * a * Math.pow(Math.cos(theta), 3));

        // 4 iteraciones exactas como en el JS
        for (int i = 0; i < 4; i++) {
            double n = a / Math.sqrt(1.0 - e * Math.sin(lat) * Math.sin(lat));
            double h = p / Math.cos(lat) - n;
            lat = Math.atan2(z / p, 1.0 / (1.0 - e * n / (n + h)));
        }

        double n = a / Math.sqrt(1.0 - e * Math.sin(lat) * Math.sin(lat));
        double h = p / Math.cos(lat) - n;
        double lon = Math.atan2(y, x);

        IGMCoordinate.GeoPoint geo = new IGMCoordinate.GeoPoint();
        geo.lat = Math.toDegrees(lat);
        geo.lon = Math.toDegrees(lon);
        geo.h = h;
        return geo;
    }
}
*/
