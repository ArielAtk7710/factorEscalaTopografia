package bo.com.factorcombinadotopo;

public class TopoCalculoManager {

    // Constantes Geodésicas Oficiales WGS84 (NGA / ISO)
    private static final double A = 6378137.0;             // Semieje mayor (m)
    private static final double F = 1.0 / 298.257223563;    // Aplanamiento
    private static final double B = A * (1.0 - F);          // Semieje menor (m)
    private static final double E_SQ = (A * A - B * B) / (A * A); // Primera excentricidad^2
    private static final double E_PRIME_SQ = (A * A - B * B) / (B * B); // Segunda excentricidad^2
    private static final double K0 = 0.9996;                // Factor de escala meridiano central

    public static class TopoResult {
        public double lat;
        public double lon;
        public double scaleFactor;
        public double elevationFactor;
        public double combinedFactor;
        public double pressureMmHg;
        public double altOrto;
        public double geoidN;
        public double este;
        public double norte;
        public int zona;
        public char hemisferio;
    }

    /**
     * Cálculo geodésico completo a partir de Geográficas + Altura Elipsoidal
     */
    public static TopoResult calculateAll(double lat, double lon, double h_el, double geoidN, double pressureOffset) {
        TopoResult result = new TopoResult();
        result.lat = lat;
        result.lon = lon;
        result.geoidN = geoidN;
        result.altOrto = h_el - geoidN; // h_orto = h_elip - N
        result.zona = (int) Math.floor((lon + 180.0) / 6.0) + 1;
        result.hemisferio = (lat >= 0) ? 'N' : 'S';

        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double lon0 = Math.toRadians(((result.zona - 1) * 6 - 180 + 3));

        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double tanLat = Math.tan(latRad);

        // Radios de curvatura en la latitud local
        double N_rad = A / Math.sqrt(1.0 - E_SQ * sinLat * sinLat);
        double M_rad = A * (1.0 - E_SQ) / Math.pow(1.0 - E_SQ * sinLat * sinLat, 1.5);
        double R_m = Math.sqrt(M_rad * N_rad); // Radio medio Gaussiano

        double eta2 = E_PRIME_SQ * cosLat * cosLat;
        double a = cosLat * (lonRad - lon0);

        // Arco meridional S(phi) exacto
        double m = A * ((1.0 - E_SQ / 4.0 - 3.0 * E_SQ * E_SQ / 64.0 - 5.0 * Math.pow(E_SQ, 3) / 256.0) * latRad
                - (3.0 * E_SQ / 8.0 + 3.0 * E_SQ * E_SQ / 32.0 + 45.0 * Math.pow(E_SQ, 3) / 1024.0) * Math.sin(2.0 * latRad)
                + (15.0 * E_SQ * E_SQ / 256.0 + 45.0 * Math.pow(E_SQ, 3) / 1024.0) * Math.sin(4.0 * latRad)
                - (35.0 * Math.pow(E_SQ, 3) / 3072.0) * Math.sin(6.0 * latRad));

        // Conversión UTM
        double t2 = tanLat * tanLat;
        result.este = K0 * N_rad * (a + (1.0 - t2 + eta2) * Math.pow(a, 3) / 6.0
                + (5.0 - 18.0 * t2 + t2 * t2 + 14.0 * eta2 - 58.0 * t2 * eta2) * Math.pow(a, 5) / 120.0) + 500000.0;

        double y = K0 * (m + N_rad * tanLat * (a * a / 2.0
                + (5.0 - t2 + 9.0 * eta2 + 4.0 * eta2 * eta2) * Math.pow(a, 4) / 24.0
                + (61.0 - 58.0 * t2 + t2 * t2 + 270.0 * eta2 - 330.0 * t2 * eta2) * Math.pow(a, 6) / 720.0));

        if (lat < 0) {
            y += 10000000.0; // Falso Norte Hemisferio Sur
        }
        result.norte = y;

        // Factor de Escala Puntual (k)
        result.scaleFactor = K0 * (1.0 + (1.0 + eta2) * Math.pow(a, 2) / 2.0
                + (5.0 - 4.0 * t2 + 42.0 * eta2 + 13.0 * eta2 * eta2) * Math.pow(a, 4) / 24.0);

        // Factor de Elevación (Kh) usando el radio medio Gaussiano local
        result.elevationFactor = R_m / (R_m + result.altOrto);

        // Factor Combinado (FC = k * Kh)
        result.combinedFactor = result.scaleFactor * result.elevationFactor;

        // Presión Barométrica Estándar ICAO/ISO 2533 + Offset
        result.pressureMmHg = calculatePressureIso(result.altOrto) + pressureOffset;

        return result;
    }

    /**
     * Backward compatibility
     */
    public static TopoResult calculateAll(double lat, double lon, double h_el, double geoidN) {
        return calculateAll(lat, lon, h_el, geoidN, 0.0);
    }

    /**
     * Cálculo rápido para libretas ingresadas por UTM + Cota
     */
    public static TopoResult calculateFromUtm(double este, double norte, int zona, String hemisferio, double altOrto, double pressureOffset) {
        // Primero convertimos a geográficas para tener latitud (necesaria para factores precisos si se quisiera)
        // Pero para el cálculo rápido usamos el radio medio Rm.
        TopoResult inv = calculateInverseUtm(este, norte, zona, hemisferio.charAt(0));
        
        TopoResult result = new TopoResult();
        result.lat = inv.lat;
        result.lon = inv.lon;
        result.este = este;
        result.norte = norte;
        result.zona = zona;
        result.hemisferio = hemisferio.charAt(0);
        result.altOrto = altOrto;

        double x = este - 500000.0;
        double R_m = 6371007.181; // Radio medio elipsoidal de la Tierra

        result.scaleFactor = K0 * (1.0 + Math.pow(x / R_m, 2) / 2.0 + Math.pow(x / R_m, 4) / 24.0);
        result.elevationFactor = R_m / (R_m + altOrto);
        result.combinedFactor = result.scaleFactor * result.elevationFactor;
        result.pressureMmHg = calculatePressureIso(altOrto) + pressureOffset;

        return result;
    }

    /**
     * Convierte coordenadas UTM a Geodésicas (WGS84)
     */
    public static TopoResult calculateInverseUtm(double este, double norte, int zona, char hemisferio) {
        TopoResult res = new TopoResult();
        res.este = este;
        res.norte = norte;
        res.zona = zona;
        res.hemisferio = hemisferio;

        double x = este - 500000.0;
        double y = norte;
        if (hemisferio == 'S' || hemisferio == 's') {
            y -= 10000000.0;
        }

        double m = y / K0;
        double mu = m / (A * (1 - E_SQ/4 - 3*E_SQ*E_SQ/64 - 5*Math.pow(E_SQ,3)/256));
        
        double e1 = (1 - Math.sqrt(1 - E_SQ)) / (1 + Math.sqrt(1 - E_SQ));
        
        double phi1 = mu + (3*e1/2 - 27*Math.pow(e1,3)/32) * Math.sin(2*mu)
                + (21*e1*e1/16 - 55*Math.pow(e1,4)/32) * Math.sin(4*mu)
                + (151*Math.pow(e1,3)/96) * Math.sin(6*mu)
                + (1097*Math.pow(e1,4)/512) * Math.sin(8*mu);
        
        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);
        
        double n1 = A / Math.sqrt(1 - E_SQ * sinPhi1 * sinPhi1);
        double r1 = A * (1 - E_SQ) / Math.pow(1 - E_SQ * sinPhi1 * sinPhi1, 1.5);
        double d = x / (n1 * K0);
        
        double t1 = tanPhi1 * tanPhi1;
        double c1 = E_PRIME_SQ * cosPhi1 * cosPhi1;
        
        double lat = phi1 - (n1 * tanPhi1 / r1) * (d*d/2 - (5 + 3*t1 + 10*c1 - 4*c1*c1 - 9*E_PRIME_SQ)*Math.pow(d,4)/24
                + (61 + 90*t1 + 298*c1 + 45*t1*t1 - 252*E_PRIME_SQ - 3*c1*c1)*Math.pow(d,6)/720);
        
        double lon0 = Math.toRadians((zona - 1) * 6 - 180 + 3);
        double lon = lon0 + (d - (1 + 2*t1 + c1)*Math.pow(d,3)/6 + (5 - 2*c1 + 28*t1 - 3*c1*c1 + 8*E_PRIME_SQ + 24*t1*t1)*Math.pow(d,5)/120) / cosPhi1;
        
        res.lat = Math.toDegrees(lat);
        res.lon = Math.toDegrees(lon);
        
        return res;
    }

    /**
     * Backward compatibility for calculateFromUtm
     */
    public static TopoResult calculateFromUtm(double este, double norte, int zona, String hemisferio, double altOrto) {
        return calculateFromUtm(este, norte, zona, hemisferio, altOrto, 0.0);
    }

    /**
     * Presión Atmosférica Estándar ICAO / ISO 2533 (mmHg)
     */
    public static double calculatePressureIso(double hOrt) {
        if (hOrt < 0) hOrt = 0;
        return 760.0 * Math.pow(1.0 - 2.25577e-5 * hOrt, 5.25588);
    }
}