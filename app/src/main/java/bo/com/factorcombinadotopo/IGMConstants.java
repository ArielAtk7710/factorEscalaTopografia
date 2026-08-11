package bo.com.factorcombinadotopo;

/**
 * Constantes geodésicas, físicas y de proyección.
 * Valores idénticos a los utilizados en los scripts JavaScript.
 */
public final class IGMConstants {

    private IGMConstants() {}

    /** Elipsoides soportados (valores exactos del JS) */
    public enum Ellipsoid {
        WGS84(6378137.0, 1.0 / 298.257223563),
        GRS80(6378137.0, 1.0 / 298.257222101),
        CLARKE_1866(6378388.0, 1.0 / 297.0);

        public final double a;      // Semieje mayor (m)
        public final double f;      // Aplanamiento
        public final double b;      // Semieje menor (m)
        public final double eSq;    // Primera excentricidad al cuadrado
        public final double ePrimeSq; // Segunda excentricidad al cuadrado

        Ellipsoid(double a, double f) {
            this.a = a;
            this.f = f;
            this.b = a * (1.0 - f);
            this.eSq = (a * a - this.b * this.b) / (a * a);
            this.ePrimeSq = (a * a - this.b * this.b) / (this.b * this.b);
        }
    }

    // ─── UTM ───
    public static final double K0 = 0.9996;
    public static final double FALSE_EASTING = 500000.0;
    public static final double FALSE_NORTHING_S = 10000000.0;

    // ─── Presión Atmosférica (modelo exacto del JS) ───
    public static final double P0_MMHG = 759.99;
    public static final double P0_HPA = 1013.25;
    public static final double PRESSURE_COEFF = 0.0000225577;
    public static final double PRESSURE_EXP = 5.2559;

    // ─── Lambert Bolivia (parámetros exactos del JS) ───
    public static final double LAMBERT_BOLIVIA_FE = 1000000.0;
    public static final double LAMBERT_BOLIVIA_FN = 0.0;
    public static final double LAMBERT_BOLIVIA_LAMBDA0 = -64.0;
    public static final double LAMBERT_BOLIVIA_PHI0 = -24.0;
    public static final double LAMBERT_BOLIVIA_PHI1 = -11.5;
    public static final double LAMBERT_BOLIVIA_PHI2 = -21.5;

    // ─── Datum PSAD56 -> WGS84 (parámetros exactos del JS calcular_1) ───
    public static final double DATUM_DX = -269.915;
    public static final double DATUM_DY = 187.8021;
    public static final double DATUM_DZ = -388.0584;
    public static final double DATUM_RX = 0.0000000158049260;
    public static final double DATUM_RY = -0.0000000975929940;
    public static final double DATUM_RZ = 0.0000003118806411;
    public static final double DATUM_DS = -0.00000002810;

    // ─── Constante e del JS (aproximación de Euler usada en Coticchia-Surace) ───
    public static final double EULER_JS = 2.718281828;
}
