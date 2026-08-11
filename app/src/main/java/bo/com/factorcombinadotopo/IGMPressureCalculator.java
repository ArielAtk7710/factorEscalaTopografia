package bo.com.factorcombinadotopo;

/**
 * Presión atmosférica.
 * Fórmulas idénticas a calcular5_1 y calcular5_2 del JS.
 */
public class IGMPressureCalculator {

    /**
     * Presión en mmHg (modelo exacto del JS).
     */
    public static double calculatePressureMmHg(double altitude) {
        if (altitude < 0) altitude = 0;
        return 759.99 * Math.pow(1.0 - 0.0000225577 * altitude, 5.2559);
    }

    /**
     * Presión en hPa (modelo exacto del JS).
     */
    public static double calculatePressureHpa(double altitude) {
        if (altitude < 0) altitude = 0;
        return 1013.25 * Math.pow(1.0 - 0.0000225577 * altitude, 5.2559);
    }
}
