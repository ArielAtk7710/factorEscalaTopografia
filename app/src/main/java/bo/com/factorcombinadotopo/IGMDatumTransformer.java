package bo.com.factorcombinadotopo;

/*
/**
 * Transformación de datum PSAD56 <-> WGS84.
 * Fórmulas idénticas a calcular_1, calcular_2, calcular_3 y calcular_4 del JS.
 * Método de Molodensky-Badekas con 7 parámetros.
 * /
public class IGMDatumTransformer {

    /**
     * PSAD56 -> WGS84 (calcular_1 del JS).
     * /
    public static IGMCoordinate.GeoPoint psad56ToWgs84(double lat, double lon, double h) {
        IGMConstants.Ellipsoid psad = IGMConstants.Ellipsoid.CLARKE_1866;
        IGMConstants.Ellipsoid wgs = IGMConstants.Ellipsoid.WGS84;

        IGMCoordinate.EcefPoint ecef = IGMEcefConverter.toEcef(lat, lon, h, psad);

        double dx = IGMConstants.DATUM_DX;
        double dy = IGMConstants.DATUM_DY;
        double dz = IGMConstants.DATUM_DZ;
        double rx = IGMConstants.DATUM_RX;
        double ry = IGMConstants.DATUM_RY;
        double rz = IGMConstants.DATUM_RZ;
        double d = IGMConstants.DATUM_DS;

        double[][] m = {
                {1.0, rz, -ry},
                {-rz, 1.0, rx},
                {ry, -rx, 1.0}
        };

        double[] xyz0 = {ecef.x, ecef.y, ecef.z};
        double[] resp = {0.0, 0.0, 0.0};

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                resp[i] += m[i][j] * xyz0[j];
            }
        }

        for (int i = 0; i < 3; i++) {
            resp[i] *= (1.0 + d);
        }

        resp[0] += dx;
        resp[1] += dy;
        resp[2] += dz;

        return IGMEcefConverter.fromEcef(resp[0], resp[1], resp[2], wgs);
    }

    /**
     * WGS84 -> PSAD56 (calcular_2 del JS).
     * /
    public static IGMCoordinate.GeoPoint wgs84ToPsad56(double lat, double lon, double h) {
        IGMConstants.Ellipsoid wgs = IGMConstants.Ellipsoid.WGS84;
        IGMConstants.Ellipsoid psad = IGMConstants.Ellipsoid.CLARKE_1866;

        IGMCoordinate.EcefPoint ecef = IGMEcefConverter.toEcef(lat, lon, h, wgs);

        double dx = 269.915;
        double dy = -187.8021;
        double dz = 388.0584;
        double rx = 0.0000000158049260;
        double ry = -0.0000000975929940;
        double rz = 0.0000003118806411;
        double d = 0.00000002810;

        double[][] m = {
                {1.0, -rz, ry},
                {rz, 1.0, -rx},
                {-ry, rx, 1.0}
        };

        double[] xyz0 = {ecef.x, ecef.y, ecef.z};
        double[] resp = {0.0, 0.0, 0.0};

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                resp[i] += m[i][j] * xyz0[j];
            }
        }

        for (int i = 0; i < 3; i++) {
            resp[i] *= (1.0 + d);
        }

        resp[0] += dx;
        resp[1] += dy;
        resp[2] += dz;

        return IGMEcefConverter.fromEcef(resp[0], resp[1], resp[2], psad);
    }

    /**
     * Transformación de coordenadas ECEF (calcular_3 del JS).
     * ITRF/PSAD56 con punto de origen.
     * /
    public static IGMCoordinate.EcefPoint transformEcef(double x, double y, double z,
                                                         double dx, double dy, double dz,
                                                         double rx, double ry, double rz,
                                                         double ds, boolean inverseScale) {
        double x0 = 0.0;
        double y0 = 0.0;
        double z0 = 0.0;

        double[] xyz = {x0 + dx, y0 + dy, z0 + dz};
        double[][] m = {
                {1.0, rz, -ry},
                {-rz, 1.0, rx},
                {ry, -rx, 1.0}
        };
        double[] xyz0 = {x - x0, y - y0, z - z0};
        double[] resp = {0.0, 0.0, 0.0};

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                resp[i] += m[i][j] * xyz0[j];
            }
        }

        double scale = inverseScale ? (1.0 - ds) : (1.0 + ds);
        for (int i = 0; i < 3; i++) {
            resp[i] *= scale;
        }

        for (int i = 0; i < 3; i++) {
            resp[i] += xyz[i];
        }

        return new IGMCoordinate.EcefPoint(resp[0], resp[1], resp[2]);
    }
}
*/
