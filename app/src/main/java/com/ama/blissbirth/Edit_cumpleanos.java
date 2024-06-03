package com.ama.blissbirth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class Edit_cumpleanos extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private StorageReference storageReference;
    private TextInputEditText nom, desc, dia, loc;
    private ImageView imageView;
    private Button editCum, editImagen;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String nombreCum;
    private String cumUid;
    private Uri selectedImageUri;
    private String imgurlCum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniadir_producto);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        nombreCum = intent.getStringExtra("nombreCum");
        String descripcionCum = intent.getStringExtra("descripcionCum");
        String diaCum = intent.getStringExtra("diaCum");
        String localizacionCum = intent.getStringExtra("localizacionCum");
        imgurlCum = intent.getStringExtra("imgurlCum");
        cumUid = intent.getStringExtra("cumUid");

        nom = findViewById(R.id.nomCumpleanos);
        desc = findViewById(R.id.descCumpleanos);
        dia = findViewById(R.id.diaCumpleanos);
        loc = findViewById(R.id.localizacionCumpleanos);
        imageView = findViewById(R.id.imageViewPreview);
        editCum = findViewById(R.id.aniadir);
        editCum.setText("Editar");
        editImagen = findViewById(R.id.aniadirimagen);

        editImagen.setOnClickListener(v -> openGallery());
        editCum.setOnClickListener(v -> editProductButtonClick());

        nom.setText(nombreCum);
        desc.setText(descripcionCum);
        dia.setText(diaCum);
        loc.setText(localizacionCum);

        Glide.with(this).load(imgurlCum).centerCrop().into(imageView);

        topBar();
    }

    private void editProductButtonClick() {
        String nuevoNombre = String.valueOf(nom.getText());
        String nuevaDescripcion = String.valueOf(desc.getText());
        String nuevoPrecio = String.valueOf(dia.getText());
        String nuevaLocalizacionString = String.valueOf(loc.getText());

        GeoPoint nuevaLocaliacion = null;
        String[] parts = nuevaLocalizacionString.split(",");
        double lat = Double.parseDouble(parts[0]);
        double lng = Double.parseDouble(parts[1]);
        nuevaLocaliacion = new GeoPoint(lat, lng);

        if (selectedImageUri != null) {
            uploadImageAndUpdateProduct(cumUid, nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevaLocaliacion);
        } else {
            actualizarProductoEnFirestore(cumUid, nuevoNombre, nuevaDescripcion, nuevoPrecio, nuevaLocaliacion, imgurlCum);
        }
    }

    private void uploadImageAndUpdateProduct(String productoUid, String nuevoNombre, String nuevaDescripcion, String nuevoDia, GeoPoint nuevaLocalizacion) {
        final StorageReference imageRef = storageReference.child("images/" + productoUid + ".jpg");

        UploadTask uploadTask = imageRef.putFile(selectedImageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                actualizarProductoEnFirestore(productoUid, nuevoNombre, nuevaDescripcion, nuevoDia, nuevaLocalizacion, downloadUri.toString());
            }).addOnFailureListener(e -> showToast("Error al obtener la URL de la imagen: " + e.getMessage()));
        }).addOnFailureListener(e -> showToast("Error al subir la imagen: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void actualizarProductoEnFirestore(String productoUid, String nuevoNombre, String nuevaDescripcion, String nuevoDia, GeoPoint nuevaLocalizacion, String nuevaImageUrl) {
        DocumentReference productoRef = firebaseFirestore.collection("bdaysHome").document(productoUid);

        Map<String, Object> nuevosDatos = new HashMap<>();
        //nuevosDatos.put("name", nuevoNombre);
        nuevosDatos.put("desc", nuevaDescripcion);
        nuevosDatos.put("date", nuevoDia);
        nuevosDatos.put("location", nuevaLocalizacion);
        nuevosDatos.put("imgurl", nuevaImageUrl);


        productoRef.update(nuevosDatos)
                .addOnSuccessListener(aVoid -> {
                    showToast("Cambios guardados correctamente");
                    openMainActivity();
                })
                .addOnFailureListener(e -> showToast("Error al guardar cambios: " + e.getMessage()));
    }

    private void openMainActivity() {
        Intent mainIntent = new Intent(Edit_cumpleanos.this, Main.class);
        startActivity(mainIntent);
        finishAffinity();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).centerCrop().into(imageView);
        }
    }

    private void topBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(Html.fromHtml("<font color=\"#FFFC97\">Editar Cumpleaños</font>"));
        }
    }
}
