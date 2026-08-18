package bo.com.factorcombinadotopo;

/**
 * Modelo de datos para puntos de replanteo importados.
 */
public class StakeoutPoint {
    private String id;
    private double easting;
    private double northing;
    private int zone;
    private char hemisphere;

    public StakeoutPoint(String id, double easting, double northing, int zone, char hemisphere) {
        this.id = id;
        this.easting = easting;
        this.northing = northing;
        this.zone = zone;
        this.hemisphere = hemisphere;
    }

    public String getId() { return id; }
    public double getEasting() { return easting; }
    public double getNorthing() { return northing; }
    public int getZone() { return zone; }
    public char getHemisphere() { return hemisphere; }

    @Override
    public String toString() {
        return id + " (" + GeoUtils.formatCoord(easting) + " E, " + GeoUtils.formatCoord(northing) + " N)";
    }
}
