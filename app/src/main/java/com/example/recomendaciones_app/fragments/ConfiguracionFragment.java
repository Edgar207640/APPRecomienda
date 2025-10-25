package com.example.recomendaciones_app.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.recomendaciones_app.LoginActivity;
import com.example.recomendaciones_app.MainActivity;
import com.example.recomendaciones_app.OnboardingActivity;
import com.example.recomendaciones_app.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ConfiguracionFragment extends Fragment {

    private static final String CHANNEL_ID = "ofertas_channel";
    private static final int NOTIFICATION_ID = 1;
    // Anotación: FUNCIONALIDAD AÑADIDA. Clave para guardar la preferencia del modo oscuro.
    public static final String KEY_DARK_MODE = "dark_mode_enabled";

    private MaterialSwitch switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // --- Lógica existente ---
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePreferences = view.findViewById(R.id.btnChangePreferences);
        Button btnTestNotification = view.findViewById(R.id.btnTestNotification);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // --- FUNCIONALIDAD AÑADIDA: Modo Oscuro ---
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        setupDarkModeSwitch();

        // --- Listeners existentes ---
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función de editar perfil no implementada.", Toast.LENGTH_SHORT).show();
        });

        btnChangePreferences.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            startActivity(intent);
        });

        btnTestNotification.setOnClickListener(v -> {
            sendTestNotification();
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences settings = getContext().getSharedPreferences(OnboardingActivity.PREFS_NAME, Context.MODE_PRIVATE);
            settings.edit().clear().apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Configura el estado inicial y el listener del interruptor.
    private void setupDarkModeSwitch() {
        SharedPreferences prefs = getContext().getSharedPreferences(OnboardingActivity.PREFS_NAME, Context.MODE_PRIVATE);
        // El valor por defecto será si el sistema operativo está en modo oscuro.
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, isSystemInDarkMode());
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            // Aplica el tema inmediatamente
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Comprueba si el sistema operativo está en modo oscuro.
    private boolean isSystemInDarkMode() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    // --- Método existente para la notificación (INTACTO) ---
    private void sendTestNotification() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) 
                .setContentTitle(getString(R.string.notification_title)) 
                .setContentText(getString(R.string.notification_content)) 
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) 
                .setContentIntent(pendingIntent) 
                .setAutoCancel(true); 

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permiso de notificaciones no concedido.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
