package com.ama.blissbirth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AniadirProducto extends AppCompatActivity {

    private Button aniadir, aniadirimagen;
    private TextInputEditText nom, desc, dia, loc;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private static final int PICK_IMAGE = 100;
    private ImageView imageView;
    private Uri selectedImageUri;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniadir_producto);
        topBar();

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String uid = user.getUid();

        nom = findViewById(R.id.nomCumpleanos);
        desc = findViewById(R.id.descCumpleanos);
        dia = findViewById(R.id.diaCumpleanos);
        loc = findViewById(R.id.localizacionCumpleanos);
        aniadir = findViewById(R.id.aniadir);
        aniadirimagen = findViewById(R.id.aniadirimagen);
        imageView = findViewById(R.id.imageViewPreview);

        aniadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nCum = nom.getText().toString().trim();
                String dCum = desc.getText().toString().trim();
                String diaCumStr = dia.getText().toString().trim();
                String locCumStr = loc.getText().toString().trim();

                if (nCum.isEmpty() || dCum.isEmpty() || diaCumStr.isEmpty() || locCumStr.isEmpty()) {
                    Toast.makeText(AniadirProducto.this, "Ingrese los datos", Toast.LENGTH_SHORT).show();
                } else {
                    if (dCum.length() > 400) {
                        Toast.makeText(AniadirProducto.this, "Descripción muy larga", Toast.LENGTH_SHORT).show();
                    } else {
                        Timestamp diaCum = null;
                        GeoPoint lCum = null;

                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                            Date parsedDate = dateFormat.parse(diaCumStr);
                            if (parsedDate != null) {
                                diaCum = new Timestamp(parsedDate);
                            }
                        } catch (ParseException e) {
                            Toast.makeText(AniadirProducto.this, "Formato de fecha y hora incorrecto", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] locParts = locCumStr.split(",");
                        if (locParts.length == 2) {
                            try {
                                double lat = Double.parseDouble(locParts[0].trim());
                                double lon = Double.parseDouble(locParts[1].trim());
                                lCum = new GeoPoint(lat, lon);
                            } catch (NumberFormatException e) {
                                Toast.makeText(AniadirProducto.this, "Formato de localización incorrecto", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(AniadirProducto.this, "Formato de localización incorrecto", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        postProd(nCum, dCum, diaCum, lCum, uid);
                    }
                }
            }
        });

        aniadirimagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
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
            imageView.setImageURI(selectedImageUri);
        }
    }

    private void postProd(String nCum, String dCum, Timestamp dtCum, GeoPoint lCum, String uid) {
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, nCum, dCum, dtCum, lCum, uid);
        } else {
            saveImageUrlToFirestore(nCum, dCum, dtCum, lCum, null, uid);
        }
    }

    private void uploadImage(Uri imageUri, final String nCum, final String dCum, final Timestamp dtCum, final GeoPoint lCum, final String uid) {
        final StorageReference imageRef = mStorageReference.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveImageUrlToFirestore(nCum, dCum, dtCum, lCum, uri.toString(), uid);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AniadirProducto.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageUrlToFirestore(String nCum, String dCum, Timestamp dtCum, GeoPoint lCum, String imageUrl, String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", nCum);
        map.put("desc", dCum);
        map.put("date", dtCum);
        map.put("location", lCum);
        map.put("imgurl", imageUrl);
        map.put("userP", uid);

        mFirestore.collection("products").add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AniadirProducto.this, "Producto subido con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AniadirProducto.this, "Error al ingresar el producto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void topBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String titulo = getResources().getString(R.string.addProduct);
            actionBar.setTitle(Html.fromHtml("<font color=\"#FFFC97\">" + titulo + "</font>"));
        }
    }
}
