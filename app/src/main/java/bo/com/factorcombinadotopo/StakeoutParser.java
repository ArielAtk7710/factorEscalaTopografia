package bo.com.factorcombinadotopo;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad profesional para parsear archivos de replanteo (.txt).
 */
public class StakeoutParser {

    // Regex ultra-flexible: Nombre, EsteE, NorteN, UTM, Zona, Hemisferio
    // Soporta variaciones de espacios y separadores regionales (punto/coma decimal)
    private static final String REGEX = "^([^,]+)\\s*,\\s*([\\d.,]+)\\s*E\\s*,\\s*([\\d.,]+)\\s*N\\s*,\\s*UTM\\s*,\\s*(\\d+)\\s*,\\s*([NS])";

    public static List<StakeoutPoint> parseUri(Context context, Uri uri) throws Exception {
        List<StakeoutPoint> points = new ArrayList<>();
        Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String id = matcher.group(1).trim();
                    // Limpiar posibles comas decimales para que Double.parseDouble no falle
                    String eastStr = matcher.group(2).replace(",", ".");
                    String northStr = matcher.group(3).replace(",", ".");
                    
                    double east = Double.parseDouble(eastStr);
                    double north = Double.parseDouble(northStr);
                    int zone = Integer.parseInt(matcher.group(4));
                    char hem = matcher.group(5).toUpperCase().charAt(0);

                    points.add(new StakeoutPoint(id, east, north, zone, hem));
                }
            }
        }
        return points;
    }
}
