package bo.com.factorcombinadotopo;

/*
/**
 * Reducción de distancias al elipsoide.
 * Fórmulas idénticas a calcular_1_03 del JS.
 * /
public class IGMDistanceReducer {

    public static class Result {
        public double d1;      // Reducción al horizonte medio
        public double d2;      // Reducción al elipsoide
        public double d3;      // Paso de cuerda a arco
        public double factor;  // Factor de reducción
    }

    /**
     * Reducción de distancia al elipsoide (calcular_1_03 del JS).
     *
     * @param dab distancia inclinada (m)
     * @param ha  altura punto A (m)
     * @param hb  altura punto B (m)
     * @param latRad latitud en radianes
     * @param ellip elipsoide
     * /
    public static Result reduce(double dab, double ha, double hb, double latRad,
                                 IGMConstants.Ellipsoid ellip) {
        double a = ellip.a;
        double f = ellip.f;
        double e2 = 2.0 * f - f * f;  // Fórmula exacta del JS: e2 = 2*f - f^2

        double r = a / Math.sqrt(1.0 - e2 * Math.pow(Math.sin(latRad), 2));
        double dh = hb - ha;
        double hm = (ha + hb) / 2.0;

        double c = -(Math.pow(dh, 2) / (2.0 * dab)) - (Math.pow(dh, 4) / (8.0 * Math.pow(dab, 3)));
        double d1 = dab + c;
        double d2 = (d1 * r) / (r + hm);
        double d3 = d2 + (Math.pow(d2, 3) / (24.0 * Math.pow(r, 2)));
        double factor = d2 / d1;

        Result res = new Result();
        res.d1 = d1;
        res.d2 = d2;
        res.d3 = d3;
        res.factor = factor;
        return res;
    }
}
*/
