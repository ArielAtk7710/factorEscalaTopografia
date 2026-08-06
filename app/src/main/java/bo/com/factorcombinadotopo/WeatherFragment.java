package bo.com.factorcombinadotopo;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    private CardView cardStatus;
    private ImageView imgStatus;
    private TextView txtTitle, txtForecast, txtTemp;
    private TextView valWind120, valWindSustained, valWindDirection, valGusts, valKp, valRain, valRainProb;
    private TextView valApparentTemp, valCloudCover, valVisibility;
    private TextView txtRecommendation;
    
    private RecyclerView rvHourly;
    private HourlyAdapter hourlyAdapter;
    private final List<WeatherManager.HourlyStatus> hourlyList = new ArrayList<>();
    
    private FusedLocationProviderClient fusedLocationClient;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private boolean isDataLoaded = false;
    private PopupWindow infoPopup;

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
        txtRecommendation = view.findViewById(R.id.txt_recommendation_msg);

        // Bindings de detalle
        valApparentTemp = view.findViewById(R.id.detail_apparent_temp).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_apparent_temp).findViewById(R.id.txt_detail_label)).setText(R.string.label_apparent_temp);

        valCloudCover = view.findViewById(R.id.detail_cloud_cover).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_cloud_cover).findViewById(R.id.txt_detail_label)).setText(R.string.label_cloud_cover);

        valVisibility = view.findViewById(R.id.detail_visibility).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_visibility).findViewById(R.id.txt_detail_label)).setText(R.string.label_visibility);

        valWind120 = view.findViewById(R.id.detail_wind_120).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_wind_120).findViewById(R.id.txt_detail_label)).setText(R.string.label_wind_120_v);

        valWindSustained = view.findViewById(R.id.detail_wind_sustained).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_wind_sustained).findViewById(R.id.txt_detail_label)).setText(R.string.label_wind_sustained);

        valWindDirection = view.findViewById(R.id.detail_wind_direction).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_wind_direction).findViewById(R.id.txt_detail_label)).setText(R.string.label_wind_direction);

        valGusts = view.findViewById(R.id.detail_gusts).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_gusts).findViewById(R.id.txt_detail_label)).setText(R.string.label_gusts_max);

        valRainProb = view.findViewById(R.id.detail_rain_prob).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_rain_prob).findViewById(R.id.txt_detail_label)).setText(R.string.label_rain_prob_v);

        valRain = view.findViewById(R.id.detail_rain).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_rain).findViewById(R.id.txt_detail_label)).setText(R.string.label_rain_actual);

        valKp = view.findViewById(R.id.detail_kp).findViewById(R.id.txt_detail_value);
        ((TextView)view.findViewById(R.id.detail_kp).findViewById(R.id.txt_detail_label)).setText(R.string.label_kp_solar);

        // Setup Info Button toggle
        ImageButton btnInfoTip = view.findViewById(R.id.btn_weather_info_tip);
        btnInfoTip.setOnClickListener(v -> toggleInfoPopup(btnInfoTip));

        // Setup RecyclerView
        rvHourly = view.findViewById(R.id.rv_hourly_weather);
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyAdapter(hourlyList);
        rvHourly.setAdapter(hourlyAdapter);

        view.findViewById(R.id.btn_refresh_weather).setOnClickListener(v -> loadWeatherData());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        loadWeatherData();
    }

    private void loadWeatherData() {
        isDataLoaded = false;
        // Iniciar temporizador de 5 segundos para el Toast de espera
        timeoutHandler.postDelayed(() -> {
            if (!isDataLoaded && isAdded()) {
                UIUtils.showInfoToastLong(requireContext(), "Espere de 10 seg. a 20 seg. para obtener la informacion.");
            }
        }, 5000);

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    WeatherManager.checkFlightSafety(location.getLatitude(), location.getLongitude(), new WeatherManager.WeatherCallback() {
                        @Override
                        public void onSuccess(WeatherManager.SafetyStatus status) {
                            isDataLoaded = true;
                            if (isAdded()) updateUI(status);
                        }

                        @Override
                        public void onError(String error) {
                            isDataLoaded = true;
                            if (isAdded()) UIUtils.showErrorToast(requireContext(), error);
                        }
                    });
                } else {
                    isDataLoaded = true;
                    UIUtils.showWarningToast(requireContext(), getString(R.string.msg_gps_no_signal));
                }
            });
        } catch (SecurityException ignored) {
            isDataLoaded = true;
        }
    }

    private void updateUI(WeatherManager.SafetyStatus status) {
        if (getContext() == null || !isAdded()) return;

        txtTemp.setText(String.format(Locale.getDefault(), "%.1f°C", status.temperature));
        txtForecast.setText(getString(status.forecastDescResId));
        txtRecommendation.setText(status.recommendation);
        
        valApparentTemp.setText(String.format(Locale.getDefault(), "%.1f°C", status.apparentTemperature));
        valCloudCover.setText(status.cloudCover + "%");
        valVisibility.setText(String.format(Locale.getDefault(), "%.1f km", status.visibility));

        valWind120.setText(String.format(Locale.getDefault(), "%.1f km/h", status.wind120));
        valWindSustained.setText(String.format(Locale.getDefault(), "%.1f km/h", status.windSustained));
        valWindDirection.setText(getCardinalDirection(status.windDirection));
        valGusts.setText(String.format(Locale.getDefault(), "%.1f km/h", status.gusts));
        
        valRainProb.setText(status.rainProbability + "%");
        valRain.setText(String.format(Locale.getDefault(), "%.1f mm", status.rain));
        valKp.setText(status.kp >= 0 ? String.format(Locale.getDefault(), "Kp %.2f", status.kp) : "N/A");

        if (status.messageArg != null) {
            txtTitle.setText(getString(status.messageResId, status.messageArg));
        } else {
            txtTitle.setText(getString(status.messageResId));
        }
        
        // Semáforo visual
        int color = ContextCompat.getColor(requireContext(), R.color.state_success);
        int icon = R.drawable.ic_success_toast;

        switch (status.level) {
            case RED:
                color = ContextCompat.getColor(requireContext(), R.color.state_error);
                icon = R.drawable.ic_toast_error;
                break;
            case YELLOW:
                color = ContextCompat.getColor(requireContext(), R.color.state_warning);
                icon = R.drawable.ic_toast_warning;
                break;
        }
        
        cardStatus.setCardBackgroundColor(color);
        imgStatus.setImageResource(icon);
        
        // Actualizar lista de horas
        hourlyList.clear();
        hourlyList.addAll(status.hourlyList);
        hourlyAdapter.notifyDataSetChanged();
    }

    private String getCardinalDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SO", "O", "NO", "N"};
        return directions[(int) Math.round((degrees % 360) / 45.0)];
    }

    private void toggleInfoPopup(View anchor) {
        if (infoPopup != null && infoPopup.isShowing()) {
            infoPopup.dismiss();
            infoPopup = null;
        } else {
            showInfoPopup(anchor);
        }
    }

    private void showInfoPopup(View anchor) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_custom_toast_pro, null);
        infoPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        
        TextView label = popupView.findViewById(R.id.toast_label);
        TextView message = popupView.findViewById(R.id.toast_message);
        
        label.setText(R.string.label_info);
        message.setText(R.string.msg_weather_accuracy_tip);
        
        infoPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        infoPopup.setOutsideTouchable(true);
        infoPopup.showAsDropDown(anchor, -250, 0); 
    }

    // --- Adaptador para el pronóstico por horas ---
    private class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
        private final List<WeatherManager.HourlyStatus> list;
        HourlyAdapter(List<WeatherManager.HourlyStatus> list) { this.list = list; }
        
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_hourly_weather, p, false));
        }
        
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            WeatherManager.HourlyStatus hs = list.get(pos);
            h.txtTime.setText(hs.time);
            h.txtWind.setText(String.format(Locale.getDefault(), "%.0f km/h", hs.wind));
            h.txtRain.setText(hs.rainProb + "%");
            h.imgIcon.setImageResource(getWeatherDescRes(hs.weatherCode));
            
            int color;
            switch (hs.level) {
                case RED: color = ContextCompat.getColor(requireContext(), R.color.state_error); break;
                case YELLOW: color = ContextCompat.getColor(requireContext(), R.color.state_warning); break;
                default: color = ContextCompat.getColor(requireContext(), R.color.state_success); break;
            }
            h.card.setCardBackgroundColor(color);
        }
        
        @Override public int getItemCount() { return list.size(); }
        
        private int getWeatherDescRes(int code) {
            switch (code) {
                case 0: return R.drawable.ic_info; // Sustituir por iconos de sol/nube si existen
                case 1: case 2: case 3: return R.drawable.ic_info;
                case 61: case 63: case 65: return R.drawable.ic_toast_error; // Lluvia
                default: return R.drawable.ic_info;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTime, txtWind, txtRain; ImageView imgIcon; CardView card;
            ViewHolder(View v) {
                super(v);
                txtTime = v.findViewById(R.id.txt_hourly_time);
                txtWind = v.findViewById(R.id.txt_hourly_wind);
                txtRain = v.findViewById(R.id.txt_hourly_rain);
                imgIcon = v.findViewById(R.id.img_hourly_icon);
                card = v.findViewById(R.id.card_hourly);
            }
        }
    }
}
