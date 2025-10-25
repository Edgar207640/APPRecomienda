package com.example.recomendaciones_app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.recomendaciones_app.fragments.BuscarFragment;
import com.example.recomendaciones_app.fragments.ConfiguracionFragment;
import com.example.recomendaciones_app.fragments.FavoritosFragment;
import com.example.recomendaciones_app.fragments.HomeFragment;
import com.example.recomendaciones_app.ui.activities.CarritoActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "ofertas_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Anotación: FUNCIONALIDAD AÑADIDA. Aplica el tema antes de mostrar la UI.
        try {
            applyTheme();
        } catch (Exception e) {
            Log.e(TAG, "Error aplicando tema", e);
        }

        try {
            setContentView(R.layout.activity_main);

            Toolbar toolbar = findViewById(R.id.toolbarMain);
            if (toolbar != null) setSupportActionBar(toolbar);

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setOnNavigationItemSelectedListener(navListener);

            createNotificationChannel();
            requestNotificationPermission();

            if (savedInstanceState == null) {
                try {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new HomeFragment()).commit();
                } catch (Exception e) {
                    Log.e(TAG, "Error al iniciar HomeFragment", e);
                    // Intentamos recuperarnos mostrando un fragmento alternativo seguro
                    try {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new BuscarFragment()).commit();
                    } catch (Exception ex) {
                        Log.e(TAG, "Error al iniciar fragmento alternativo", ex);
                    }
                }
            }
        } catch (Exception e) {
            // Capturamos cualquier excepción de inflado/layout/initialization para diagnosticar el crash
            Log.e(TAG, "Error inicializando MainActivity UI", e);
            // Mostramos un Toast mínimo y terminamos la activity para evitar cierres inesperados sin diagnóstico
            try {
                // usar runOnUiThread por si acaso
                runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this, "Error al iniciar la aplicación", android.widget.Toast.LENGTH_LONG).show());
            } catch (Exception ignored) {}
            finish();
            return;
        }
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Lee la preferencia guardada y aplica el tema.
    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(OnboardingActivity.PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(ConfiguracionFragment.KEY_DARK_MODE, isSystemInDarkMode());
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Comprueba si el sistema está en modo oscuro por defecto.
    private boolean isSystemInDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            Intent intent = new Intent(this, CarritoActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.navigation_buscar) {
                        selectedFragment = new BuscarFragment();
                    } else if (itemId == R.id.navigation_favoritos) {
                        selectedFragment = new FavoritosFragment();
                    } else if (itemId == R.id.navigation_configuracion) {
                        selectedFragment = new ConfiguracionFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                    return true;
                }
            };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                CharSequence name = getString(R.string.notification_channel_name);
                String description = getString(R.string.notification_channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                } else {
                    Log.w(TAG, "createNotificationChannel: NotificationManager es null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creando el NotificationChannel", e);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
}
