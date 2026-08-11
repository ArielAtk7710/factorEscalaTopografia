package bo.com.factorcombinadotopo;

/*
/**
 * Proyección Lambert Conforme Cónica (Bolivia).
 * Fórmulas idénticas a calcular_B071 y calcular_B062 del JS.
 * /
public class IGMLambertConverter {

    private final double n, F, r0, lambda0Rad;
    private final double a, e, eSq;

    public IGMLambertConverter(IGMConstants.Ellipsoid ellip) {
        this.a = ellip.a;
        this.eSq = ellip.eSq;
        this.e = Math.sqrt(eSq);

        double phi0 = Math.toRadians(IGMConstants.LAMBERT_BOLIVIA_PHI0);
        double phi1 = Math.toRadians(IGMConstants.LAMBERT_BOLIVIA_PHI1);
        double phi2 = Math.toRadians(IGMConstants.LAMBERT_BOLIVIA_PHI2);
        this.lambda0Rad = Math.toRadians(IGMConstants.LAMBERT_BOLIVIA_LAMBDA0);

        double m1 = Math.cos(phi1) / Math.sqrt(1.0 - eSq * Math.sin(phi1) * Math.sin(phi1));
        double m2 = Math.cos(phi2) / Math.sqrt(1.0 - eSq * Math.sin(phi2) * Math.sin(phi2));

        double t1 = t(phi1);
        double t2 = t(phi2);
        double t0 = t(phi0);

        this.n = Math.log(m1 / m2) / Math.log(t1 / t2);
        this.F = m1 / (this.n * Math.pow(t1, this.n));
        this.r0 = a * this.F * Math.pow(t0, this.n);
    }

    private double t(double phi) {
        return Math.tan(Math.PI / 4.0 - phi / 2.0)
                / Math.pow((1.0 - e * Math.sin(phi)) / (1.0 + e * Math.sin(phi)), e / 2.0);
    }

    /**
     * Forward: Geo -> Lambert (calcular_B071 del JS).
     * /
    public IGMCoordinate.LambertPoint forward(double lat, double lon) {
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double tLat = t(latRad);
        double r = a * F * Math.pow(tLat, n);
        double theta = n * (lonRad - lambda0Rad);

        double x = IGMConstants.LAMBERT_BOLIVIA_FE + r * Math.sin(theta);
        double y = r0 - r * Math.cos(theta);
        return new IGMCoordinate.LambertPoint(x, y);
    }

    /**
     * Inverse: Lambert -> Geo (calcular_B062 del JS).
     * Búsqueda numérica robusta para el hemisferio sur (n < 0).
     * /
    public IGMCoordinate.GeoPoint inverse(double x, double y) {
        double dx = x - IGMConstants.LAMBERT_BOLIVIA_FE;
        double dy = r0 - y;
        double r = Math.sqrt(dx * dx + dy * dy);
        double theta = Math.atan2(dx, dy);

        double lon = Math.toDegrees(lambda0Rad + theta / n);

        // Búsqueda binaria para latitud (método robusto)
        double tGuess = Math.pow(r / (a * Math.abs(F)), 1.0 / Math.abs(n));
        double latLow = -90.0, latHigh = 90.0;
        for (int i = 0; i < 50; i++) {
            double latMid = (latLow + latHigh) / 2.0;
            double tMid = t(Math.toRadians(latMid));
            double rMid = a * F * Math.pow(tMid, n);
            if (rMid < r) {
                latHigh = latMid;
            } else {
                latLow = latMid;
            }
        }
        double lat = (latLow + latHigh) / 2.0;

        IGMCoordinate.GeoPoint geo = new IGMCoordinate.GeoPoint();
        geo.lat = lat;
        geo.lon = lon;
        return geo;
    }
}
*/
