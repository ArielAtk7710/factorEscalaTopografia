package bo.com.factorcombinadotopo;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Locale;

/**
 * Pruebas Unitarias de Alta Precisión con Datos Reales de Control.
 * Valida que el motor de cálculo coincida con los benchmarks proporcionados.
 */
public class TopoAlgorithmTest {

    private static final double TOL_FACTOR = 1e-8;
    private static final double TOL_PRESION = 0.01;

    @Test
    public void testCasoReal_1() {
        double E = 594886.500, N = 8177859.037, Alt = 3839.611;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(E, N, 19, "S", Alt);
        
        // Verificación de Factores
        assertEquals(0.999711333, res.scaleFactor, TOL_FACTOR);
        assertEquals(0.999396669, res.elevationFactor, TOL_FACTOR);
        assertEquals(0.999108176, res.combinedFactor, TOL_FACTOR);
        
        // Verificación de Presión
        assertEquals(472.078, res.pressureMmHg, TOL_PRESION);
        assertEquals(629.394, res.pressureHpa, TOL_PRESION);
    }

    @Test
    public void testCasoReal_2() {
        double E = 594834.125, N = 8177831.112, Alt = 3827.360;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(E, N, 19, "S", Alt);

        assertEquals(0.999711210, res.scaleFactor, TOL_FACTOR);
        assertEquals(0.999398593, res.elevationFactor, TOL_FACTOR);
        assertEquals(0.999109976, res.combinedFactor, TOL_FACTOR);
        assertEquals(472.830, res.pressureMmHg, TOL_PRESION);
        assertEquals(630.396, res.pressureHpa, TOL_PRESION);
    }

    @Test
    public void testCasoReal_3() {
        double E = 596592.889, N = 8172209.386, Alt = 3351.246;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(E, N, 19, "S", Alt);

        assertEquals(0.999715372, res.scaleFactor, TOL_FACTOR);
        assertEquals(0.999473369, res.elevationFactor, TOL_FACTOR);
        assertEquals(0.999188891, res.combinedFactor, TOL_FACTOR);
        assertEquals(502.782, res.pressureMmHg, TOL_PRESION);
        assertEquals(670.330, res.pressureHpa, TOL_PRESION);
    }

    @Test
    public void testCasoReal_4() {
        double E = 596599.117, N = 8172294.342, Alt = 3355.811;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(E, N, 19, "S", Alt);

        assertEquals(0.999715387, res.scaleFactor, TOL_FACTOR);
        assertEquals(0.999472651, res.elevationFactor, TOL_FACTOR);
        assertEquals(0.999188189, res.combinedFactor, TOL_FACTOR);
        assertEquals(502.488, res.pressureMmHg, TOL_PRESION);
        assertEquals(669.938, res.pressureHpa, TOL_PRESION);
    }

    @Test
    public void testCasoReal_5() {
        double E = 597031.331, N = 8175066.199, Alt = 3511.756;
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateFromUtm(E, N, 19, "S", Alt);

        assertEquals(0.999716422, res.scaleFactor, TOL_FACTOR);
        assertEquals(0.999448158, res.elevationFactor, TOL_FACTOR);
        assertEquals(0.999164737, res.combinedFactor, TOL_FACTOR);
        assertEquals(492.518, res.pressureMmHg, TOL_PRESION);
        assertEquals(656.645, res.pressureHpa, TOL_PRESION);
    }
}
