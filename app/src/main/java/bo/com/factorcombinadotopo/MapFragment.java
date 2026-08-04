package bo.com.factorcombinadotopo;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MapFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        Button btnOpenMaps = view.findViewById(R.id.btn_open_google_maps);
        btnOpenMaps.setOnClickListener(v -> openCurrentLocationInMaps());
    }

    private void openCurrentLocationInMaps() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_location_permission_required));
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                String uri = String.format(java.util.Locale.US, "geo:%f,%f?q=%f,%f(Mi Ubicación)", lat, lon, lat, lon);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // Si no tiene Google Maps, abrir en cualquier visor de geo:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            } else {
                UIUtils.showWarningToast(requireContext(), getString(R.string.msg_location_error));
            }
        });
    }
}
