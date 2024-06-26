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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AniadirCumpleanos extends AppCompatActivity {

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
        setContentView(R.layout.activity_aniadir_cumpleanos);
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
                    Toast.makeText(AniadirCumpleanos.this, "Enter the data", Toast.LENGTH_SHORT).show();
                } else {
                    if (dCum.length() > 400) {
                        Toast.makeText(AniadirCumpleanos.this, "Very long description", Toast.LENGTH_SHORT).show();
                    } else if (!isValidDate(diaCumStr)) {
                        Toast.makeText(AniadirCumpleanos.this, "Incorrect date format. Use dd/mm/yyyy", Toast.LENGTH_SHORT).show();
                    } else {
                        // Conversión de fecha
                        String diaCum = diaCumStr;

                        // Conversión de localización
                        GeoPoint lCum = null;
                        String[] locParts = locCumStr.split(",");
                        if (locParts.length == 2) {
                            try {
                                double lat = Double.parseDouble(locParts[0].trim());
                                double lon = Double.parseDouble(locParts[1].trim());
                                lCum = new GeoPoint(lat, lon);
                            } catch (NumberFormatException e) {
                                Toast.makeText(AniadirCumpleanos.this, "Incorrect localization format", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AniadirCumpleanos.this, "Incorrect localization format", Toast.LENGTH_SHORT).show();
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

    private boolean isValidDate(String date) {
        String regex = "^\\d{2}/\\d{2}/\\d{4}$";
        return date.matches(regex);
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

    private void postProd(String nCum, String dCum, String dtCum, GeoPoint lCum, String uid) {
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, nCum, dCum, dtCum, lCum, uid);
        } else {
            saveImageUrlToFirestore(nCum, dCum, dtCum, lCum, null, uid);
        }
    }

    private void uploadImage(Uri imageUri, final String nCum, final String dCum, final String dtCum, final GeoPoint lCum, final String uid) {
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
                        Toast.makeText(AniadirCumpleanos.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageUrlToFirestore(String nCum, String dCum, String dtCum, GeoPoint lCum, String imageUrl, String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", nCum);
        map.put("desc", dCum);
        map.put("date", dtCum); // date es un String
        map.put("location", lCum);
        map.put("imgurl", imageUrl);
        map.put("userP", uid);

        mFirestore.collection("bdaysHome").add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AniadirCumpleanos.this, "Birthday uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AniadirCumpleanos.this, "Error entering birthday", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void topBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String titulo = "Add birthday";
            actionBar.setTitle(Html.fromHtml("<font color=\"#FAEFF1\">" + titulo + "</font>"));
        }
    }
}
