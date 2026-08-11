package bo.com.factorcombinadotopo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests de regresión basados en libreta de campo real.
 * Validan que la refactorización modular mantenga los mismos resultados
 * dentro de tolerancias profesionales.
 */
public class TopoAlgorithmTest {

    private static final double TOL_COORD = 0.5;
    private static final double TOL_FACTOR = 1e-5;
    private static final double TOL_PRESION = 1.0;
    private static final double TOL_PRESION_TABLA = 3.0;

    @Test
    public void testTabla_Punto1() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(
                -16.5469869, -68.059860, 3438.750, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170324.030, resGeo.norte, TOL_COORD);
        assertEquals(600312.642, resGeo.este, TOL_COORD);
        assertEquals(0.9997244, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994608, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991854, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);

        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                600312.642, 8170324.030, 19, "S", 3438.750);
        assertNotNull(resUtm);
        assertEquals(resGeo.scaleFactor, resUtm.scaleFactor, 1e-9);
        assertEquals(resGeo.elevationFactor, resUtm.elevationFactor, 1e-9);
        assertEquals(resGeo.combinedFactor, resUtm.combinedFactor, 1e-9);
    }

    @Test
    public void testTabla_Punto2() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(
                -16.5463623, -68.0591842, 3442.274, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170392.786, resGeo.norte, TOL_COORD);
        assertEquals(600385.081, resGeo.este, TOL_COORD);
        assertEquals(0.9997246, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994603, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991850, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto3() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(
                -16.5468434, -68.0587516, 3443.297, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170339.348, resGeo.norte, TOL_COORD);
        assertEquals(600430.994, resGeo.este, TOL_COORD);
        assertEquals(0.9997247, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994601, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991850, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto4() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(
                -16.5473659, -68.0591876, 3443.367, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170281.759, resGeo.norte, TOL_COORD);
        assertEquals(600384.203, resGeo.este, TOL_COORD);
        assertEquals(0.9997246, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994601, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991848, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto5() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                597031.331, 8175066.199, 19, "S", 3511.756);
        assertNotNull(resUtm);
        assertEquals(0.99971700, resUtm.scaleFactor, TOL_FACTOR);
        assertEquals(0.99916600, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(492.2, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto6_GPS1() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                594886.500, 8177859.037, 19, "S", 3839.611);
        assertNotNull(resUtm);
        assertEquals(0.9991095180, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(472.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto7_GPS2() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                594834.125, 8177831.112, 19, "S", 3827.360);
        assertNotNull(resUtm);
        assertEquals(0.9991113160, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(473.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto8_GPS1_B() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                596592.889, 8172209.386, 19, "S", 3351.246);
        assertNotNull(resUtm);
        assertEquals(0.999188891, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(502.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto9_GPS2_B() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                596599.117, 8172294.342, 19, "S", 3355.811);
        assertNotNull(resUtm);
        assertEquals(0.999188189, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(502.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testTabla_Punto10_ERI827() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595706.669, 8171803.416, 19, "S", 3356.491);
        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.0, resUtm.pressureMmHg, TOL_PRESION_TABLA);
    }

    @Test
    public void testTabla_Punto11_ERJ932() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595730.017, 8171843.222, 19, "S", 3356.039);
        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.1, resUtm.pressureMmHg, TOL_PRESION_TABLA);
    }

    @Test
    public void testTabla_Punto12_ERT390() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595710.290, 8171782.203, 19, "S", 3355.319);
        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.1, resUtm.pressureMmHg, TOL_PRESION_TABLA);
    }

    @Test
    public void testTabla_Punto13_ERT391() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595626.208, 8171904.612, 19, "S", 3362.621);
        assertNotNull(resUtm);
        assertEquals(0.99918700, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(499.6, resUtm.pressureMmHg, TOL_PRESION_TABLA);
    }

    @Test
    public void testInverseUtm_Punto1() {
        IGMCoordinate.GeoPoint geo = IGMUtmConverter.inverse(
                600312.642, 8170324.030, 19, 'S', IGMConstants.Ellipsoid.WGS84);
        assertNotNull(geo);
        assertEquals(-16.5469869, geo.lat, 1e-5);
        assertEquals(-68.059860, geo.lon, 1e-5);
    }

    @Test
    public void testInverseUtm_Punto5() {
        IGMCoordinate.GeoPoint geo = IGMUtmConverter.inverse(
                597031.331, 8175066.199, 19, 'S', IGMConstants.Ellipsoid.WGS84);
        assertNotNull(geo);
        assertEquals(-16.504259669, geo.lat, 1e-6);
        assertEquals(-68.090810545, geo.lon, 1e-6);
    }

    @Test
    public void testRoundTrip_Consistency() {
        double lat = -16.5469869, lon = -68.059860;
        IGMCoordinate.UtmPoint utm = IGMUtmConverter.forward(lat, lon, IGMConstants.Ellipsoid.WGS84);
        IGMCoordinate.GeoPoint geo = IGMUtmConverter.inverse(utm.easting, utm.northing, utm.zone,
                utm.hemisphere, IGMConstants.Ellipsoid.WGS84);
        assertEquals(lat, geo.lat, 1e-7);
        assertEquals(lon, geo.lon, 1e-7);
    }

    @Test
    public void testPressureWithOffset() {
        double alt = 3511.756, offset = -0.3;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(
                597031.331, 8175066.199, 19, "S", alt, offset);
        double expected = IGMPressureCalculator.calculatePressureMmHg(alt) + offset;
        assertEquals(expected, res.pressureMmHg, 1e-3);
    }

    @Test
    public void testEcefRoundTrip() {
        double lat = -16.504259669, lon = -68.090810545, h = 3511.756;
        IGMCoordinate.EcefPoint ecef = IGMEcefConverter.toEcef(lat, lon, h, IGMConstants.Ellipsoid.WGS84);
        IGMCoordinate.GeoPoint geo = IGMEcefConverter.fromEcef(ecef.x, ecef.y, ecef.z,
                IGMConstants.Ellipsoid.WGS84);
        assertEquals(lat, geo.lat, 1e-9);
        assertEquals(lon, geo.lon, 1e-9);
        assertEquals(h, geo.h, 1e-6);
    }

    @Test
    public void testEnuRoundTrip() {
        double lat0 = -16.504259669, lon0 = -68.090810545, h0 = 3511.756;
        IGMCoordinate.EcefPoint ecef0 = IGMEcefConverter.toEcef(lat0, lon0, h0, IGMConstants.Ellipsoid.WGS84);
        IGMCoordinate.EnuPoint enu = new IGMCoordinate.EnuPoint(100, 200, 50);
        IGMCoordinate.EcefPoint ecef1 = IGMEnuConverter.fromEnu(enu, ecef0.x, ecef0.y, ecef0.z, lat0, lon0);
        IGMCoordinate.EnuPoint enuBack = IGMEnuConverter.toEnu(ecef1.x, ecef1.y, ecef1.z,
                ecef0.x, ecef0.y, ecef0.z, lat0, lon0);
        assertEquals(enu.dE, enuBack.dE, 1e-6);
        assertEquals(enu.dN, enuBack.dN, 1e-6);
        assertEquals(enu.dU, enuBack.dU, 1e-6);
    }

    @Test
    public void testCoordinateFormatter() {
        IGMCoordinate.DmsCoordinate dms = IGMCoordinateFormatter.toDms(-16.504259669, true);
        assertEquals(16, dms.degrees);
        assertEquals(30, dms.minutes);
        assertEquals('S', dms.hemisphere);
        double back = IGMCoordinateFormatter.toDecimal(dms.degrees, dms.minutes, dms.seconds, dms.hemisphere);
        assertEquals(-16.504259669, back, 1e-6);
    }

    @Test
    public void testDistanceReducer() {
        double lat = -16.504259669;
        IGMDistanceReducer.Result r = IGMDistanceReducer.reduce(1000.0, 3400, 3600,
                Math.toRadians(lat), IGMConstants.Ellipsoid.WGS84);
        assertTrue(r.d2 < r.d1);
        assertTrue(r.factor < 1.0);
        assertTrue(r.factor > 0.99);
    }

    @Test
    public void testSurveyDirectInverse() {
        double[] p1 = IGMSurveyCalculator.direct(1000, 2000, 100, 45);
        IGMSurveyCalculator.InverseResult inv = IGMSurveyCalculator.inverse(1000, 2000, p1[0], p1[1]);
        assertEquals(100.0, inv.distance, 1e-6);
        assertEquals(45.0, inv.azimuth, 1e-6);
    }

    @Test
    public void testGeodesicBowring() {
        IGMGeodesicCalculator.GeoResult r = IGMGeodesicCalculator.bowringDirect(
                -16.504259669, -68.090810545, 45, 1000, IGMConstants.Ellipsoid.WGS84);
        assertNotNull(r);
        assertTrue(r.lat > -16.6 && r.lat < -16.4);
        assertTrue(r.lon > -68.2 && r.lon < -68.0);
    }
}
