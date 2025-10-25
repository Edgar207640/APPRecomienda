package com.example.recomendaciones_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Anotación: Encontrar los botones en el layout por su ID.
        Button btnInvitado = findViewById(R.id.btnInvitado);
        Button btnGoogle = findViewById(R.id.btnGoogle);

        // Anotación: Configurar el listener para el botón de invitado.
        // Al hacer clic, se inicia la OnboardingActivity.
        btnInvitado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simula el flujo de invitado, llevando a la siguiente pantalla.
                Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish(); // Cierra LoginActivity para que el usuario no pueda volver atrás.
            }
        });

        // Anotación: Configurar el listener para el botón de Google.
        // Por ahora, simula el mismo flujo que el de invitado.
        // La implementación real de Google Sign-In es más compleja y se omite.
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simula el flujo de Google Sign-In.
                Toast.makeText(LoginActivity.this, "Función de Google Sign-In no implementada. Continuando como invitado.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish(); // Cierra LoginActivity.
            }
        });
    }
}
