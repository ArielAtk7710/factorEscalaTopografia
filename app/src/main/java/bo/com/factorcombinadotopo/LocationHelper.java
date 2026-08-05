package bo.com.factorcombinadotopo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Clase auxiliar para gestionar la ubicación mediante FusedLocationProviderClient.
 * Sigue el principio de responsabilidad única (Single Responsibility Principle).
 */
public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private LocationUpdateListener listener;

    public interface LocationUpdateListener {
        void onLocationUpdated(Location location);
    }

    public LocationHelper(Context context, LocationUpdateListener listener) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.listener = listener;

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null && LocationHelper.this.listener != null) {
                        LocationHelper.this.listener.onLocationUpdated(location);
                    }
                }
            }
        };
    }

    /**
     * Inicia la solicitud de actualizaciones de ubicación.
     */
    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Detiene las actualizaciones de ubicación para ahorrar batería.
     */
    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Obtiene la última ubicación conocida de forma rápida.
     */
    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && listener != null) {
                listener.onLocationUpdated(location);
            }
        });
    }
}
