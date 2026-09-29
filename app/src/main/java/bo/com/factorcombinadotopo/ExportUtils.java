package bo.com.factorcombinadotopo;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad avanzada para exportación e importación multiformato de puntos topográficos.
 * Soportados: KML (Google Earth/Maps), CSV (QGIS/ArcGIS/Excel), GeoJSON (WebGIS),
 * DXF 3D (AutoCAD/Civil 3D) y GPX (GPS Garmin/Navegadores).
 */
public class ExportUtils {

    public static class ExportPoint {
        public int id;
        public String nombre;
        public String latitud;
        public String longitud;
        public String alturaElipsoidal;
        public String alturaOrtometrica;
        public String este;
        public String norte;
        public String zona;
        public String hemisferio;
        public String factorEscala;
        public String factorAltura;
        public String factorCombinado;
        public String modeloGeoidal;
        public String precision;
        public String satelites;
        public String temperatura;
        public String fecha;
        public String notas;

        public ExportPoint() {}

        public double getNumericLat() {
            return parseCoordinate(latitud);
        }

        public double getNumericLon() {
            return parseCoordinate(longitud);
        }

        public double getNumericAlt() {
            return parseDoubleSafe(alturaOrtometrica != null ? alturaOrtometrica : alturaElipsoidal);
        }

        public double getNumericEast() {
            return parseDoubleSafe(este);
        }

        public double getNumericNorth() {
            return parseDoubleSafe(norte);
        }
    }

    private static double parseCoordinate(String val) {
        if (TextUtils.isEmpty(val)) return 0.0;
        try {
            String clean = val.replace("º", " ").replace("°", " ").replace("'", " ").replace("''", " ").replace("\"", " ").trim();
            String[] parts = clean.split("\\s+");
            if (parts.length == 1) {
                return Double.parseDouble(parts[0].replace(",", "."));
            } else if (parts.length >= 3) {
                double deg = Double.parseDouble(parts[0]);
                double min = Double.parseDouble(parts[1]);
                double sec = Double.parseDouble(parts[2]);
                double dec = deg + (min / 60.0) + (sec / 3600.0);
                if (val.contains("S") || val.contains("W") || val.contains("O")) dec = -dec;
                return dec;
            }
            return Double.parseDouble(clean.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static double parseDoubleSafe(String str) {
        if (TextUtils.isEmpty(str)) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", ".").replaceAll("[^0-9.-]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Genera archivo KML para Google Earth / Google Maps.
     */
    public static String generateKml(List<ExportPoint> puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        sb.append("  <Document>\n");
        sb.append("    <name>Puntos Topográficos - FactorEscala</name>\n");
        sb.append("    <description>Exportación de puntos topográficos georreferenciados</description>\n");

        sb.append("    <Style id=\"topoPointStyle\">\n");
        sb.append("      <IconStyle>\n");
        sb.append("        <scale>1.1</scale>\n");
        sb.append("        <Icon>\n");
        sb.append("          <href>http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png</href>\n");
        sb.append("        </Icon>\n");
        sb.append("      </IconStyle>\n");
        sb.append("      <LabelStyle>\n");
        sb.append("        <color>ff0000ff</color>\n");
        sb.append("        <scale>0.9</scale>\n");
        sb.append("      </LabelStyle>\n");
        sb.append("    </Style>\n");

        for (ExportPoint p : puntos) {
            double lat = p.getNumericLat();
            double lon = p.getNumericLon();
            double alt = p.getNumericAlt();

            sb.append("    <Placemark>\n");
            sb.append("      <name>").append(escapeXml(p.nombre)).append("</name>\n");
            sb.append("      <styleUrl>#topoPointStyle</styleUrl>\n");
            sb.append("      <description><![CDATA[\n");
            sb.append("        <div style=\"font-family: Arial, sans-serif; font-size: 13px; line-height: 1.5;\">\n");
            sb.append("          <h3 style=\"color: #0284C7; margin-bottom: 8px;\">Punto Topográfico: ").append(escapeXml(p.nombre)).append("</h3>\n");
            sb.append("          <table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" style=\"border-collapse: collapse; width: 100%; border-color: #CBD5E1;\">\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Latitud</b></td><td>").append(p.latitud).append("</td></tr>\n");
            sb.append("            <tr><td><b>Longitud</b></td><td>").append(p.longitud).append("</td></tr>\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Este UTM</b></td><td>").append(p.este).append(" m</td></tr>\n");
            sb.append("            <tr><td><b>Norte UTM</b></td><td>").append(p.norte).append(" m</td></tr>\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Zona / Hemisferio</b></td><td>").append(p.zona).append(" ").append(p.hemisferio).append("</td></tr>\n");
            sb.append("            <tr><td><b>Altura Ortométrica (H)</b></td><td>").append(p.alturaOrtometrica).append(" m</td></tr>\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Altura Elipsoidal (h)</b></td><td>").append(p.alturaElipsoidal).append(" m</td></tr>\n");
            sb.append("            <tr><td><b>Factor Combinado (FC)</b></td><td><b>").append(p.factorCombinado).append("</b></td></tr>\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Modelo Geoidal</b></td><td>").append(p.modeloGeoidal).append("</td></tr>\n");
            sb.append("            <tr><td><b>Precisión GPS</b></td><td>").append(p.precision).append("</td></tr>\n");
            sb.append("            <tr bgcolor=\"#F1F5F9\"><td><b>Fecha / Hora</b></td><td>").append(p.fecha).append("</td></tr>\n");
            if (!TextUtils.isEmpty(p.notas)) {
                sb.append("            <tr><td><b>Notas</b></td><td>").append(escapeXml(p.notas)).append("</td></tr>\n");
            }
            sb.append("          </table>\n");
            sb.append("          <p style=\"font-size: 11px; color: #64748B; margin-top: 8px;\">Generado por <b>FactorEscala Topografía</b></p>\n");
            sb.append("        </div>\n");
            sb.append("      ]]></description>\n");
            sb.append("      <Point>\n");
            sb.append("        <coordinates>").append(String.format(Locale.US, "%.8f,%.8f,%.3f", lon, lat, alt)).append("</coordinates>\n");
            sb.append("      </Point>\n");
            sb.append("    </Placemark>\n");
        }

        sb.append("  </Document>\n");
        sb.append("</kml>");
        return sb.toString();
    }

    /**
     * Genera archivo CSV para QGIS / ArcGIS / Excel.
     */
    public static String generateCsv(List<ExportPoint> puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,Latitud,Longitud,Este_UTM,Norte_UTM,Zona,Hemisferio,Altura_Elipsoidal,Altura_Ortometrica,Factor_Escala,Factor_Altura,Factor_Combinado,Modelo_Geoidal,Precision,Satelites,Temperatura,Fecha,Notas\n");

        for (ExportPoint p : puntos) {
            sb.append(p.id).append(",")
              .append(csvEscape(p.nombre)).append(",")
              .append(csvEscape(p.latitud)).append(",")
              .append(csvEscape(p.longitud)).append(",")
              .append(csvEscape(p.este)).append(",")
              .append(csvEscape(p.norte)).append(",")
              .append(csvEscape(p.zona)).append(",")
              .append(csvEscape(p.hemisferio)).append(",")
              .append(csvEscape(p.alturaElipsoidal)).append(",")
              .append(csvEscape(p.alturaOrtometrica)).append(",")
              .append(csvEscape(p.factorEscala)).append(",")
              .append(csvEscape(p.factorAltura)).append(",")
              .append(csvEscape(p.factorCombinado)).append(",")
              .append(csvEscape(p.modeloGeoidal)).append(",")
              .append(csvEscape(p.precision)).append(",")
              .append(csvEscape(p.satelites)).append(",")
              .append(csvEscape(p.temperatura)).append(",")
              .append(csvEscape(p.fecha)).append(",")
              .append(csvEscape(p.notas)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Genera archivo GeoJSON para WebGIS / QGIS.
     */
    public static String generateGeoJson(List<ExportPoint> puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"FeatureCollection\",\n");
        sb.append("  \"name\": \"FactorEscala_Puntos\",\n");
        sb.append("  \"features\": [\n");

        for (int i = 0; i < puntos.size(); i++) {
            ExportPoint p = puntos.get(i);
            double lat = p.getNumericLat();
            double lon = p.getNumericLon();
            double alt = p.getNumericAlt();

            sb.append("    {\n");
            sb.append("      \"type\": \"Feature\",\n");
            sb.append("      \"geometry\": {\n");
            sb.append("        \"type\": \"Point\",\n");
            sb.append("        \"coordinates\": [").append(String.format(Locale.US, "%.8f, %.8f, %.3f", lon, lat, alt)).append("]\n");
            sb.append("      },\n");
            sb.append("      \"properties\": {\n");
            sb.append("        \"id\": ").append(p.id).append(",\n");
            sb.append("        \"nombre\": \"").append(jsonEscape(p.nombre)).append("\",\n");
            sb.append("        \"este\": \"").append(jsonEscape(p.este)).append("\",\n");
            sb.append("        \"norte\": \"").append(jsonEscape(p.norte)).append("\",\n");
            sb.append("        \"zona\": \"").append(jsonEscape(p.zona)).append("\",\n");
            sb.append("        \"hemisferio\": \"").append(jsonEscape(p.hemisferio)).append("\",\n");
            sb.append("        \"altura_ortometrica\": \"").append(jsonEscape(p.alturaOrtometrica)).append("\",\n");
            sb.append("        \"factor_combinado\": \"").append(jsonEscape(p.factorCombinado)).append("\",\n");
            sb.append("        \"modelo_geoidal\": \"").append(jsonEscape(p.modeloGeoidal)).append("\",\n");
            sb.append("        \"fecha\": \"").append(jsonEscape(p.fecha)).append("\"\n");
            sb.append("      }\n");
            sb.append("    }").append(i < puntos.size() - 1 ? "," : "").append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Genera archivo DXF 3D para AutoCAD / Civil 3D / Global Mapper.
     */
    public static String generateDxf(List<ExportPoint> puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("0\nSECTION\n2\nHEADER\n0\nENDSEC\n");
        sb.append("0\nSECTION\n2\nTABLES\n0\nENDSEC\n");
        sb.append("0\nSECTION\n2\nBLOCKS\n0\nENDSEC\n");
        sb.append("0\nSECTION\n2\nENTITIES\n");

        for (ExportPoint p : puntos) {
            double x = p.getNumericEast();
            double y = p.getNumericNorth();
            double z = p.getNumericAlt();

            // 1. Entidad POINT (Punto 3D en AutoCAD)
            sb.append("0\nPOINT\n8\nPUNTOS_TOPO\n");
            sb.append("10\n").append(String.format(Locale.US, "%.3f", x)).append("\n");
            sb.append("20\n").append(String.format(Locale.US, "%.3f", y)).append("\n");
            sb.append("30\n").append(String.format(Locale.US, "%.3f", z)).append("\n");

            // 2. Entidad TEXT para Nombre de Punto
            sb.append("0\nTEXT\n8\nNOMBRES_PUNTO\n");
            sb.append("10\n").append(String.format(Locale.US, "%.3f", x + 0.4)).append("\n");
            sb.append("20\n").append(String.format(Locale.US, "%.3f", y + 0.4)).append("\n");
            sb.append("30\n").append(String.format(Locale.US, "%.3f", z)).append("\n");
            sb.append("40\n1.2\n");
            sb.append("1\n").append(p.nombre != null ? p.nombre : "PUNTO").append("\n");

            // 3. Entidad TEXT para Cota
            sb.append("0\nTEXT\n8\nCOTAS_PUNTO\n");
            sb.append("10\n").append(String.format(Locale.US, "%.3f", x + 0.4)).append("\n");
            sb.append("20\n").append(String.format(Locale.US, "%.3f", y - 1.0)).append("\n");
            sb.append("30\n").append(String.format(Locale.US, "%.3f", z)).append("\n");
            sb.append("40\n0.9\n");
            sb.append("1\nZ=").append(String.format(Locale.US, "%.3f", z)).append("\n");
        }

        sb.append("0\nENDSEC\n0\nEOF\n");
        return sb.toString();
    }

    /**
     * Genera archivo GPX para GPS Garmin y navegadores.
     */
    public static String generateGpx(List<ExportPoint> puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"FactorEscalaTop\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");

        for (ExportPoint p : puntos) {
            double lat = p.getNumericLat();
            double lon = p.getNumericLon();
            double alt = p.getNumericAlt();

            sb.append("  <wpt lat=\"").append(String.format(Locale.US, "%.8f", lat)).append("\" lon=\"").append(String.format(Locale.US, "%.8f", lon)).append("\">\n");
            sb.append("    <ele>").append(String.format(Locale.US, "%.3f", alt)).append("</ele>\n");
            sb.append("    <name>").append(escapeXml(p.nombre)).append("</name>\n");
            sb.append("    <cmt>Este: ").append(p.este).append(", Norte: ").append(p.norte).append(", FC: ").append(p.factorCombinado).append("</cmt>\n");
            sb.append("    <sym>WayPoint</sym>\n");
            sb.append("  </wpt>\n");
        }

        sb.append("</gpx>");
        return sb.toString();
    }

    /**
     * Parsea contenido KML, GPX o CSV para importar puntos al sistema de replanteo.
     */
    public static List<StakeoutPoint> parseImportFile(String content, String fileName) {
        List<StakeoutPoint> result = new ArrayList<>();
        if (TextUtils.isEmpty(content)) return result;

        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) {
            parseCsvLines(content, result);
        } else if (lowerName.endsWith(".kml")) {
            parseKmlContent(content, result);
        } else if (lowerName.endsWith(".gpx")) {
            parseGpxContent(content, result);
        } else {
            if (content.contains("<kml")) parseKmlContent(content, result);
            else if (content.contains("<gpx")) parseGpxContent(content, result);
            else parseCsvLines(content, result);
        }
        return result;
    }

    private static void parseCsvLines(String content, List<StakeoutPoint> result) {
        String[] lines = content.split("\r?\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().startsWith("id,") || trimmed.toLowerCase().startsWith("nombre,")) {
                continue;
            }
            String[] parts = trimmed.split("[,;\\t]");
            if (parts.length >= 3) {
                try {
                    String name = parts[0].trim().replaceAll("^\"|\"$", "");
                    double val1 = parseDoubleSafe(parts[1]);
                    double val2 = parseDoubleSafe(parts[2]);
                    double easting, northing;
                    int zone = 19;
                    char hemisphere = 'S';

                    if (Math.abs(val1) <= 90.0 && Math.abs(val2) <= 180.0) {
                        IGMCoordinate.UtmPoint utm = IGMUtmConverter.forward(val1, val2, IGMConstants.Ellipsoid.WGS84);
                        easting = utm.easting;
                        northing = utm.northing;
                        zone = utm.zone;
                        hemisphere = utm.hemisphere;
                    } else {
                        easting = val1;
                        northing = val2;
                    }

                    StakeoutPoint pt = new StakeoutPoint(name, easting, northing, zone, hemisphere);
                    result.add(pt);
                } catch (Exception ignored) {}
            }
        }
    }

    private static void parseKmlContent(String content, List<StakeoutPoint> result) {
        Pattern pattern = Pattern.compile("<Placemark>(.*?)</Placemark>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String pm = matcher.group(1);
            if (pm == null) continue;
            String name = extractTagValue(pm, "name");
            String coordsStr = extractTagValue(pm, "coordinates");

            if (!TextUtils.isEmpty(coordsStr)) {
                String[] coords = coordsStr.trim().split(",");
                if (coords.length >= 2) {
                    try {
                        double lon = Double.parseDouble(coords[0].trim());
                        double lat = Double.parseDouble(coords[1].trim());

                        IGMCoordinate.UtmPoint utm = IGMUtmConverter.forward(lat, lon, IGMConstants.Ellipsoid.WGS84);
                        String pointName = !TextUtils.isEmpty(name) ? name : "PUNTO_KML";
                        StakeoutPoint pt = new StakeoutPoint(pointName, utm.easting, utm.northing, utm.zone, utm.hemisphere);
                        result.add(pt);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private static void parseGpxContent(String content, List<StakeoutPoint> result) {
        Pattern pattern = Pattern.compile("<wpt\\s+lat=\"([^\"]+)\"\\s+lon=\"([^\"]+)\">(.*?)</wpt>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String latStr = matcher.group(1);
            String lonStr = matcher.group(2);
            String inner = matcher.group(3);

            if (latStr != null && lonStr != null && inner != null) {
                try {
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    String name = extractTagValue(inner, "name");

                    IGMCoordinate.UtmPoint utm = IGMUtmConverter.forward(lat, lon, IGMConstants.Ellipsoid.WGS84);
                    String pointName = !TextUtils.isEmpty(name) ? name : "PUNTO_GPX";
                    StakeoutPoint pt = new StakeoutPoint(pointName, utm.easting, utm.northing, utm.zone, utm.hemisphere);
                    result.add(pt);
                } catch (Exception ignored) {}
            }
        }
    }

    private static String extractTagValue(String xml, String tagName) {
        if (xml == null) return "";
        Pattern p = Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        if (m.find()) {
            String match = m.group(1);
            return match != null ? match.trim().replaceAll("<!\\[CDATA\\[|\\]\\]>", "") : "";
        }
        return "";
    }

    private static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String csvEscape(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\"", "\"\"") + "\"";
    }

    private static String jsonEscape(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
