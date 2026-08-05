package bo.com.factorcombinadotopo;

public class IGMPressureCalculator {

    /** Presión en mmHg (modelo calibrado del JS) */
    public static double calculatePressureMmHg(double altitude) {
        if (altitude < 0) altitude = 0;
        return IGMConstants.P0_MMHG * Math.pow(1.0 - IGMConstants.PRESSURE_COEFF * altitude, IGMConstants.PRESSURE_EXP);
    }

    /** Presión en hPa (modelo calibrado del JS) */
    public static double calculatePressureHpa(double altitude) {
        if (altitude < 0) altitude = 0;
        return IGMConstants.P0_HPA * Math.pow(1.0 - IGMConstants.PRESSURE_COEFF * altitude, IGMConstants.PRESSURE_EXP);
    }

    /** Presión en mmHg (modelo ISO/ICAO) */
    public static double calculatePressureIso(double altitude) {
        if (altitude < 0) altitude = 0;
        return IGMConstants.P0_MMHG_ISO * Math.pow(1.0 - IGMConstants.PRESSURE_COEFF * altitude, IGMConstants.PRESSURE_EXP_ISO);
    }
}
