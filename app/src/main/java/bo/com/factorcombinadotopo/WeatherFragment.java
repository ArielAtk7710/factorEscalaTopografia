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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    // Main Card
    private MaterialCardView cardMain;
    private ImageView imgMainIcon;
    private TextView txtMainTemp, txtMainCondition, txtLocationName;
    private LinearLayout layoutGpsWarning;
    private TextView txtGpsWarning;

    // Recommendation
    private MaterialCardView cardAssistant;
    private ImageView imgAssistantIcon;
    private TextView txtAssistantTitle, txtFlightRec;

    // Lists
    private RecyclerView rvHourly, rvWeekly;
    private HourlyAdapter hourlyAdapter;
    private WeeklyAdapter weeklyAdapter;
    private final List<WeatherManager.HourlyStatus> hourlyList = new ArrayList<>();
    private final List<WeatherManager.DailyForecast> weeklyList = new ArrayList<>();

    // Technical Details
    private View detApparent, detCloud, detVis;
    private View detWind120, detWindSust, detWindDir, detGusts, detRainProb, detRainAct;
    private View detKp;

    private FusedLocationProviderClient fusedLocationClient;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private boolean isDataLoaded = false;
    private WeatherManager.SafetyStatus lastStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Main Card
        cardMain = view.findViewById(R.id.card_main_weather);
        imgMainIcon = view.findViewById(R.id.img_main_weather_icon);
        txtMainTemp = view.findViewById(R.id.txt_main_temp);
        txtLocationName = view.findViewById(R.id.txt_location_name);
        txtMainCondition = view.findViewById(R.id.txt_main_condition);
        layoutGpsWarning = view.findViewById(R.id.layout_gps_warning);
        txtGpsWarning = view.findViewById(R.id.txt_gps_warning);

        // Bind Rec
        cardAssistant = view.findViewById(R.id.card_assistant);
        imgAssistantIcon = view.findViewById(R.id.img_assistant_icon);
        txtAssistantTitle = view.findViewById(R.id.txt_assistant_title);
        txtFlightRec = view.findViewById(R.id.txt_flight_rec);

        // Bind Lists
        rvHourly = view.findViewById(R.id.rv_hourly_weather);
        rvWeekly = view.findViewById(R.id.rv_weekly_forecast);

        // Bind Details
        detApparent = view.findViewById(R.id.detail_apparent);
        detCloud = view.findViewById(R.id.detail_cloud);
        detVis = view.findViewById(R.id.detail_vis);
        detWind120 = view.findViewById(R.id.detail_wind_120);
        detWindSust = view.findViewById(R.id.detail_wind_sust);
        detWindDir = view.findViewById(R.id.detail_wind_dir);
        detGusts = view.findViewById(R.id.detail_gusts);
        detRainProb = view.findViewById(R.id.detail_rain_prob);
        detRainAct = view.findViewById(R.id.detail_rain_act);
        detKp = view.findViewById(R.id.detail_kp);

        setupTechnicalLabels();
        setupRecyclerViews();

        view.findViewById(R.id.btn_refresh_weather).setOnClickListener(v -> loadWeatherData());
        
        cardMain.setOnClickListener(v -> {
            if (lastStatus != null) showSafetyDetailsDialog(lastStatus);
        });
        
        cardAssistant.setOnClickListener(v -> {
            if (lastStatus != null) showSafetyDetailsDialog(lastStatus);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        loadWeatherData();
    }

    private void setupTechnicalLabels() {
        setDetailLabel(detApparent, R.string.label_apparent_temp, R.drawable.ic_temp_pro);
        setDetailLabel(detCloud, R.string.label_cloud_cover, R.drawable.ic_humidity_pro);
        setDetailLabel(detVis, R.string.label_visibility, R.drawable.ic_visibility_pro);

        setDetailLabel(detWind120, R.string.label_wind_120_v, R.drawable.ic_wind_pro);
        setDetailLabel(detWindSust, R.string.label_wind_sustained, R.drawable.ic_wind_pro);
        setDetailLabel(detWindDir, R.string.label_wind_direction, R.drawable.ic_info);
        setDetailLabel(detGusts, R.string.label_gusts_max, R.drawable.ic_wind_pro);
        setDetailLabel(detRainProb, R.string.label_rain_prob_v, R.drawable.ic_rain_drop_pro);
        setDetailLabel(detRainAct, R.string.label_rain_actual, R.drawable.ic_rain_drop_pro);

        setDetailLabel(detKp, R.string.label_kp_solar, R.drawable.ic_shield_pro);
    }

    private void setDetailLabel(View container, int labelRes, int iconRes) {
        if (container == null) return;
        TextView label = container.findViewById(R.id.txt_detail_label);
        ImageView icon = container.findViewById(R.id.img_detail_icon);
        if (label != null) label.setText(labelRes);
        if (icon != null) icon.setImageResource(iconRes);
    }

    private void setDetailValue(View container, String value) {
        if (container == null) return;
        TextView txtValue = container.findViewById(R.id.txt_detail_value);
        if (txtValue != null) txtValue.setText(value);
    }

    private void setDetailValue(View container, String value, int color) {
        if (container == null) return;
        TextView txtValue = container.findViewById(R.id.txt_detail_value);
        if (txtValue != null) {
            txtValue.setText(value);
            txtValue.setTextColor(color);
        }
    }

    private void setupRecyclerViews() {
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyAdapter(hourlyList);
        rvHourly.setAdapter(hourlyAdapter);

        rvWeekly.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        weeklyAdapter = new WeeklyAdapter(weeklyList);
        rvWeekly.setAdapter(weeklyAdapter);
    }

    private void loadWeatherData() {
        isDataLoaded = false;
        timeoutHandler.postDelayed(() -> {
            if (!isDataLoaded && isAdded()) {
                UIUtils.showInfoToast(requireContext(), "Actualizando información meteorológica...");
            }
        }, 3000);

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    updateLocationName(location.getLatitude(), location.getLongitude());
                    // Simular captura de PDOP (en una app real vendría de GnssStatus o extras)
                    double currentPdop = 1.8; 

                    WeatherManager.checkFlightSafety(requireContext(), location.getLatitude(), location.getLongitude(), currentPdop, new WeatherManager.WeatherCallback() {
                        @Override
                        public void onSuccess(WeatherManager.SafetyStatus status) {
                            isDataLoaded = true;
                            lastStatus = status;
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

    private void updateLocationName(double lat, double lon) {
        if (!isAdded()) return;
        
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    String cityName = address.getLocality();
                    String countryName = address.getCountryName();
                    
                    final String locationDisplay = (cityName != null ? cityName : "") + 
                                                   (cityName != null && countryName != null ? ", " : "") + 
                                                   (countryName != null ? countryName : "");
                    
                    if (!locationDisplay.isEmpty()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (isAdded() && txtLocationName != null) {
                                txtLocationName.setText(locationDisplay);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI(WeatherManager.SafetyStatus status) {
        if (!isAdded()) return;

        // Main Card
        txtMainTemp.setText(String.format(Locale.getDefault(), "%.1f°C", status.temperature));
        txtMainCondition.setText(getString(status.forecastDescResId));
        imgMainIcon.setImageResource(obtenerIconoClima(status.forecastDescResId, status.isDay == 1));
        
        // Dynamic background
        int cardBg = ContextCompat.getColor(requireContext(), R.color.bg_weather_clear);
        if (status.forecastDescResId == R.string.weather_desc_61_65 || status.forecastDescResId == R.string.weather_desc_95_99) {
            cardBg = ContextCompat.getColor(requireContext(), R.color.bg_weather_rain);
        } else if (status.forecastDescResId == R.string.weather_desc_45_48 || status.forecastDescResId == R.string.weather_desc_1_3) {
            cardBg = ContextCompat.getColor(requireContext(), R.color.bg_weather_cloudy);
        }
        cardMain.setCardBackgroundColor(cardBg);

        // Semáforo Avanzado (Borde)
        int strokeColor = ContextCompat.getColor(requireContext(), R.color.flight_green);
        switch(status.safetyAnalysis.nivel) {
            case AMARILLO: strokeColor = ContextCompat.getColor(requireContext(), R.color.flight_yellow); break;
            case NARANJA: strokeColor = ContextCompat.getColor(requireContext(), R.color.flight_orange); break;
            case ROJO: strokeColor = ContextCompat.getColor(requireContext(), R.color.flight_red); break;
        }
        cardMain.setStrokeColor(strokeColor);
        cardMain.setStrokeWidth(8); 

        // GPS Warning (Advanced message)
        if (status.safetyAnalysis.nivel != FlightSafetyLevel.VERDE) {
            layoutGpsWarning.setVisibility(View.VISIBLE);
            txtGpsWarning.setText(status.safetyAnalysis.mensajeBreve);
        } else {
            layoutGpsWarning.setVisibility(View.GONE);
        }

        // Assistant
        txtFlightRec.setText(status.safetyAnalysis.ventanaOptima);
        txtAssistantTitle.setTextColor(strokeColor);
        imgAssistantIcon.setColorFilter(strokeColor);

        // Technical Details
        setDetailValue(detApparent, String.format(Locale.getDefault(), "%.1f°C", status.apparentTemperature));
        setDetailValue(detCloud, status.cloudCover + "%");
        setDetailValue(detVis, String.format(Locale.getDefault(), "%.1f km", status.visibility));

        setDetailValue(detWind120, String.format(Locale.getDefault(), "%.1f km/h", status.wind120));
        setDetailValue(detWindSust, String.format(Locale.getDefault(), "%.1f km/h", status.windSustained));
        setDetailValue(detWindDir, getCardinalDirection(status.windDirection));
        setDetailValue(detGusts, String.format(Locale.getDefault(), "%.1f km/h", status.gusts));
        setDetailValue(detRainProb, status.rainProbability + "%");
        setDetailValue(detRainAct, String.format(Locale.getDefault(), "%.1f mm", status.rain));

        // Kp Index with color
        String kpText = status.kp >= 0 ? String.format(Locale.getDefault(), "%.1f", status.kp) : "N/A";
        int kpColor = ContextCompat.getColor(requireContext(), R.color.weather_text_primary);
        if (status.kp >= 0 && status.kp < 4) {
            kpText += " (Bajo)";
            kpColor = ContextCompat.getColor(requireContext(), R.color.weather_green_safe);
        } else if (status.kp >= 5) {
            kpText += " (Alto)";
            kpColor = ContextCompat.getColor(requireContext(), R.color.weather_orange);
        }
        setDetailValue(detKp, kpText, kpColor);

        // Lists
        hourlyList.clear();
        hourlyList.addAll(status.hourlyList);
        hourlyAdapter.notifyDataSetChanged();

        weeklyList.clear();
        weeklyList.addAll(status.dailyList);
        weeklyAdapter.notifyDataSetChanged();
    }

    private int obtenerIconoClima(int descResId, boolean isDay) {
        if (descResId == R.string.weather_desc_0) return isDay ? R.drawable.ic_weather_clear : R.drawable.ic_weather_night;
        if (descResId == R.string.weather_desc_1_3) return R.drawable.ic_weather_partly_cloudy;
        if (descResId == R.string.weather_desc_45_48) return R.drawable.ic_weather_fog;
        if (descResId == R.string.weather_desc_51_55) return R.drawable.ic_weather_rain;
        if (descResId == R.string.weather_desc_61_65) return R.drawable.ic_weather_heavy_rain;
        if (descResId == R.string.weather_desc_95_99) return R.drawable.ic_weather_storm;
        if (descResId == R.string.weather_desc_71_75) return R.drawable.ic_weather_snow;
        return R.drawable.ic_weather_cloudy;
    }

    private String getCardinalDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SO", "O", "NO", "N"};
        return directions[(int) Math.round((degrees % 360) / 45.0)];
    }

    private void showSafetyDetailsDialog(WeatherManager.SafetyStatus status) {
        View dv = getLayoutInflater().inflate(R.layout.layout_dialog_safety_details, null);
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        androidx.appcompat.app.AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        ImageView imgIcon = dv.findViewById(R.id.img_dialog_icon);
        TextView txtTitle = dv.findViewById(R.id.txt_dialog_title);
        TextView txtDesc = dv.findViewById(R.id.txt_dialog_desc);
        TextView txtRec = dv.findViewById(R.id.txt_dialog_rec);

        txtTitle.setText(getString(R.string.safety_report_title));
        txtRec.setText(status.safetyAnalysis.recomendacion);

        // Construir descripción con causas y disclaimer
        StringBuilder sb = new StringBuilder();
        sb.append(status.safetyAnalysis.detalle).append("\n\n");
        
        if (!status.safetyAnalysis.causas.isEmpty()) {
            sb.append(getString(R.string.label_detected_causes)).append("\n");
            for (String causa : status.safetyAnalysis.causas) {
                sb.append("• ").append(causa).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append(getString(R.string.msg_safety_disclaimer));
        txtDesc.setText(sb.toString());

        // Icono dinámico según el riesgo principal determinado por el analizador
        imgIcon.setImageResource(status.safetyAnalysis.mainIconRes);
        
        // Ajustar color del icono según nivel
        int tint = ContextCompat.getColor(requireContext(), R.color.flight_green);
        switch(status.safetyAnalysis.nivel) {
            case AMARILLO: tint = ContextCompat.getColor(requireContext(), R.color.flight_yellow); break;
            case NARANJA: tint = ContextCompat.getColor(requireContext(), R.color.flight_orange); break;
            case ROJO: tint = ContextCompat.getColor(requireContext(), R.color.flight_red); break;
        }
        imgIcon.setColorFilter(tint);

        dv.findViewById(R.id.btn_dialog_close).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    // Adaptadores
    private class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
        private final List<WeatherManager.HourlyStatus> list;
        HourlyAdapter(List<WeatherManager.HourlyStatus> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_hourly_weather, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            WeatherManager.HourlyStatus hs = list.get(pos);
            h.txtTime.setText(hs.time);
            h.txtTemp.setText(String.format(Locale.getDefault(), "%.0f°", hs.wind)); 
            h.txtWind.setText(String.format(Locale.getDefault(), "%.0f km/h", hs.wind));
            h.txtRain.setText(hs.rainProb + "%");
            h.imgIcon.setImageResource(obtenerIconoClimaFromCode(hs.weatherCode, hs.time));
            if (hs.level == WeatherManager.SafetyLevel.YELLOW) h.card.setStrokeColor(Color.YELLOW);
            else if (hs.level == WeatherManager.SafetyLevel.RED) h.card.setStrokeColor(Color.RED);
            else h.card.setStrokeColor(Color.TRANSPARENT);
        }
        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTime, txtTemp, txtWind, txtRain; ImageView imgIcon; MaterialCardView card;
            ViewHolder(View v) {
                super(v);
                txtTime = v.findViewById(R.id.txt_hourly_time);
                txtTemp = v.findViewById(R.id.txt_hourly_temp);
                txtWind = v.findViewById(R.id.txt_hourly_wind);
                txtRain = v.findViewById(R.id.txt_hourly_rain);
                imgIcon = v.findViewById(R.id.img_hourly_icon);
                card = (MaterialCardView) v;
            }
        }
    }

    private class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.ViewHolder> {
        private final List<WeatherManager.DailyForecast> list;
        WeeklyAdapter(List<WeatherManager.DailyForecast> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_daily_weather, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            WeatherManager.DailyForecast df = list.get(pos);
            h.txtDay.setText(formatDate(df.date));
            h.txtTempRange.setText(String.format(Locale.getDefault(), "%.0f° / %.0f°", df.tempMax, df.tempMin));
            h.txtRain.setText(df.rainProb + "%");
            h.imgIcon.setImageResource(obtenerIconoClimaFromCode(df.weatherCode, "12:00")); 
        }
        @Override public int getItemCount() { return list.size(); }
        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = sdf.parse(dateStr);
                return new SimpleDateFormat("EEE", Locale.getDefault()).format(date);
            } catch (Exception e) { return dateStr; }
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDay, txtTempRange, txtRain; ImageView imgIcon;
            ViewHolder(View v) {
                super(v);
                txtDay = v.findViewById(R.id.txt_daily_day);
                txtTempRange = v.findViewById(R.id.txt_daily_temp_range);
                txtRain = v.findViewById(R.id.txt_daily_rain);
                imgIcon = v.findViewById(R.id.img_daily_icon);
            }
        }
    }

    private int obtenerIconoClimaFromCode(int code, String time) {
        boolean isNight = false;
        if (time != null && time.length() >= 2) {
            try {
                int hour = Integer.parseInt(time.substring(0, 2));
                if (hour >= 19 || hour <= 6) isNight = true;
            } catch (Exception ignored) {}
        }

        if (code == 0) return isNight ? R.drawable.ic_weather_night : R.drawable.ic_weather_clear;
        if (code <= 3) return R.drawable.ic_weather_partly_cloudy;
        if (code <= 48) return R.drawable.ic_weather_fog;
        if (code <= 55) return R.drawable.ic_weather_rain;
        if (code <= 65) return R.drawable.ic_weather_heavy_rain;
        if (code <= 99) return R.drawable.ic_weather_storm;
        return R.drawable.ic_weather_cloudy;
    }
}
