package com.example.recomendaciones_app.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.recomendaciones_app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class BuscarFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Anotación: FUNCIONALIDAD AÑADIDA. Launcher moderno para solicitar permisos.
    private final ActivityResultLauncher<String> requestPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Si el permiso se concede, intentamos activar la capa de ubicación.
                enableMyLocation();
            } else {
                // Si se deniega, informamos al usuario.
                Toast.makeText(getContext(), "Permiso de ubicación denegado. No se puede mostrar la ubicación.", Toast.LENGTH_LONG).show();
            }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Anotación: Obtener el SupportMapFragment y notificar cuando el mapa esté listo.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Anotación: Este método se llama cuando el mapa está listo para ser usado.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Anotación: Mover la cámara a una ubicación por defecto (p.ej., Ciudad de México) con un zoom inicial.
        LatLng mexicoCity = new LatLng(19.4326, -99.1332);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mexicoCity, 10));

        // Anotación: Una vez que el mapa está listo, comprobamos los permisos de ubicación.
        enableMyLocation();
    }

    /**
     * Anotación: Comprueba los permisos y activa la capa de ubicación si es posible.
     */
    private void enableMyLocation() {
        // Comprueba si el mapa está inicializado.
        if (mMap == null) {
            return;
        }

        // Comprueba si el permiso ACCESS_FINE_LOCATION ya está concedido.
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Si tenemos permiso, activamos la capa de "Mi Ubicación" en el mapa.
            mMap.setMyLocationEnabled(true);
        } else {
            // Si no tenemos permiso, lo solicitamos.
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
}
