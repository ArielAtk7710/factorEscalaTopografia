package bo.com.factorcombinadotopo;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class WeatherFragment extends Fragment {

    private CardView cardStatus;
    private ImageView imgStatus;
    private TextView txtTitle, txtForecast, txtTemp;
    private TextView labelWind120, valWind120, labelGusts, valGusts, labelKp, valKp, labelRain, valRain, labelRainProb, valRainProb;
    
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        cardStatus = view.findViewById(R.id.card_safety_status);
        imgStatus = view.findViewById(R.id.img_status_icon);
        txtTitle = view.findViewById(R.id.txt_safety_title);
        txtForecast = view.findViewById(R.id.txt_weather_forecast);
        txtTemp = view.findViewById(R.id.txt_weather_temp);

        View rowWind = view.findViewById(R.id.detail_wind_120);
        labelWind120 = rowWind.findViewById(R.id.txt_detail_label);
        valWind120 = rowWind.findViewById(R.id.txt_detail_value);
        labelWind120.setText(R.string.label_wind_120_v);

        View rowGusts = view.findViewById(R.id.detail_gusts);
        labelGusts = rowGusts.findViewById(R.id.txt_detail_label);
        valGusts = rowGusts.findViewById(R.id.txt_detail_value);
        labelGusts.setText(R.string.label_gusts_max);

        View rowRainProb = view.findViewById(R.id.detail_rain_prob);
        labelRainProb = rowRainProb.findViewById(R.id.txt_detail_label);
        valRainProb = rowRainProb.findViewById(R.id.txt_detail_value);
        labelRainProb.setText(R.string.label_rain_prob_v);

        View rowRain = view.findViewById(R.id.detail_rain);
        labelRain = rowRain.findViewById(R.id.txt_detail_label);
        valRain = rowRain.findViewById(R.id.txt_detail_value);
        labelRain.setText(R.string.label_rain_actual);

        View rowKp = view.findViewById(R.id.detail_kp);
        labelKp = rowKp.findViewById(R.id.txt_detail_label);
        valKp = rowKp.findViewById(R.id.txt_detail_value);
        labelKp.setText(R.string.label_kp_solar);

        view.findViewById(R.id.btn_refresh_weather).setOnClickListener(v -> loadWeatherData());
        ((android.widget.Button)view.findViewById(R.id.btn_refresh_weather)).setText(R.string.btn_refresh_data);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        loadWeatherData();
    }

    private void loadWeatherData() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    WeatherManager.checkFlightSafety(location.getLatitude(), location.getLongitude(), new WeatherManager.WeatherCallback() {
                        @Override
                        public void onSuccess(WeatherManager.SafetyStatus status) {
                            if (isAdded()) updateUI(status);
                        }

                        @Override
                        public void onError(String error) {
                            if (isAdded()) UIUtils.showErrorToast(requireContext(), error);
                        }
                    });
                } else {
                    UIUtils.showWarningToast(requireContext(), getString(R.string.msg_gps_no_signal));
                }
            });
        } catch (SecurityException ignored) {}
    }

    private void updateUI(WeatherManager.SafetyStatus status) {
        valWind120.setText(String.format(Locale.getDefault(), "%.1f km/h", status.wind120));
        valGusts.setText(String.format(Locale.getDefault(), "%.1f km/h", status.gusts));
        valRain.setText(String.format(Locale.getDefault(), "%.1f mm", status.rain));
        valRainProb.setText(status.rainProbability + "%");
        valKp.setText(status.kp >= 0 ? String.format(Locale.getDefault(), "Kp %.2f", status.kp) : "N/A");

        txtTemp.setText(String.format(Locale.getDefault(), "%.1f°C", status.temperature));
        txtForecast.setText(getString(status.forecastDescResId));
        
        if (status.messageArg != null) {
            txtTitle.setText(getString(status.messageResId, status.messageArg));
        } else {
            txtTitle.setText(getString(status.messageResId));
        }
        
        int color;
        int icon;

        // Semáforo de Probabilidad de Lluvia
        if (status.rainProbability > 60) valRainProb.setTextColor(requireContext().getColor(R.color.state_error));
        else if (status.rainProbability > 30) valRainProb.setTextColor(requireContext().getColor(R.color.state_warning));
        else valRainProb.setTextColor(requireContext().getColor(R.color.state_success));
        
        switch (status.level) {
            case RED:
                color = requireContext().getColor(R.color.state_error);
                icon = R.drawable.ic_toast_error;
                break;
            case YELLOW:
                color = requireContext().getColor(R.color.state_warning);
                icon = R.drawable.ic_toast_warning;
                break;
            default:
                color = requireContext().getColor(R.color.state_success);
                icon = R.drawable.ic_success_toast;
                break;
        }
        
        cardStatus.setCardBackgroundColor(color);
        imgStatus.setImageResource(icon);
        // Usaremos el txtTitle para el mensaje dinámico y desc para la recomendación general
    }
}
