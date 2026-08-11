package bo.com.factorcombinadotopo;

/**
 * Orquestador principal de cálculos topográficos.
 * Mantiene compatibilidad total con ManualFragment y toda la app.
 */
public class TopoCalculoManager {

    public static class TopoResult {
        public double lat;
        public double lon;
        public double scaleFactor;
        public double elevationFactor;
        public double combinedFactor;
        public double pressureMmHg;
        public double pressureHpa;
        public double altEllipsoidal;
        public double altOrto;
        public double geoidN;
        public double este;
        public double norte;
        public int zona;
        public char hemisferio;
    }

    /**
     * Cálculo completo desde coordenadas geográficas (WGS84).
     * Fórmulas idénticas a calcular5_1 del JS.
     */
    public static TopoResult calculateAll(double lat, double lon, double hEl,
                                           double geoidN, double pressureOffset) {
        TopoResult r = new TopoResult();
        r.lat = lat;
        r.lon = lon;
        r.geoidN = geoidN;
        r.altEllipsoidal = hEl;
        r.altOrto = hEl - geoidN;
        r.zona = IGMUtmConverter.getZone(lon);
        r.hemisferio = IGMUtmConverter.getHemisphere(lat);

        IGMCoordinate.UtmPoint utm = IGMUtmConverter.forward(lat, lon, IGMConstants.Ellipsoid.WGS84);
        r.este = utm.easting;
        r.norte = utm.northing;

        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0Rad = Math.toRadians(IGMUtmConverter.getCentralMeridian(r.zona));

        r.scaleFactor = IGMScaleCalculator.calculateScaleFactor(latRad, lonRad, lon0Rad,
                IGMConstants.K0, IGMConstants.Ellipsoid.WGS84);

        double rm = IGMElevationCalculator.calculateMeanRadius(latRad, IGMConstants.Ellipsoid.WGS84);
        r.elevationFactor = IGMElevationCalculator.calculateElevationFactor(rm, r.altOrto);
        r.combinedFactor = r.scaleFactor * r.elevationFactor;
        r.pressureMmHg = IGMPressureCalculator.calculatePressureMmHg(r.altOrto) + pressureOffset;
        double hpaOffset = pressureOffset * (IGMConstants.P0_HPA / IGMConstants.P0_MMHG);
        r.pressureHpa = IGMPressureCalculator.calculatePressureHpa(r.altOrto) + hpaOffset;

        return r;
    }

    public static TopoResult calculateAll(double lat, double lon, double hEl, double geoidN) {
        return calculateAll(lat, lon, hEl, geoidN, 0.0);
    }

    /**
     * Cálculo completo desde coordenadas UTM (WGS84).
     * Fórmulas idénticas a calcular5_2 del JS.
     */
    public static TopoResult calculateFromUtm(double este, double norte, int zona,
                                               String hemisferio, double altOrto,
                                               double pressureOffset) {
        IGMCoordinate.GeoPoint geo = IGMUtmConverter.inverse(este, norte, zona,
                hemisferio.charAt(0), IGMConstants.Ellipsoid.WGS84);

        TopoResult r = new TopoResult();
        r.lat = geo.lat;
        r.lon = geo.lon;
        r.este = este;
        r.norte = norte;
        r.zona = zona;
        r.hemisferio = hemisferio.charAt(0);
        r.altOrto = altOrto;

        double latRad = Math.toRadians(r.lat);
        double lonRad = Math.toRadians(r.lon);
        double lon0Rad = Math.toRadians(IGMUtmConverter.getCentralMeridian(zona));

        r.scaleFactor = IGMScaleCalculator.calculateScaleFactor(latRad, lonRad, lon0Rad,
                IGMConstants.K0, IGMConstants.Ellipsoid.WGS84);

        double rm = IGMElevationCalculator.calculateMeanRadius(latRad, IGMConstants.Ellipsoid.WGS84);
        r.elevationFactor = IGMElevationCalculator.calculateElevationFactor(rm, altOrto);
        r.combinedFactor = r.scaleFactor * r.elevationFactor;
        r.pressureMmHg = IGMPressureCalculator.calculatePressureMmHg(altOrto) + pressureOffset;
        double hpaOffset = pressureOffset * (IGMConstants.P0_HPA / IGMConstants.P0_MMHG);
        r.pressureHpa = IGMPressureCalculator.calculatePressureHpa(altOrto) + hpaOffset;

        return r;
    }

    public static TopoResult calculateFromUtm(double este, double norte, int zona,
                                               String hemisferio, double altOrto) {
        return calculateFromUtm(este, norte, zona, hemisferio, altOrto, 0.0);
    }
}
