package bo.com.factorcombinadotopo;

import java.util.Locale;

/**
 * Formateo de coordenadas: Decimal <-> Sexagesimal.
 * Fórmulas idénticas a calcular_A1 y calcular_A1_002 del JS.
 */
public class IGMCoordinateFormatter {

    /**
     * Decimal -> Sexagesimal (idéntico a calcular_A1 del JS).
     * Usa Math.trunc equivalente: (int) value trunca hacia cero en Java.
     */
    public static IGMCoordinate.DmsCoordinate toDms(double decimal, boolean isLatitude) {
        IGMCoordinate.DmsCoordinate dms = new IGMCoordinate.DmsCoordinate();
        double absVal = Math.abs(decimal);
        dms.degrees = (int) absVal;  // Equivalente a Math.trunc en JS
        double n = (absVal - dms.degrees) * 60.0;
        dms.minutes = (int) n;       // Equivalente a Math.trunc
        dms.seconds = Math.abs((n - dms.minutes) * 60.0);
        dms.hemisphere = isLatitude
                ? (decimal >= 0 ? 'N' : 'S')
                : (decimal >= 0 ? 'E' : 'W');
        return dms;
    }

    /**
     * Sexagesimal -> Decimal (idéntico a calcular_A1_002 del JS).
     */
    public static double toDecimal(int degrees, int minutes, double seconds, char hemisphere) {
        double decimal = degrees + minutes / 60.0 + seconds / 3600.0;
        return (hemisphere == 'S' || hemisphere == 'W') ? -decimal : decimal;
    }

    public static String formatDms(IGMCoordinate.DmsCoordinate dms) {
        return String.format(Locale.US, "%d°%d'%.5f\"%c",
                dms.degrees, dms.minutes, dms.seconds, dms.hemisphere);
    }
}
