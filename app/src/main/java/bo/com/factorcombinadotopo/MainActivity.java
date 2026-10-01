package bo.com.factorcombinadotopo;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.app.DatePickerDialog;
import android.location.LocationManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.google.android.material.switchmaterial.SwitchMaterial;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.util.concurrent.Executors;

import android.widget.PopupWindow;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.core.content.FileProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import androidx.lifecycle.ViewModelProvider;

import androidx.annotation.NonNull;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import android.widget.ImageView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Color;

import bo.com.factorcombinadotopo.GeodesicFragment;
import bo.com.factorcombinadotopo.DistanceReductionFragment;
import bo.com.factorcombinadotopo.LineCalculatorFragment;
import bo.com.factorcombinadotopo.LambertFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager2 mViewPager;
    private DrawerLayout drawer;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_LANG = "Language";
    private static final String KEY_THEME = "Theme";
    public static final String KEY_PRESSURE_OFFSET = "PressureOffset";
    public static final String KEY_GEOID_MODEL = "GeoidModel"; // 0: EGM96, 1: MGBol08

    // Nuevas llaves para ajustes de Mapas
    public static final String KEY_SHOW_LOCATION = "ShowLocation";
    public static final String KEY_REAL_TIME_UPDATE = "RealTimeUpdate";
    private static final String KEY_TERMS_ACCEPTED = "TermsAccepted";

    private TextView txtCacheSizeStreet;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SurveyViewModel viewModel;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private ImageView imgOfflineStatus;
    private android.net.ConnectivityManager.NetworkCallback networkCallback;

    private boolean isGpsEnabledGlobal = true;
    private final BroadcastReceiver gpsStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                checkGlobalGpsState(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Cargar preferencias de la App
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Asegurar modo Online por defecto en el primer inicio absoluto
        if (!prefs.contains(MapManager.KEY_MAP_TYPE)) {
            prefs.edit().putInt(MapManager.KEY_MAP_TYPE, 0).apply();
        }
        if (!prefs.contains(KEY_GEOID_MODEL)) {
            prefs.edit().putInt(KEY_GEOID_MODEL, 1).apply();
        }
        String lang = prefs.getString(KEY_LANG, "es");
        updateLocale(lang);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // 2.1 Verificar aceptación de Términos y Condiciones
        if (!prefs.getBoolean(KEY_TERMS_ACCEPTED, false)) {
            showTermsDialog();
        }

        setupPermissionLauncher();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawer = findViewById(R.id.drawer_layout);
        imgOfflineStatus = findViewById(R.id.img_offline_status);
        setupNetworkMonitoring();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.btn_confirm, R.string.btn_cancel);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.viewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (viewModel != null) viewModel.processNewLocation(result.getLastLocation());
            }
        };

        this.mSectionsPagerAdapter = new SectionsPagerAdapter(this);
        this.mViewPager = findViewById(R.id.container);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mViewPager.setUserInputEnabled(false); // Desactivar deslizamiento para no interferir con el mapa
        this.mViewPager.setOffscreenPageLimit(1); // Mantener pestañas adyacentes vivas para evitar recargas del mapa
        
        // 🛡️ PROTECCIÓN DE CARGA: Esperar a que los servicios críticos estén listos
        ((SurveyApplication)getApplication()).isReady.observe(this, isReady -> {
            if (isReady) {
                UIUtils.showSuccessToast(this, "Motor Geoidal y Sistemas Listos");
            } else {
                UIUtils.showInfoToast(this, "Iniciando sistemas...");
            }
        });
        
        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, mViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_text_1);
                    tab.setIcon(R.drawable.ic_auto);
                    break;
                case 1:
                    tab.setText(R.string.tab_text_2);
                    tab.setIcon(R.drawable.ic_manual);
                    break;
                case 2:
                    tab.setText(R.string.tab_text_map);
                    tab.setIcon(R.drawable.ic_map);
                    break;
                case 3:
                    tab.setText(R.string.tab_text_3);
                    tab.setIcon(R.drawable.ic_register);
                    break;
            }
        }).attach();

        // Sincronización del menú inferior con las pantallas secundarias
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mViewPager.getVisibility() != View.VISIBLE) {
                    showHome();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {
                if (mViewPager.getVisibility() != View.VISIBLE) {
                    showHome();
                }
            }
        });
    }

    private void showTermsDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_terms, null);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false); // Obligatorio aceptar o salir
        
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btn_terms_exit).setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Cerrar app si no acepta
        });

        view.findViewById(R.id.btn_terms_accept).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, true).apply();
            dialog.dismiss();
            
            // Disparar solicitud de permisos inmediatamente después de aceptar términos
            solicitarPermisosIniciales();
        });

        dialog.show();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    
                    if (fineLocationGranted != null && fineLocationGranted) {
                        UIUtils.showSuccessToast(this, "Permiso de ubicación concedido");
                        startLocationUpdates();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        UIUtils.showInfoToast(this, "Ubicación aproximada concedida. Se recomienda alta precisión.");
                        startLocationUpdates();
                    } else {
                        UIUtils.showWarningToast(this, "La app requiere GPS para funcionar correctamente.");
                    }
                }
        );
    }

    private void solicitarPermisosIniciales() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ no usa READ_EXTERNAL_STORAGE para archivos generales
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        requestPermissionLauncher.launch(permissions);
    }

    private void setupNetworkMonitoring() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        networkCallback = new android.net.ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull android.net.Network network) {
                NetworkUtils.resetOfflineWarning();
                runOnUiThread(() -> { 
                    if (imgOfflineStatus != null) imgOfflineStatus.setVisibility(View.GONE);
                    updateFragmentsNetworkState(true);
                });
            }

            @Override
            public void onLost(@NonNull android.net.Network network) {
                runOnUiThread(() -> { 
                    if (imgOfflineStatus != null) imgOfflineStatus.setVisibility(View.VISIBLE);
                    updateFragmentsNetworkState(false);
                });
            }
        };

        cm.registerDefaultNetworkCallback(networkCallback);
        
        // Estado inicial
        if (imgOfflineStatus != null) {
            boolean isAvailable = NetworkUtils.isNetworkAvailable(this);
            imgOfflineStatus.setVisibility(isAvailable ? View.GONE : View.VISIBLE);
            updateFragmentsNetworkState(isAvailable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Verificar si hay fragmentos activos sobre el home
            boolean hasSecondaryFragment = false;
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof CompassFragment || f instanceof FieldNotebookFragment || 
                    f instanceof StakeoutFragment || f instanceof WeatherFragment ||
                    f instanceof GeodesicFragment || f instanceof DistanceReductionFragment ||
                    f instanceof LineCalculatorFragment || f instanceof LambertFragment) {
                    hasSecondaryFragment = true;
                    break;
                }
            }

            if (hasSecondaryFragment) {
                showHome();
            } else {
                UIUtils.showConfirmDialog(this, 
                    R.string.title_exit_app, 
                    R.string.msg_exit_app, 
                    () -> {
                        MapManager.clearSession(); // Limpiar pines al salir
                        finishAffinity();
                    });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            showHome();
        } else if (id == R.id.nav_gnss_calendar_pro) {
            showGnssCalendarDialog();
        } else if (id == R.id.nav_compass_pro) {
            showCompassPro();
        } else if (id == R.id.nav_field_notebook) {
            showFieldNotebook();
        } else if (id == R.id.nav_stakeout) {
            showStakeout();
        } else if (id == R.id.nav_weather) {
            showWeather();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateFragmentsNetworkState(boolean isOnline) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MapFragment) {
                ((MapFragment) fragment).setNetworkState(isOnline);
            }
        }
    }

    private void showHome() {
        // Remover cualquier fragmento adicional que se haya puesto sobre el FrameLayout
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof CompassFragment || 
                fragment instanceof FieldNotebookFragment || 
                fragment instanceof StakeoutFragment ||
                fragment instanceof WeatherFragment) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        
        // Dar un respiro al sistema para limpiar fragmentos antes de mostrar el ViewPager (Mapa)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mViewPager != null) {
                mViewPager.setVisibility(View.VISIBLE);
            }
        }, 300); // 300ms de carga segura
    }

    private void showCompassPro() {
        hideMainAndShowFragment(new CompassFragment());
    }

    private void showFieldNotebook() {
        hideMainAndShowFragment(new FieldNotebookFragment());
    }

    private void showStakeout() {
        hideMainAndShowFragment(new StakeoutFragment());
    }

    private void showWeather() {
        hideMainAndShowFragment(new WeatherFragment());
    }

    private void hideMainAndShowFragment(Fragment fragment) {
        if (mViewPager != null) {
            mViewPager.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, fragment) // Usar replace en lugar de add para evitar superposiciones
                .commit();
    }

    private void updateLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, android.view.Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                java.lang.reflect.Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_tutorial) {
            showQuickGuideDialog();
            return true;
        } else if (id == R.id.action_calibrate) {
            showCompassCalibrateDialog();
            return true;
        } else if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_about, null);
        dialog.setContentView(view);

        TextView txtCollabBody = view.findViewById(R.id.txt_about_collaborators_body);
        if (txtCollabBody != null) {
            txtCollabBody.setText(getText(R.string.about_collaborators_body));
        }
        
        final int[] _x_val = {0};
        View _v_trig = view.findViewById(R.id.txt_about_collaborators_title);
        if (_v_trig != null) {
            _v_trig.setOnClickListener(v -> {
                _x_val[0]++;
                if (_x_val[0] >= 5) {
                    _x_val[0] = 0;
                    _show_ext_ms();
                }
            });
        }
        
        view.findViewById(R.id.btn_about_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showQuickGuideDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_guide, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setView(view);

        view.findViewById(R.id.btn_guide_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void _show_ext_ms() {
        try {
            View layout = getLayoutInflater().inflate(R.layout.layout_ms_ext, null);
            ImageView img = layout.findViewById(R.id.ext_img);
            
            InputStream is = getAssets().open("images/pandapache.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            img.setImageBitmap(bitmap);
            
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(android.view.Gravity.CENTER, 0, 0); // Centrar en pantalla
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(view);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 1. Configurar Idioma
        Spinner spLang = view.findViewById(R.id.sp_language);
        String[] langNames = {getString(R.string.lang_es), getString(R.string.lang_en)};
        String[] langCodes = {"es", "en"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, langNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLang.setAdapter(adapter);

        String currentLang = prefs.getString(KEY_LANG, "es");
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                spLang.setSelection(i);
                break;
            }
        }

        spLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!langCodes[position].equals(prefs.getString(KEY_LANG, "es"))) {
                    prefs.edit().putString(KEY_LANG, langCodes[position]).apply();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 1.1 Configurar Modelo Geoidal
        com.google.android.material.button.MaterialButtonToggleGroup toggleGeoid = view.findViewById(R.id.toggle_geoid_model);
        int savedGeoid = prefs.getInt(KEY_GEOID_MODEL, 1); // 1 es MGBol08 por defecto
        if (savedGeoid == 1) toggleGeoid.check(R.id.btn_geoid_mgb);
        else toggleGeoid.check(R.id.btn_geoid_egm96);

        toggleGeoid.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int model = (checkedId == R.id.btn_geoid_mgb) ? 1 : 0;
                prefs.edit().putInt(KEY_GEOID_MODEL, model).apply();
                
                // Mostrar Toast Informativo
                String msg = (model == 1) ? getString(R.string.msg_using_mgbol) : getString(R.string.msg_using_egm96);
                UIUtils.showInfoToast(this, msg);
            }
        });

        View btnGeoidInfo = view.findViewById(R.id.btn_geoid_info);
        btnGeoidInfo.setOnClickListener(v -> {
            UIUtils.showPopupInfo(this, view, 
                    getString(R.string.title_geoid_adjustment), 
                    getString(R.string.msg_geoid_mgb_info));
        });

        // 2. Configurar Offset de Presión (0.0 por defecto)
        EditText etOffset = view.findViewById(R.id.et_pressure_offset);
        float currentOffset = prefs.getFloat(KEY_PRESSURE_OFFSET, 0.0f);
        etOffset.setText(String.valueOf(currentOffset));

        View btnBarometerInfo = view.findViewById(R.id.btn_barometer_info);
        btnBarometerInfo.setOnClickListener(v -> UIUtils.showBarometerInfo(this));

        // 3. SECCIÓN MAPAS (Solo ONLINE disponible en Ajustes)
        View btnMapInfo = view.findViewById(R.id.btn_map_info);
        btnMapInfo.setOnClickListener(v -> {
            UIUtils.showPopupInfo(this, view, 
                    getString(R.string.label_maps), 
                    getString(R.string.msg_map_offline_auto));
        });

        com.google.android.material.button.MaterialButtonToggleGroup toggleMapMode = view.findViewById(R.id.toggle_map_mode);
        int currentMapType = prefs.getInt(MapManager.KEY_MAP_TYPE, 0);
        toggleMapMode.check(currentMapType == 1 ? R.id.btn_mode_offline_aesthetic : R.id.btn_mode_online);
        
        // El modo del mapa se gestiona principalmente desde el Fragmento de Mapa,
        // pero permitimos visualizar el estado actual en Ajustes.

        // Caché
        txtCacheSizeStreet = view.findViewById(R.id.txt_cache_size_street);
        updateCacheSizeUI();
        
        view.findViewById(R.id.btn_clear_cache_street).setOnClickListener(v -> {
            UIUtils.showConfirmDialog(this, 
                R.string.title_confirm_cache_street, 
                R.string.msg_confirm_cache_street, 
                this::clearMapCache);
        });

        // GPS
        com.google.android.material.checkbox.MaterialCheckBox cbShowLoc = view.findViewById(R.id.cb_show_location);
        com.google.android.material.checkbox.MaterialCheckBox cbUpdateRealTime = view.findViewById(R.id.cb_real_time_update);
        cbShowLoc.setChecked(prefs.getBoolean(KEY_SHOW_LOCATION, true));
        cbUpdateRealTime.setChecked(prefs.getBoolean(KEY_REAL_TIME_UPDATE, true));

        cbShowLoc.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(KEY_SHOW_LOCATION, isChecked).apply());
        cbUpdateRealTime.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(KEY_REAL_TIME_UPDATE, isChecked).apply());

        // 4. Configurar Tema
        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);
        TextView txtStatus = view.findViewById(R.id.txt_theme_status);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        switchTheme.setChecked(isDark);
        txtStatus.setText(isDark ? R.string.label_dark_mode : R.string.label_light_mode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_THEME, isChecked).apply();
            txtStatus.setText(isChecked ? R.string.label_dark_mode : R.string.label_light_mode);
        });

        // 5. Configurar Sección LICENCIA
        TextView txtLicType = view.findViewById(R.id.txt_license_type);
        TextView txtLicStatus = view.findViewById(R.id.txt_license_status);
        TextView txtLicExp = view.findViewById(R.id.txt_license_expiration);
        TextView txtLicDays = view.findViewById(R.id.txt_license_days_remaining);
        EditText etCode = view.findViewById(R.id.et_activation_code);
        View btnActivate = view.findViewById(R.id.btn_activate_license);

        Runnable updateLicenseUI = () -> {
            LicenseManager.LicenseInfo info = LicenseManager.getActiveLicense(this);
            if (txtLicType != null) txtLicType.setText(info.tipo);
            if (txtLicStatus != null) {
                if (info.isExpired) {
                    txtLicStatus.setText(R.string.status_license_expired);
                    txtLicStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.state_error));
                } else {
                    txtLicStatus.setText(R.string.status_license_active);
                    txtLicStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.state_success));
                }
            }
            if (txtLicExp != null) {
                String cleanExp = info.fechaExpiracion;
                if (cleanExp != null && cleanExp.length() >= 10) {
                    String[] parts = cleanExp.substring(0, 10).split("-");
                    if (parts.length == 3) cleanExp = parts[2] + "/" + parts[1] + "/" + parts[0];
                }
                txtLicExp.setText(cleanExp);
            }
            if (txtLicDays != null) {
                txtLicDays.setText(String.format(java.util.Locale.getDefault(), "%d días", info.diasRestantes));
            }
        };

        updateLicenseUI.run();

        if (btnActivate != null) {
            btnActivate.setOnClickListener(v -> {
                String inputCode = etCode.getText().toString().trim();
                if (inputCode.length() == 8 && LicenseManager.activateCode(this, inputCode)) {
                    LicenseManager.LicenseInfo newInfo = LicenseManager.getActiveLicense(this);
                    String msg = String.format(getString(R.string.msg_license_activated_success), newInfo.tipo, newInfo.fechaExpiracion);
                    UIUtils.showInfoToast(this, msg);
                    etCode.setText("");
                    updateLicenseUI.run();
                } else {
                    UIUtils.showErrorToast(this, getString(R.string.msg_invalid_activation_code));
                }
            });
        }

        view.findViewById(R.id.btn_close_settings).setOnClickListener(v -> {
            String offsetStr = etOffset.getText().toString();
            try {
                float offset = offsetStr.isEmpty() ? 0f : Float.parseFloat(offsetStr);
                prefs.edit().putFloat(KEY_PRESSURE_OFFSET, offset).apply();
            } catch (Exception ignored) {}
            
            dialog.dismiss();
            recreate(); // Reiniciar para aplicar cambios
        });

        dialog.setOnDismissListener(d -> {
            txtCacheSizeStreet = null;
        });

        dialog.show();
    }

    private void updateCacheSizeUI() {
        if (txtCacheSizeStreet == null) return;
        
        txtCacheSizeStreet.setText("...");

        TopographyRepository.getInstance(this).runOnBackground(() -> {
            File osmdroidDir = new File(getExternalFilesDir(null), "osmdroid");
            final long totalSize = FileUtils.getFolderSize(new File(osmdroidDir, "tiles_cache"));

            new Handler(Looper.getMainLooper()).post(() -> {
                if (txtCacheSizeStreet != null) txtCacheSizeStreet.setText(FileUtils.formatSize(totalSize));
            });
        });
    }

    private void clearMapCache() {
        if (txtCacheSizeStreet != null) txtCacheSizeStreet.setText("...");

        TopographyRepository.getInstance(this).runOnBackground(() -> {
            File osmdroidDir = new File(getExternalFilesDir(null), "osmdroid");
            File cacheDir = new File(osmdroidDir, "tiles_cache");
            
            FileUtils.clearDirectory(cacheDir);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                updateCacheSizeUI();
                UIUtils.showSuccessToast(this, getString(R.string.msg_cache_cleared));
            });
        });
    }

    private void showCompassCalibrateDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_calibrate_compass, null);
        dialog.setContentView(view);

        TextView txtStatus = view.findViewById(R.id.txt_sensor_status);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener calibrationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {}

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                if (txtStatus == null) return;
                switch (accuracy) {
                    case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                        txtStatus.setText(getString(R.string.label_high_precision_status));
                        txtStatus.setTextColor(Color.GREEN);
                        break;
                    case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                        txtStatus.setText(getString(R.string.precision_medium_move));
                        txtStatus.setTextColor(Color.YELLOW);
                        break;
                    default:
                        txtStatus.setText(getString(R.string.precision_low_calibrate));
                        txtStatus.setTextColor(Color.RED);
                        break;
                }
            }
        };

        if (magnetometer != null) {
            sensorManager.registerListener(calibrationListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            txtStatus.setText("Sensor no disponible");
        }

        view.findViewById(R.id.btn_close_calibrate).setOnClickListener(v -> dialog.dismiss());
        
        dialog.setOnDismissListener(d -> {
            if (magnetometer != null) sensorManager.unregisterListener(calibrationListener);
        });

        dialog.show();
    }

    private void showGnssCalendarDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_gnss_calendar, null);
        dialog.setContentView(view);

        TextView txtJulian = view.findViewById(R.id.txt_julian_day);
        TextView txtDoy = view.findViewById(R.id.txt_doy);
        TextView txtGpsWeek = view.findViewById(R.id.txt_gps_week);
        TextView txtGpsWeekNum = view.findViewById(R.id.txt_gps_week_num);
        TextView txtDateDisplay = view.findViewById(R.id.txt_current_date_display);
        android.widget.Button btnSelectDate = view.findViewById(R.id.btn_select_date);

        Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        updateGnssFields(current, txtJulian, txtDoy, txtGpsWeek, txtGpsWeekNum, txtDateDisplay);

        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                selected.set(year, month, dayOfMonth, 12, 0, 0);
                updateGnssFields(selected, txtJulian, txtDoy, txtGpsWeek, txtGpsWeekNum, txtDateDisplay);
            }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
            datePicker.setOnShowListener(dialogInterface -> {
                try {
                    android.widget.Button posButton = datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE);
                    android.widget.Button negButton = datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE);
                    if (posButton != null) posButton.setTextColor(getResources().getColor(R.color.accent_orange, null));
                    if (negButton != null) negButton.setTextColor(getResources().getColor(R.color.text_primary, null));
                } catch (Exception ignored) {}
            });
            datePicker.show();
        });

        dialog.show();
    }

    private void updateGnssFields(Calendar cal, TextView txtJulian, TextView txtDoy, TextView txtWeek, TextView txtWeekNum, TextView txtDate) {
        Locale spanishLocale = new Locale("es", "ES");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy '(UTC)'", spanishLocale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateStr = sdf.format(cal.getTime());
        // Capitalizar primera letra
        txtDate.setText(dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1));

        // Fecha Base GPS: 6 de Enero de 1980
        Calendar base = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        base.set(1980, 0, 6, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        long diffMillis = cal.getTimeInMillis() - base.getTimeInMillis();
        if (diffMillis < 0) {
            txtWeek.setText("----");
            txtDoy.setText("---");
            txtJulian.setText("-------.-");
            txtWeekNum.setText("-----");
            return;
        }

        long seconds = diffMillis / 1000;
        long days = seconds / 86400;
        long weeks = days / 7;
        long dayOfWeekGps = days % 7;

        // Julian Day Number (JDN)
        // 2440587.5 es el JDN para 1970-01-01 00:00:00 UTC
        double jdn = (cal.getTimeInMillis() / 86400000.0) + 2440587.5;

        txtJulian.setText(String.format(Locale.getDefault(), "%.1f", jdn));
        txtDoy.setText(String.format(Locale.getDefault(), "%03d", cal.get(Calendar.DAY_OF_YEAR)));
        txtWeek.setText(String.valueOf(weeks));
        txtWeekNum.setText(String.format(Locale.getDefault(), "%d%d", weeks, dayOfWeekGps));
    }
    /**
     * Proporciona acceso al ViewModel compartido desde los fragmentos.
     */
    public SurveyViewModel getViewModel() {
        return viewModel;
    }
    public class SectionsPagerAdapter extends FragmentStateAdapter {
        public SectionsPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AutomaticFragment();
                case 1:
                    return new ManualFragment();
                case 2:
                    return new MapFragment();
                case 3:
                    return new RegisterFragment();
                default:
                    return new AutomaticFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private void checkGlobalGpsState(boolean isInitialCheck) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;

        boolean isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        
        if (isInitialCheck) {
            isGpsEnabledGlobal = isEnabled;
            if (!isEnabled) {
                UIUtils.showWarningToast(this, getString(R.string.msg_gps_required));
            }
            return;
        }

        if (isEnabled && !isGpsEnabledGlobal) {
            UIUtils.showSuccessToast(this, getString(R.string.msg_gps_activated));
        } else if (!isEnabled && isGpsEnabledGlobal) {
            UIUtils.showWarningToast(this, getString(R.string.msg_gps_deactivated));
        }
        
        isGpsEnabledGlobal = isEnabled;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gpsStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        checkGlobalGpsState(true);
        startLocationUpdates();
        checkLicenseExpiration();
    }

    private void checkLicenseExpiration() {
        if (LicenseManager.isLicenseExpired(this)) {
            showLicenseExpiredDialog();
        }
    }

    private void showLicenseExpiredDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_license_expired);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etCode = dialog.findViewById(R.id.et_expired_activation_code);
        View btnActivate = dialog.findViewById(R.id.btn_activate_expired_license);
        View btnExit = dialog.findViewById(R.id.btn_close_app);

        if (btnActivate != null) {
            btnActivate.setOnClickListener(v -> {
                String inputCode = (etCode != null) ? etCode.getText().toString().trim() : "";
                if (inputCode.length() == 8 && LicenseManager.activateCode(this, inputCode)) {
                    LicenseManager.LicenseInfo info = LicenseManager.getActiveLicense(this);
                    String msg = String.format(getString(R.string.msg_license_activated_success), info.tipo, info.fechaExpiracion);
                    UIUtils.showInfoToast(this, msg);
                    dialog.dismiss();
                    recreate();
                } else {
                    UIUtils.showErrorToast(this, getString(R.string.msg_invalid_activation_code));
                }
            });
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                dialog.dismiss();
                finishAffinity();
            });
        }

        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gpsStateReceiver);
        stopLocationUpdates();
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build();
            fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
