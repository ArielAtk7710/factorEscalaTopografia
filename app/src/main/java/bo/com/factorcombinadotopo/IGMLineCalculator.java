package bo.com.factorcombinadotopo;

/**
 * Línea entre 2 puntos UTM.
 * Fórmulas idénticas a calcular6_4 del JS.
 */
public class IGMLineCalculator {

    public static class LineResult {
        public double distance;
        public double azimuth;
        public double convergence1;
        public double convergence2;
    }

    /**
     * Calcula distancia plana, acimut y convergencia entre 2 puntos UTM.
     */
    public static LineResult calculate(double east1, double north1, double east2, double north2,
                                        int zone, String hemisphere) {
        IGMCoordinate.GeoPoint geo1 = IGMUtmConverter.inverse(east1, north1, zone,
                hemisphere.charAt(0), IGMConstants.Ellipsoid.WGS84);
        IGMCoordinate.GeoPoint geo2 = IGMUtmConverter.inverse(east2, north2, zone,
                hemisphere.charAt(0), IGMConstants.Ellipsoid.WGS84);

        double dx = east2 - east1;
        double dy = north2 - north1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double az = Math.toDegrees(Math.atan2(dx, dy));
        if (az < 0) az += 360.0;

        double lon0 = Math.toRadians(IGMUtmConverter.getCentralMeridian(zone));
        double conv1 = Math.toDegrees(Math.sin(Math.toRadians(geo1.lat)) * (Math.toRadians(geo1.lon) - lon0));
        double conv2 = Math.toDegrees(Math.sin(Math.toRadians(geo2.lat)) * (Math.toRadians(geo2.lon) - lon0));

        LineResult r = new LineResult();
        r.distance = dist;
        r.azimuth = az;
        r.convergence1 = conv1;
        r.convergence2 = conv2;
        return r;
    }
}
