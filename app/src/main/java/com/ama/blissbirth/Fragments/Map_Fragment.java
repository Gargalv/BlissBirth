package com.ama.blissbirth.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ama.blissbirth.CumpleanosDetalle;
import com.ama.blissbirth.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Map_Fragment extends Fragment {

    private GoogleMap gMap;
    private LatLng location;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            if (location != null) {
                googleMap.addMarker(new MarkerOptions().position(location).title("Ubicación"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
            // Llamar a la función para agregar chinchetas desde Firestore
            agregarChinchetasDesdeFirestore();

            // Configurar el listener de clic en los marcadores
            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // Obtener el nombre del cumpleaños desde el tag del marcador
                    String nombreCumple = (String) marker.getTag();
                    if (nombreCumple != null) {
                        // Iniciar la actividad CumpleanosDetalle pasando el nombre del cumpleaños
                        Intent intent = new Intent(getContext(), CumpleanosDetalle.class);
                        intent.putExtra("idCumple", nombreCumple);
                        startActivity(intent);
                    }
                    return false;
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            location = getArguments().getParcelable("location");
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void agregarChinchetasDesdeFirestore() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("bdaysHome")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreCumple = document.getString("name");
                            GeoPoint geoPoint = document.getGeoPoint("location");
                            if (geoPoint != null && nombreCumple != null) {
                                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                Marker marker = gMap.addMarker(new MarkerOptions().position(latLng).title(nombreCumple));
                                marker.setTag(nombreCumple); // Establecer el nombre del cumpleaños como tag del marcador
                            }
                        }
                    } else {
                        // Manejar errores de consulta
                    }
                });
    }
}
