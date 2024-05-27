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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AniadirProducto extends AppCompatActivity {

    private Button aniadir, aniadirimagen;
    private TextInputEditText nom, desc, prec;
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

        nom = findViewById(R.id.nomCumpleanios);
        desc = findViewById(R.id.descProducto);
        prec = findViewById(R.id.precProducto);
        aniadir = findViewById(R.id.aniadir);
        aniadirimagen = findViewById(R.id.aniadirimagen);
        imageView = findViewById(R.id.imageViewPreview);

        aniadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nProd = nom.getText().toString().trim();
                String dProd = desc.getText().toString().trim();
                String pProd = prec.getText().toString().trim();

                if (nProd.isEmpty() || dProd.isEmpty() || pProd.isEmpty()) {
                    Toast.makeText(AniadirProducto.this, "Ingrese los datos", Toast.LENGTH_SHORT).show();
                } else {
                    if (dProd.length() > 400) {
                        Toast.makeText(AniadirProducto.this, "Descripción muy larga", Toast.LENGTH_SHORT).show();
                    } else {
                        postProd(nProd, dProd, pProd, uid);
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

    private void postProd(String nProd, String dProd, String pProd, String uid) {
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, nProd, dProd, pProd, uid);
        } else {
            finish();
        }
    }

    private void uploadImage(Uri imageUri, final String nProd, final String dProd, final String pProd, final String uid) {
        final StorageReference imageRef = mStorageReference.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveImageUrlToFirestore(nProd, dProd, pProd, uri.toString(),uid);
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

    private void saveImageUrlToFirestore(String nProd, String dProd, String pProd, String imageUrl, String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", nProd);
        map.put("desc", dProd);
        map.put("price", pProd);
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
            actionBar.setTitle(Html.fromHtml("<font color=\"#F2A71B\">"+titulo+"</font>"));
        }
    }
}