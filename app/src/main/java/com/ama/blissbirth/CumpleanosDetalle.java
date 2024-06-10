package com.ama.blissbirth;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CumpleanosDetalle extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String userIdOfProduct;
    private Button eliminarP, editarP;
    private String nombreC;
    private MapView mapView;
    private GeoPoint location;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cumpleanos_detalle);
        topBar();

        // Inicializa FirebaseFirestore
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Recupera la información del Intent
        Intent intent = getIntent();
        nombreC = intent.getStringExtra("idCumple");

        obtenerDatosDelProducto(nombreC);
        obtenerDatosUsuario(nombreC);

        TextView nCum = findViewById(R.id.nameGift);
        nCum.setText(nombreC);

        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Botón de contacto con el usuario
        Button contactUser = findViewById(R.id.contactUser);
        contactUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el nombre y el correo del creador del cum
                TextView userNameTextView = findViewById(R.id.userName);
                String nombreCreadorCum = userNameTextView.getText().toString();
                TextView userEmailTextView = findViewById(R.id.userMail);
                String correoCreadorCum = userEmailTextView.getText().toString();

                // Abrir el correo con el nombre del creador del cum ya escrito
                abrirCorreoConNombreCreadorcum(nombreCreadorCum, correoCreadorCum);
            }
        });

        // Botón de eliminar y modificar producto
        eliminarP = findViewById(R.id.deleteProduct);
        editarP = findViewById(R.id.editProduct);
    }

    private void obtenerDatosDelCumParaEditar(String nombreCum) {
        // Realiza una consulta para obtener el documento del cum
        firebaseFirestore.collection("bdaysHome")
                .whereEqualTo("name", nombreCum)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // El documento existe, ahora puedes obtener más datos
                            String descripcion = document.getString("desc");
                            String dateC = document.getString("date");
                            GeoPoint locationC = document.getGeoPoint("location");
                            String photo = document.getString("imgurl");
                            String cumUid = document.getId(); // Obtener el UID del cum

                            // Convertir GeoPoint a String
                            String locationString = locationC != null ? locationC.getLatitude() + "," + locationC.getLongitude() : "";

                            // Envía los datos a la clase Edit_cumpleanos
                            Intent editProduct = new Intent(CumpleanosDetalle.this, Edit_cumpleanos.class);
                            editProduct.putExtra("nombreCum", nombreCum);
                            editProduct.putExtra("descripcionCum", descripcion);
                            editProduct.putExtra("diaCum", dateC);
                            editProduct.putExtra("localizacionCum", locationString);
                            editProduct.putExtra("imgurlCum", photo);
                            editProduct.putExtra("cumUid", cumUid); // Agregar el UID del producto

                            // Agrega más extras si es necesario
                            startActivity(editProduct);
                        }
                    } else {
                        // Error al realizar la consulta
                        Log.e("ProductoDetalle", "Error getting product data to edit: " + task.getException());
                    }
                });
    }

    private void obtenerDatosDelProducto(String nombreCum) {
        firebaseFirestore.collection("bdaysHome")
                .whereEqualTo("name", nombreCum)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String descripcion = document.getString("desc");
                            String dateC = document.getString("date");
                            String photo = document.getString("imgurl");
                            location = document.getGeoPoint("location");

                            if (photo == null) {
                                photo = ""; // Asignar una cadena vacía si photo es nula
                            }

                            mostrarDatosEnLaInterfaz(descripcion, dateC, photo, location);
                        }
                    } else {
                        // Manejar el error
                    }
                });
    }



    private void obtenerDatosUsuario(String nombreProducto) {
        // Realiza una consulta para obtener el documento del producto
        firebaseFirestore.collection("bdaysHome")
                .whereEqualTo("name", nombreProducto)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // El documento existe, ahora puedes obtener más datos
                            String userId = document.getString("userP");
                            verificarPropietarioDelProducto(userId);
                            firebaseFirestore.collection("user")
                                    .whereEqualTo("id", userId)
                                    .get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            for (QueryDocumentSnapshot d : task1.getResult()) {
                                                // El documento existe, ahora puedes obtener más datos
                                                String userName = d.getString("name");
                                                String userMail = d.getString("email");
                                                // Ahora puedes usar la información como desees
                                                mostrarDatosEnLaInterfazUsuario(userName, userMail);
                                            }
                                        } else {
                                            // Error al realizar la segunda consulta
                                        }
                                    });
                        }
                    } else {
                        // Error al realizar la primera consulta
                    }
                });
    }

    private void mostrarDatosEnLaInterfazUsuario(String userName, String userMail) {
        TextView name = findViewById(R.id.userName);
        TextView email = findViewById(R.id.userMail);

        name.setText(userName);
        email.setText(String.valueOf(userMail));
    }

    private void mostrarDatosEnLaInterfaz(String descripcion, String dia, String photo, GeoPoint location) {
        TextView descripcionTextView = findViewById(R.id.descriptionGift);
        TextView diaTextView = findViewById(R.id.Dategift);
        ImageView photoImageView = findViewById(R.id.photoProduct);

        descripcionTextView.setText(descripcion);
        diaTextView.setText(dia);

        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .placeholder(R.drawable.logoregister) // Imagen de placeholder mientras se carga la imagen
                    .error(R.drawable.logoregister) // Imagen a mostrar si hay un error en la carga
                    .into(photoImageView);
        } else {
            // Manejar el caso en que photo es nula o está vacía
            photoImageView.setImageResource(R.drawable.logoregister); // Usar la imagen por defecto
        }
    }


    private void verificarPropietarioDelProducto(String userId) {
        // Obtén el UID del usuario actual
        String currentUserUid = firebaseAuth.getCurrentUser().getUid();

        // Tarjetas de Usuario y Producto
        CardView cardUsuario = findViewById(R.id.cardUsuario);
        CardView cardProducto = findViewById(R.id.cardProducto);

        // Verificar si el usuario actual es el propietario del producto
        if (userId.equals(currentUserUid)) {
            // El usuario actual es el propietario del producto, ocultar tarjeta de Usuario
            cardUsuario.setVisibility(View.GONE);
            // Mostrar tarjeta de Producto
            cardProducto.setVisibility(View.VISIBLE);
            eliminarP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarProducto(nombreC);
                }
            });
            editarP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("CumpleanosDetalle", "nombreProducto: " + nombreC);
                    obtenerDatosDelProductoParaEditar(nombreC);
                }
            });
        } else {
            // El usuario actual no es el propietario del producto, ocultar tarjeta de Producto
            cardProducto.setVisibility(View.GONE);
            // Mostrar tarjeta de Usuario
            cardUsuario.setVisibility(View.VISIBLE);
        }

    }

    private void obtenerDatosDelProductoParaEditar(String nombreC) {
        // Realiza una consulta para obtener el documento del producto
        firebaseFirestore.collection("bdaysHome")
                .whereEqualTo("name", nombreC)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // El documento existe, ahora puedes obtener más datos
                            String descripcion = document.getString("desc");
                            String dia = document.getString("date");
                            GeoPoint localizacion = document.getGeoPoint("location");
                            double latitud = localizacion.getLatitude();
                            double longitud = localizacion.getLongitude();
                            String localizacionString = latitud + ", " + longitud;

                            String photo = document.getString("imgurl");
                            String productoUid = document.getId(); // Obtener el UID del producto

                            // Envía los datos a la clase Edit_product
                            Intent editProduct = new Intent(CumpleanosDetalle.this, Edit_cumpleanos.class);
                            editProduct.putExtra("nombreCum", nombreC);
                            editProduct.putExtra("descripcionCum", descripcion);
                            editProduct.putExtra("diaCum", dia);
                            editProduct.putExtra("localizacionCum", localizacionString);
                            editProduct.putExtra("imgurlCum", photo);
                            editProduct.putExtra("cumUid", productoUid); // Agregar el UID del producto

                            // Agrega más extras si es necesario
                            startActivity(editProduct);
                        }
                    } else {
                        // Error al realizar la consulta
                        Log.e("CumpleanosDetalle", "Error getting birthday data to edit: " + task.getException());
                    }
                });
    }

    private void eliminarProducto(String nombreCum) {
        // Crear un AlertDialog para confirmar la eliminación
        new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage("Are you sure you want to delete this birthday?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Realiza una consulta para obtener el documento del producto
                    firebaseFirestore.collection("bdaysHome")
                            .whereEqualTo("name", nombreCum)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // El documento existe, ahora puedes eliminarlo
                                        document.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(CumpleanosDetalle.this, "Successfully deleted birthday", Toast.LENGTH_SHORT).show();
                                                    finish(); // Cierra la actividad actual
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error al eliminar el documento
                                                    Log.e("CumpleanosDetalle", "Error deleting birthday: " + e.getMessage());
                                                });
                                    }
                                } else {
                                    // Error al realizar la consulta
                                    Log.e("CumpleanosDetalle", "Error getting birthday data: " + task.getException());
                                }
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // El usuario ha cancelado la eliminación
                    dialog.dismiss();
                })
                .show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Birthday location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else {
            // Si no hay ubicación, puedes mostrar un mensaje o una ubicación por defecto
            LatLng defaultLocation = new LatLng(0, 0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    private void abrirCorreoConNombreCreadorcum(String nombreVendedor, String correoVendedor) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{correoVendedor});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Duda cumpleaños");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola "+ nombreVendedor + ", ");
        emailIntent.setType("message/rfc822");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo"));
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("No hay una app de correo instalada");
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void topBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(Html.fromHtml("<font color=\"#FAEFF1\">Birthday details</font>"));
        }
    }
}
