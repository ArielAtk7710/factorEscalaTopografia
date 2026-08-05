package bo.com.factorcombinadotopo;

public class TopoAlgorithmTest {
    /**
     * Validación numérica con datos de campo:
     *   Este: 597031.331, Norte: 8175066.199, Zona: 19S, Altura: 3511.756 m
     * Resultados esperados:
     *   k = 0.999716422 | Kh = 0.999448158 | Kc = 0.999164737
     *   P = 492.518 mmHg | 656.645 hPa
     */

        private static final double TOL_9 = 1e-9;
        private static final double TOL_3 = 1e-3;
        private static int pass = 0, fail = 0;

        public static void main(String[] args) {
            System.out.println("=== IGM Validation Tests ===\n");

            testUtmInverse();
            testScaleFactor();
            testElevationFactor();
            testPressure();
            testFullPipelineUtm();

            System.out.println("\n============================");
            System.out.println("  PASS: " + pass + "  |  FAIL: " + fail);
            System.out.println("============================");
        }

        private static void testUtmInverse() {
            IGMUtmConverter.GeoPoint g = IGMUtmConverter.inverse(597031.331, 8175066.199, 19, 'S');
            assertEq("Inv-Lat", g.lat, -16.504259669, 1e-6);
            assertEq("Inv-Lon", g.lon, -68.090810545, 1e-6);
        }

        private static void testScaleFactor() {
            double lat = -16.50425966947146;
            double lon = -68.09081054467711;
            double k = IGMScaleCalculator.calculateScaleFactor(
                    Math.toRadians(lat), Math.toRadians(lon),
                    Math.toRadians(IGMUtmConverter.getCentralMeridian(19)), IGMConstants.K0);
            assertEq("ScaleFactor", k, 0.999716422, TOL_9);
        }

        private static void testElevationFactor() {
            double rm = IGMElevationCalculator.calculateMeanRadius(Math.toRadians(-16.50425966947146));
            double kh = IGMElevationCalculator.calculateElevationFactor(rm, 3511.756);
            assertEq("ElevFactor", kh, 0.999448158, TOL_9);
        }

        private static void testPressure() {
            assertEq("Pres-mmHg", IGMPressureCalculator.calculatePressureMmHg(3511.756), 492.518, TOL_3);
            assertEq("Pres-hPa",  IGMPressureCalculator.calculatePressureHpa(3511.756),  656.645, TOL_3);
        }

        private static void testFullPipelineUtm() {
            TopoCalculoManager.TopoResult r = TopoCalculoManager.calculateFromUtm(
                    597031.331, 8175066.199, 19, "S", 3511.756);

            assertEq("Pipe-Scale", r.scaleFactor, 0.999716422, TOL_9);
            assertEq("Pipe-Elev",  r.elevationFactor, 0.999448158, TOL_9);
            assertEq("Pipe-Comb",  r.combinedFactor, 0.999164737, TOL_9);
            assertEq("Pipe-mmHg",  r.pressureMmHg, 492.518, TOL_3);
            assertEq("Pipe-hPa",   r.pressureHpa, 656.645, TOL_3);
        }

        private static void assertEq(String name, double actual, double expected, double tol) {
            if (Math.abs(actual - expected) <= tol) {
                System.out.printf("[PASS] %-15s | exp: %.9f | got: %.9f%n", name, expected, actual);
                pass++;
            } else {
                System.out.printf("[FAIL] %-15s | exp: %.9f | got: %.9f | diff: %.2e%n",
                        name, expected, actual, Math.abs(actual - expected));
                fail++;
            }
        }
    }