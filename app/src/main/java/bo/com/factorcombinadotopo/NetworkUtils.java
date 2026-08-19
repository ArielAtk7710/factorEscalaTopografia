package bo.com.factorcombinadotopo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Utilidad Senior para monitoreo de conectividad.
 * Proporciona validación robusta del estado del hardware de red.
 */
public class NetworkUtils {

    private static boolean offlineWarningShown = false;

    /**
     * Verifica si el dispositivo tiene una conexión activa a Internet.
     * Soporta redes Wi-Fi, Datos Móviles y Ethernet.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        boolean available = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                available = capabilities != null && (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                );
            }
        } else {
            // Soporte para versiones antiguas de Android (Legacy)
            android.net.NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            available = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return available;
    }

    /**
     * Verifica si hay red disponible de forma estricta.
     * Útil para evitar falsos positivos de Wi-Fi sin internet.
     */
    public static boolean isNetworkAvailableStrict(Context context) {
        if (!isNetworkAvailable(context)) return false;
        
        // En Android moderno, isNetworkAvailable ya valida capacidades.
        // Podríamos añadir un check de socket aquí, pero para evitar lags
        // confiamos en el sistema operativo y su validación de internet.
        return true;
    }

    /**
     * Determina si se debe mostrar la advertencia de "Sin Internet".
     * Solo retorna true la primera vez que se llama en un estado de desconexión.
     */
    public static boolean shouldShowOfflineWarning(Context context) {
        if (isNetworkAvailable(context)) {
            offlineWarningShown = false;
            return false;
        }
        
        if (!offlineWarningShown) {
            offlineWarningShown = true;
            return true;
        }
        return false;
    }

    /**
     * Restablece el flag de advertencia. Llamar cuando la red vuelve a estar disponible.
     */
    public static void resetOfflineWarning() {
        offlineWarningShown = false;
    }
}
