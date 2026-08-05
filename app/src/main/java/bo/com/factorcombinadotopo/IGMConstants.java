package bo.com.factorcombinadotopo;

public final class IGMConstants {

    private IGMConstants() {}

    // ─── WGS84 (NGA / ISO) ───
    public static final double WGS84_A = 6378137.0;
    public static final double WGS84_F = 1.0 / 298.257223563;
    public static final double WGS84_B = WGS84_A * (1.0 - WGS84_F);
    public static final double WGS84_E_SQ = (WGS84_A * WGS84_A - WGS84_B * WGS84_B) / (WGS84_A * WGS84_A);
    public static final double WGS84_E_PRIME_SQ = (WGS84_A * WGS84_A - WGS84_B * WGS84_B) / (WGS84_B * WGS84_B);
    public static final double K0 = 0.9996;

    // ─── Presión Atmosférica (modelo calibrado del JS) ───
    public static final double P0_MMHG = 759.99;
    public static final double P0_HPA = 1013.25;
    public static final double PRESSURE_COEFF = 0.0000225577;
    public static final double PRESSURE_EXP = 5.2559;

    // ─── Presión Atmosférica (modelo ISO/ICAO alternativo) ───
    public static final double P0_MMHG_ISO = 760.0;
    public static final double PRESSURE_EXP_ISO = 5.25588;

    // ─── Radio Medio Terrestre ───
    public static final double R_MEAN = 6371007.181;
}