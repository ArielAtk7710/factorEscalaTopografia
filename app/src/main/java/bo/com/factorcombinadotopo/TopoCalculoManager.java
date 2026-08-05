package bo.com.factorcombinadotopo;

public class TopoCalculoManager {

    public static class TopoResult {
        public double lat;
        public double lon;
        public double scaleFactor;
        public double elevationFactor;
        public double combinedFactor;
        public double pressureMmHg;
        public double pressureHpa;
        public double altOrto;
        public double geoidN;
        public double este;
        public double norte;
        public int zona;
        public char hemisferio;
    }

    /**
     * Cálculo completo desde coordenadas geográficas (WGS84).
     */
    public static TopoResult calculateAll(double lat, double lon, double hEl, double geoidN, double pressureOffset) {
        TopoResult r = new TopoResult();
        r.lat = lat;
        r.lon = lon;
        r.geoidN = geoidN;
        r.altOrto = hEl - geoidN;
        r.zona = GeoUtils.getUtmZone(lon);
        r.hemisferio = GeoUtils.getUtmHemisphere(lat);

        // UTM Directa
        IGMUtmConverter.UtmPoint utm = IGMUtmConverter.forward(lat, lon);
        r.este = utm.easting;
        r.norte = utm.northing;

        // Factores
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0Rad = Math.toRadians(IGMUtmConverter.getCentralMeridian(r.zona));

        r.scaleFactor = IGMScaleCalculator.calculateScaleFactor(latRad, lonRad, lon0Rad, IGMConstants.K0);

        double rm = IGMElevationCalculator.calculateMeanRadius(latRad);
        r.elevationFactor = IGMElevationCalculator.calculateElevationFactor(rm, r.altOrto);
        r.combinedFactor = r.scaleFactor * r.elevationFactor;

        // Presión
        r.pressureMmHg = IGMPressureCalculator.calculatePressureMmHg(r.altOrto) + pressureOffset;
        r.pressureHpa = IGMPressureCalculator.calculatePressureHpa(r.altOrto) + pressureOffset;

        return r;
    }

    public static TopoResult calculateAll(double lat, double lon, double hEl, double geoidN) {
        return calculateAll(lat, lon, hEl, geoidN, 0.0);
    }

    /**
     * Cálculo completo desde coordenadas UTM.
     */
    public static TopoResult calculateFromUtm(double este, double norte, int zona, String hemisferio, double altOrto, double pressureOffset) {
        // 1. Inverso preciso UTM → Geo
        IGMUtmConverter.GeoPoint geo = IGMUtmConverter.inverse(este, norte, zona, hemisferio.charAt(0));

        TopoResult r = new TopoResult();
        r.lat = geo.lat;
        r.lon = geo.lon;
        r.este = este;
        r.norte = norte;
        r.zona = zona;
        r.hemisferio = hemisferio.charAt(0);
        r.altOrto = altOrto;

        // 2. Factor de escala exacto desde geográficas
        double latRad = Math.toRadians(r.lat);
        double lonRad = Math.toRadians(r.lon);
        double lon0Rad = Math.toRadians(IGMUtmConverter.getCentralMeridian(zona));

        r.scaleFactor = IGMScaleCalculator.calculateScaleFactor(latRad, lonRad, lon0Rad, IGMConstants.K0);

        // 3. Factor de elevación con radio medio LOCAL
        double rm = IGMElevationCalculator.calculateMeanRadius(latRad);
        r.elevationFactor = IGMElevationCalculator.calculateElevationFactor(rm, altOrto);
        r.combinedFactor = r.scaleFactor * r.elevationFactor;

        // 4. Presión
        r.pressureMmHg = IGMPressureCalculator.calculatePressureMmHg(altOrto) + pressureOffset;
        r.pressureHpa = IGMPressureCalculator.calculatePressureHpa(altOrto) + pressureOffset;

        return r;
    }

    public static TopoResult calculateFromUtm(double este, double norte, int zona, String hemisferio, double altOrto) {
        return calculateFromUtm(este, norte, zona, hemisferio, altOrto, 0.0);
    }
}
