package bo.com.factorcombinadotopo;

/*
/**
 * Conversión ECEF <-> ENU (Sistema Topocéntrico Local).
 * Fórmulas idénticas a calcular_B03 y calcular_B04 del JS.
 * /
public class IGMEnuConverter {

    /**
     * ECEF -> ENU (calcular_B03 del JS).
     * /
    public static IGMCoordinate.EnuPoint toEnu(double x, double y, double z,
                                                double x0, double y0, double z0,
                                                double lat0, double lon0) {
        double latRad = Math.toRadians(lat0);
        double lonRad = Math.toRadians(lon0);
        double dx = x - x0;
        double dy = y - y0;
        double dz = z - z0;

        double dE = -Math.sin(lonRad) * dx + Math.cos(lonRad) * dy;
        double dN = -Math.sin(latRad) * Math.cos(lonRad) * dx
                    - Math.sin(latRad) * Math.sin(lonRad) * dy
                    + Math.cos(latRad) * dz;
        double dU = Math.cos(latRad) * Math.cos(lonRad) * dx
                    + Math.cos(latRad) * Math.sin(lonRad) * dy
                    + Math.sin(latRad) * dz;

        return new IGMCoordinate.EnuPoint(dE, dN, dU);
    }

    /**
     * ENU -> ECEF (calcular_B04 del JS).
     * /
    public static IGMCoordinate.EcefPoint fromEnu(IGMCoordinate.EnuPoint enu,
                                                   double x0, double y0, double z0,
                                                   double lat0, double lon0) {
        double latRad = Math.toRadians(lat0);
        double lonRad = Math.toRadians(lon0);
        double dE = enu.dE, dN = enu.dN, dU = enu.dU;

        double dx = -Math.sin(lonRad) * dE
                    - Math.sin(latRad) * Math.cos(lonRad) * dN
                    + Math.cos(latRad) * Math.cos(lonRad) * dU;
        double dy = Math.cos(lonRad) * dE
                    - Math.sin(latRad) * Math.sin(lonRad) * dN
                    + Math.cos(latRad) * Math.sin(lonRad) * dU;
        double dz = Math.cos(latRad) * dN + Math.sin(latRad) * dU;

        return new IGMCoordinate.EcefPoint(x0 + dx, y0 + dy, z0 + dz);
    }
}
*/
