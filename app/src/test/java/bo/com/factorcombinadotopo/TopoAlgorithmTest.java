package bo.com.factorcombinadotopo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class TopoAlgorithmTest {

    // Tolerancias para comprobación estricta de libreta de campo
    private static final double TOL_COORD = 0.5;     // ±0.5 metros en coordenadas UTM
    private static final double TOL_FACTOR = 1e-5;    // Precisión hasta 5 decimales en factores
    private static final double TOL_PRESION = 1.0;   // ±1 mmHg en presión barométrica

    // ==========================================
    // EJEMPLOS DE LA TABLA DE LIBRETA DE CAMPO
    // ==========================================

    /**
     * Punto 1:
     * Norte: 8170324.030 | Este: 600312.642 | H_ort: 3438.750 | Zona: 19
     * Lat: -16.5469869 | Lon: -68.059860
     */
    @Test
    public void testTabla_Punto1() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(-16.5469869, -68.059860, 3438.750, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170324.030, resGeo.norte, TOL_COORD);
        assertEquals(600312.642, resGeo.este, TOL_COORD);
        assertEquals(0.9997244, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994608, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991854, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);

        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(600312.642, 8170324.030, 19, "S", 3438.750);
        assertNotNull(resUtm);
        assertEquals(0.9997244, resUtm.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994608, resUtm.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991854, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 2:
     * Norte: 8170392.786 | Este: 600385.081 | H_ort: 3442.274 | Zona: 19
     * Lat: -16.5463623 | Lon: -68.0591842
     */
    @Test
    public void testTabla_Punto2() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(-16.5463623, -68.0591842, 3442.274, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170392.786, resGeo.norte, TOL_COORD);
        assertEquals(600385.081, resGeo.este, TOL_COORD);
        assertEquals(0.9997246, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994603, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991850, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 3:
     * Norte: 8170339.348 | Este: 600430.994 | H_ort: 3443.297 | Zona: 19
     * Lat: -16.5468434 | Lon: -68.0587516
     */
    @Test
    public void testTabla_Punto3() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(-16.5468434, -68.0587516, 3443.297, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170339.348, resGeo.norte, TOL_COORD);
        assertEquals(600430.994, resGeo.este, TOL_COORD);
        assertEquals(0.9997247, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994601, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991850, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 4:
     * Norte: 8170281.759 | Este: 600384.203 | H_ort: 3443.367 | Zona: 19
     * Lat: -16.5473659 | Lon: -68.0591876
     */
    @Test
    public void testTabla_Punto4() {
        TopoCalculoManager.TopoResult resGeo = TopoCalculoManager.calculateAll(-16.5473659, -68.0591876, 3443.367, 0.0);
        assertNotNull(resGeo);
        assertEquals(8170281.759, resGeo.norte, TOL_COORD);
        assertEquals(600384.203, resGeo.este, TOL_COORD);
        assertEquals(0.9997246, resGeo.scaleFactor, TOL_FACTOR);
        assertEquals(0.9994601, resGeo.elevationFactor, TOL_FACTOR);
        assertEquals(0.9991848, resGeo.combinedFactor, TOL_FACTOR);
        assertEquals(497.0, resGeo.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 5:
     * Este: 597031.331 | Norte: 8175066.199 | Elevación: 3511.756
     * FactorEscala: 0.99971700 | FactorCombinado: 0.99916600 | Presion: 492.2
     */
    @Test
    public void testTabla_Punto5() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                597031.331,
                8175066.199,
                19,
                "S",
                3511.756
        );

        assertNotNull(resUtm);
        assertEquals(0.99971700, resUtm.scaleFactor, TOL_FACTOR);
        assertEquals(0.99916600, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(492.2, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 6 (GPS1 Grupo A):
     * Este: 594886.500 | Norte: 8177859.037 | Elevación: 3839.611
     * FactorCombinado: 0.9991095180 | Presion: 472 mmHg
     */
    @Test
    public void testTabla_Punto6_GPS1() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                594886.500,
                8177859.037,
                19,
                "S",
                3839.611
        );

        assertNotNull(resUtm);
        assertEquals(0.9991095180, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(472.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 7 (GPS2 Grupo A):
     * Este: 594834.125 | Norte: 8177831.112 | Elevación: 3827.360
     * FactorCombinado: 0.9991113160 | Presion: 473 mmHg
     */
    @Test
    public void testTabla_Punto7_GPS2() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                594834.125,
                8177831.112,
                19,
                "S",
                3827.360
        );

        assertNotNull(resUtm);
        assertEquals(0.9991113160, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(473.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 8 (GPS1 Grupo B):
     * Este: 596592.889 | Norte: 8172209.386 | Elevación: 3351.246
     * FactorCombinado: 0.999188891 | Presion: 502 mmHg
     */
    @Test
    public void testTabla_Punto8_GPS1_B() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                596592.889,
                8172209.386,
                19,
                "S",
                3351.246
        );

        assertNotNull(resUtm);
        assertEquals(0.999188891, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(502.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 9 (GPS2 Grupo B):
     * Este: 596599.117 | Norte: 8172294.342 | Elevación: 3355.811
     * FactorCombinado: 0.999188189 | Presion: 502 mmHg
     */
    @Test
    public void testTabla_Punto9_GPS2_B() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                596599.117,
                8172294.342,
                19,
                "S",
                3355.811
        );

        assertNotNull(resUtm);
        assertEquals(0.999188189, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(502.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 10 (ERI-827):
     * Este: 595706.669 | Norte: 8171803.416 | Elevación: 3356.491
     * FactorCombinado: 0.99918800 | Presion: 500 mmHg
     */
    @Test
    public void testTabla_Punto10_ERI827() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595706.669,
                8171803.416,
                19,
                "S",
                3356.491
        );

        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.0, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 11 (ERJ-932):
     * Este: 595730.017 | Norte: 8171843.222 | Elevación: 3356.039
     * FactorCombinado: 0.99918800 | Presion: 500.1 mmHg
     */
    @Test
    public void testTabla_Punto11_ERJ932() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595730.017,
                8171843.222,
                19,
                "S",
                3356.039
        );

        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.1, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 12 (ERT−390):
     * Este: 595710.290 | Norte: 8171782.203 | Elevación: 3355.319
     * FactorCombinado: 0.99918800 | Presion: 500.1 mmHg
     */
    @Test
    public void testTabla_Punto12_ERT390() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595710.290,
                8171782.203,
                19,
                "S",
                3355.319
        );

        assertNotNull(resUtm);
        assertEquals(0.99918800, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(500.1, resUtm.pressureMmHg, TOL_PRESION);
    }

    /**
     * Punto 13 (ERT−391):
     * Este: 595626.208 | Norte: 8171904.612 | Elevación: 3362.621
     * FactorCombinado: 0.99918700 | Presion: 499.6 mmHg
     */
    @Test
    public void testTabla_Punto13_ERT391() {
        TopoCalculoManager.TopoResult resUtm = TopoCalculoManager.calculateFromUtm(
                595626.208,
                8171904.612,
                19,
                "S",
                3362.621
        );

        assertNotNull(resUtm);
        assertEquals(0.99918700, resUtm.combinedFactor, TOL_FACTOR);
        assertEquals(499.6, resUtm.pressureMmHg, TOL_PRESION);
    }

    @Test
    public void testInverseUtm_Punto1() {
        // Lat: -16.5469869 | Lon: -68.059860 -> UTM: 600312.642, 8170324.030
        TopoCalculoManager.TopoResult resInv = TopoCalculoManager.calculateInverseUtm(600312.642, 8170324.030, 19, 'S');
        assertNotNull(resInv);
        // Tolerancia de 0.00001 grados (~1 metro) es aceptable para conversión inversa simplificada
        assertEquals(-16.5469869, resInv.lat, 1e-5);
        assertEquals(-68.059860, resInv.lon, 1e-5);
    }
}
