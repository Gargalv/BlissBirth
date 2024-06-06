package com.ama.blissbirth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private Button su;
    private TextInputLayout userName, userEmail, userPass, userPassRep;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.username);
        userEmail = findViewById(R.id.email);
        userPass = findViewById(R.id.password);
        userPassRep = findViewById(R.id.passwordconfirm);
        su = findViewById(R.id.register);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Configura el color del título, por ejemplo
            String titulo = "SIGN UP";
            actionBar.setTitle(Html.fromHtml("<font color=\"#FAEFF1\">"+titulo+"</font>"));
        }

        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = userName.getEditText().getText().toString().trim();
                String uEmail = userEmail.getEditText().getText().toString().trim();
                String uPass = userPass.getEditText().getText().toString().trim();
                String uPassRep = userPassRep.getEditText().getText().toString().trim();
                setError();
                if (uName.isEmpty() || uEmail.isEmpty() || uPass.isEmpty() || uPassRep.isEmpty()) {
                    if(uName.isEmpty())
                        userName.setError("Empty field");
                    if(uEmail.isEmpty())
                        userEmail.setError("Empty field");
                    if(uPass.isEmpty())
                        userPass.setError("Empty field");
                    if(uPassRep.isEmpty())
                        userPassRep.setError("Empty field");
                } else if (uPass.equals(uPassRep) && uPass.length() >= 6 && uPassRep.length() >= 6) {
                    registerUser(uName, uEmail, uPass);
                } else {
                    if (uPass.length() >= 6 && uPassRep.length() >= 6) {
                        userPassRep.setError("Review");
                        showToast("Passwords do not match");
                    } else {
                        userPass.setError("Min 6 characters");
                        userPassRep.setError("Min 6 characters");
                    }
                }
            }
        });

    }

    private void registerUser(String uName, String uEmail, String uPass) {
        firebaseAuth.createUserWithEmailAndPassword(uEmail, uPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String id = firebaseAuth.getCurrentUser().getUid();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("name", uName);
                map.put("email", uEmail);
                /*map.put("password", uPass);*/
                firestore.collection("user").document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    //Never enters onComplete function or onFailure
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast("User sucessfully registered");
                        toMainPage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast(e.getMessage());
            }
        });

    }

    private void toMainPage() {
        Intent toMain = new Intent(Register.this, Main.class);
        toMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMain);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setError() {
        userName.setErrorEnabled(false);
        userEmail.setErrorEnabled(false);
        userPass.setErrorEnabled(false);
        userPassRep.setErrorEnabled(false);
    }
}