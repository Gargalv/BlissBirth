package com.ama.blissbirth;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ama.blissbirth.Fragments.Map_Fragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class CumpleanosDetalle extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String userIdOfProduct;
    private Button eliminarP, editarP;
    private String nombreC;
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
        //Boton para ir al maps
        Button maps = findViewById(R.id.goMaps);


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

        //Boton de eliminar y modificar producto
        eliminarP=findViewById(R.id.deleteProduct);
        editarP=findViewById(R.id.editProduct);
    }

    //Boton editar
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
                        Log.e("ProductoDetalle", "Error al obtener datos del producto para editar: " + task.getException());
                    }
                });
    }


    private void obtenerDatosDelProducto(String nombreCum) {
        // Realiza una consulta para obtener el documento del cumpleaños
        firebaseFirestore.collection("bdaysHome")
                .whereEqualTo("name", nombreCum)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // El documento existe, ahora puedes obtener más datos
                            String descripcion = document.getString("desc");
                            String dateC = document.getString("date");
                            String photo = document.getString("imgurl");
                            GeoPoint location = document.getGeoPoint("location");
                            String userProd = document.getString("userP");
                            // Ahora puedes usar la información como desees
                            mostrarDatosEnLaInterfaz(descripcion, dateC, photo, location);
                        }
                    } else {
                        // Error al realizar la consulta
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
                                        if (task1.isSuccessful()) { // Corregir aquí
                                            for (QueryDocumentSnapshot d : task1.getResult()) {
                                                // El documento existe, ahora puedes obtener más datos
                                                String userName = d.getString("name");
                                                String userMail = d.getString("email");
                                                // Ahora puedes usar la información como desees
                                                mostrarDatosEnLaInterfazUsuario(userName, userMail);
                                            }
                                        } else {
                                            // Error al realizar la segunda consulta
                                            // Maneja el error de alguna manera
                                        }
                                    });
                        }
                    } else {
                        // Error al realizar la primera consulta
                        // Maneja el error de alguna manera
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
        ImageView imagenImageView = findViewById(R.id.photoProduct);
        Button mapsButton = findViewById(R.id.goMaps);

        descripcionTextView.setText(descripcion);
        diaTextView.setText(dia);
        Glide.with(this).load(photo).centerCrop().into(imagenImageView);

        if (location != null) {
            String locationText = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
            mapsButton.setText(locationText);

            mapsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Map_Fragment mapFragment = new Map_Fragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("location", latLng);
                    mapFragment.setArguments(bundle);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mapFragmentContainer, mapFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        } else {
            mapsButton.setText("Ubicación no disponible");
            mapsButton.setOnClickListener(null);
        }
    }


    /*
    private void abrirMapa(GeoPoint location) {
        if (location != null) {
            // Convierte GeoPoint a latitud y longitud
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Crea una URI para mostrar la ubicación en un mapa
            String geoUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
            Uri gmmIntentUri = Uri.parse(geoUri);

            // Crea un intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verifica si hay una app que puede manejar el intent
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                showToast("No hay aplicaciones de mapas instaladas.");
            }
        } else {
            showToast("Ubicación no disponible.");
        }
    }
*/

    private void verificarPropietarioDelProducto(String userId) {
        // Obtén el UID del usuario actual
        String currentUserUid = firebaseAuth.getCurrentUser().getUid();

        // Tarjetas de Usuario y Producto
        CardView cardUsuario = findViewById(R.id.cardUsuario);
        CardView cardProducto = findViewById(R.id.cardProducto);
        Button add = findViewById(R.id.goMaps);

        // Verificar si el usuario actual es el propietario del producto
        if (userId.equals(currentUserUid)) {
            // El usuario actual es el propietario del producto, ocultar tarjeta de Usuario
            cardUsuario.setVisibility(View.GONE);
            // Mostrar tarjeta de Producto
            cardProducto.setVisibility(View.VISIBLE);
            eliminarP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoConfirmacion();
                }
            });
            editarP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ProductoDetalle", "nombreProducto: " + nombreC);
                    obtenerDatosDelCumParaEditar(nombreC);
                }
            });
        } else {
            // El usuario actual no es el propietario del producto, ocultar tarjeta de Producto
            cardProducto.setVisibility(View.GONE);
            // Mostrar tarjeta de Usuario
            cardUsuario.setVisibility(View.VISIBLE);
        }
    }



    private void abrirCorreoConNombreCreadorcum(String nombreVendedor, String correoVendedor) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{correoVendedor});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre cumpleaños");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola, "+ nombreVendedor + ", ");
        emailIntent.setType("message/rfc822");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo"));
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("No hay apps de correo instaladas");
        }
    }

    @Override
    public void onBackPressed() {
        // Agrega cualquier lógica adicional que desees al presionar el botón de retroceso.
        // Por ejemplo, puedes realizar alguna acción específica antes de cerrar la actividad.

        // Elimina la llamada a super.onBackPressed() para evitar cerrar la actividad.
        super.onBackPressed();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void topBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String titulo = "Detalles del cumpleaños";
            actionBar.setTitle(Html.fromHtml("<font color=\"#6DD7FF\">"+titulo+"</font>"));
        }
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Borrar cumpleaños");
        builder.setMessage("¿Estas seguro que quieres eliminar el cumpleaños?");

        // Agregar botón de confirmación
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para eliminar el producto
                // Obtén el UID del usuario actual
                String currentUserUid = firebaseAuth.getCurrentUser().getUid();

                // Realiza una consulta para obtener el documento del producto
                firebaseFirestore.collection("bdaysHome")
                        .whereEqualTo("name", nombreC)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // El documento existe, ahora puedes eliminarlo
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Redirigir o realizar alguna acción adicional después de eliminar
                                                Intent intent = new Intent(CumpleanosDetalle.this, Main.class);
                                                startActivity(intent);
                                                finish();  // Cierra la actividad actual
                                            });
                                }
                            } else {
                                // Error al realizar la consulta
                                //showToast("Error al obtener datos del producto: " + task.getException().getMessage());
                            }
                        });
            }
        });

        // Agregar botón de cancelación
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo sin hacer nada
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        builder.create().show();
    }
}