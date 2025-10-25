package com.example.recomendaciones_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recomendaciones_app.data.model.Category;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.data.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "RecomendacionesPrefs";
    public static final String KEY_USER_CATEGORIES = "user_categories";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private static final String TAG = "OnboardingActivity";

    private ChipGroup chipGroupCategories;
    private ProgressBar progressBar;
    private ScrollView categoriesScrollView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        progressBar = findViewById(R.id.progressBarOnboarding);
        categoriesScrollView = findViewById(R.id.categoriesScrollView);
        Button btnSave = findViewById(R.id.btnSaveOnboarding);

        apiService = ApiClient.getClient().create(ApiService.class);

        loadCategories();

        btnSave.setOnClickListener(v -> savePreferences());
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoriesScrollView.setVisibility(View.GONE);

        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);
                categoriesScrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    chipGroupCategories.removeAllViews(); // Limpia los chips de la vista previa
                    for (Category category : response.body()) {
                        Chip chip = new Chip(OnboardingActivity.this);
                        // Anotación: CORRECCIÓN. Usamos el "name" para el texto y el "slug" como tag.
                        chip.setText(category.getName());
                        chip.setTag(category.getSlug());
                        chip.setCheckable(true);
                        chipGroupCategories.addView(chip);
                    }
                } else {
                    Toast.makeText(OnboardingActivity.this, "Error al cargar las categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OnboardingActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePreferences() {
        try {
            if (chipGroupCategories == null) {
                Log.w(TAG, "savePreferences: chipGroupCategories es null");
                Toast.makeText(this, "Error interno: no se pueden guardar las preferencias", Toast.LENGTH_SHORT).show();
                return;
            }

            Set<String> selectedCategories = new HashSet<>();
            for (int id : chipGroupCategories.getCheckedChipIds()) {
                Chip chip = chipGroupCategories.findViewById(id);
                if (chip != null && chip.getTag() != null) {
                    // Anotación: CORRECCIÓN. Guardamos el "slug" (tag) que es un identificador más robusto.
                    selectedCategories.add(chip.getTag().toString());
                }
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(OnboardingActivity.this, "Por favor, selecciona al menos una categoría", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet(KEY_USER_CATEGORIES, selectedCategories);
            editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
            editor.apply();

            Log.d(TAG, "savePreferences: preferences saved, launching MainActivity");

            Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show();

            // Instalamos un manejador global temporal para volcar excepciones en un archivo dentro del almacenamiento de la app
            final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                try {
                    java.io.File outFile = new java.io.File(getFilesDir(), "crash_log.txt");
                    java.io.FileWriter fw = new java.io.FileWriter(outFile, true);
                    java.io.PrintWriter pw = new java.io.PrintWriter(fw);
                    pw.println("--- Uncaught exception: " + new java.util.Date() + " ---");
                    throwable.printStackTrace(pw);
                    pw.println();
                    pw.close();
                    fw.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error escribiendo crash_log.txt", e);
                }

                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                } else {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(2);
                }
            });

            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar preferencias o iniciar MainActivity", e);
            Toast.makeText(this, "Error al guardar preferencias", Toast.LENGTH_LONG).show();
        }
    }
}
