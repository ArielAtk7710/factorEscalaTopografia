package bo.com.factorcombinadotopo;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.google.android.material.switchmaterial.SwitchMaterial;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText et1;
    private EditText et2;
    private Typeface fontAwesome;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager2 mViewPager;
    private DrawerLayout drawer;
    private View layoutTabsBottom;
    private View mainContentFrame;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_LANG = "Language";
    private static final String KEY_THEME = "Theme";
    public static final String KEY_PRESSURE_OFFSET = "PressureOffset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar preferencias antes de crear la vista
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, "es");
        updateLocale(lang);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawer = findViewById(R.id.drawer_layout);
        layoutTabsBottom = findViewById(R.id.layout_tabs_bottom);
        mainContentFrame = findViewById(R.id.main_content_frame);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.btn_confirm, R.string.btn_cancel);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.mSectionsPagerAdapter = new SectionsPagerAdapter(this);
        this.mViewPager = findViewById(R.id.container);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        
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
                    tab.setText(R.string.tab_text_3);
                    tab.setIcon(R.drawable.ic_register);
                    break;
            }
        }).attach();

        // Al tocar un tab, si estamos en un fragmento "extra", volvemos al home
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
        this.fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

        // Insertar datos de ejemplo si el registro está vacío
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.seedExampleData();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        } else if (id == R.id.nav_map) {
            showMap();
        } else if (id == R.id.nav_weather) {
            showWeather();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHome() {
        // Remover cualquier fragmento adicional que se haya puesto sobre el FrameLayout
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof CompassFragment || fragment instanceof MapFragment 
                    || fragment instanceof FieldNotebookFragment || fragment instanceof WeatherFragment) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        mViewPager.setVisibility(View.VISIBLE);
    }

    private void showCompassPro() {
        hideMainAndShowFragment(new CompassFragment());
    }

    private void showFieldNotebook() {
        hideMainAndShowFragment(new FieldNotebookFragment());
    }

    private void showMap() {
        hideMainAndShowFragment(new MapFragment());
    }

    private void showWeather() {
        hideMainAndShowFragment(new WeatherFragment());
    }

    private void hideMainAndShowFragment(Fragment fragment) {
        mViewPager.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content_frame, fragment)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            UIUtils.showInfoToast(this, getString(R.string.menu_about));
            return true;
        } else if (id == R.id.action_tutorial) {
            UIUtils.showInfoToast(this, getString(R.string.menu_tutorial));
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

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(view);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 1. Configurar Idioma
        Spinner spLang = view.findViewById(R.id.sp_language);
        String[] langNames = {getString(R.string.lang_es), getString(R.string.lang_en), getString(R.string.lang_pt), getString(R.string.lang_fr)};
        String[] langCodes = {"es", "en", "pt", "fr"};
        
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

        // 2. Configurar Tema
        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);
        TextView txtStatus = view.findViewById(R.id.txt_theme_status);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        switchTheme.setChecked(isDark);
        txtStatus.setText(isDark ? R.string.label_dark_mode : R.string.label_light_mode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_THEME, isChecked).apply();
            txtStatus.setText(isChecked ? R.string.label_dark_mode : R.string.label_light_mode);
        });

        // 3. Configurar Offset de Presión
        EditText etOffset = view.findViewById(R.id.et_pressure_offset);
        float currentOffset = prefs.getFloat(KEY_PRESSURE_OFFSET, 0f);
        if (currentOffset != 0f) etOffset.setText(String.valueOf(currentOffset));

        view.findViewById(R.id.btn_close_settings).setOnClickListener(v -> {
            String offsetStr = etOffset.getText().toString();
            try {
                float offset = offsetStr.isEmpty() ? 0f : Float.parseFloat(offsetStr);
                prefs.edit().putFloat(KEY_PRESSURE_OFFSET, offset).apply();
            } catch (Exception ignored) {}
            
            dialog.dismiss();
            recreate(); // Reiniciar para aplicar cambios
        });

        dialog.show();
    }

    private void showCompassCalibrateDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_calibrate_compass, null);
        dialog.setContentView(view);
        view.findViewById(R.id.btn_close_calibrate).setOnClickListener(v -> dialog.dismiss());
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

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, Integer.valueOf(getArguments().getInt(ARG_SECTION_NUMBER))));
            return rootView;
        }
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
                    return new RegisterFragment();
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
