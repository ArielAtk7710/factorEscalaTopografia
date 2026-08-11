package bo.com.factorcombinadotopo;

/**
 * Clases de datos para coordenadas geodésicas y cartográficas.
 */
public class IGMCoordinate {

    public static class GeoPoint {
        public double lat;
        public double lon;
        public double h;
        public GeoPoint() {}
        public GeoPoint(double lat, double lon) { this(lat, lon, 0.0); }
        public GeoPoint(double lat, double lon, double h) {
            this.lat = lat; this.lon = lon; this.h = h;
        }
    }

    public static class UtmPoint {
        public double easting;
        public double northing;
        public int zone;
        public char hemisphere;
        public UtmPoint() {}
        public UtmPoint(double easting, double northing, int zone, char hemisphere) {
            this.easting = easting; this.northing = northing;
            this.zone = zone; this.hemisphere = hemisphere;
        }
    }

    public static class EcefPoint {
        public double x, y, z;
        public EcefPoint() {}
        public EcefPoint(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    public static class EnuPoint {
        public double dE, dN, dU;
        public EnuPoint() {}
        public EnuPoint(double dE, double dN, double dU) {
            this.dE = dE; this.dN = dN; this.dU = dU;
        }
    }

    public static class LambertPoint {
        public double x, y;
        public LambertPoint() {}
        public LambertPoint(double x, double y) { this.x = x; this.y = y; }
    }

    public static class DmsCoordinate {
        public int degrees;
        public int minutes;
        public double seconds;
        public char hemisphere;
        public DmsCoordinate() {}
        public DmsCoordinate(int degrees, int minutes, double seconds, char hemisphere) {
            this.degrees = degrees; this.minutes = minutes;
            this.seconds = seconds; this.hemisphere = hemisphere;
        }
    }

    public static class TopoResult {
        public double lat, lon, scaleFactor, elevationFactor, combinedFactor;
        public double pressureMmHg, pressureHpa, altOrto, geoidN;
        public double este, norte;
        public int zona;
        public char hemisferio;
    }
}
