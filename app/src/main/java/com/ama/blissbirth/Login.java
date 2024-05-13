package com.ama.blissbirth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private Button reg, log;
    private TextInputLayout email, passwd;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.username);
        passwd = findViewById(R.id.password);
        reg = findViewById(R.id.register);
        log = findViewById(R.id.login);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegister = new Intent(Login.this, Register.class);
                startActivity(toRegister);
            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uEmail = email.getEditText().getText().toString().trim();
                String uPass = passwd.getEditText().getText().toString().trim();
                setError();
                if(uEmail.isEmpty() || uPass.isEmpty()) {
                    if (uEmail.isEmpty()){
                        passwd.setError(" ");
                    }
                    //cambio provisional
                    if (uPass.isEmpty()){
                        passwd.setError(" ");
                    }
                } else {
                    loginUser(uEmail, uPass);
                }
            }
        });

    }

    private void loginUser(String uEmail, String uPass) {
        firebaseAuth.signInWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    goToMain();
                } else {
                    //showToast("Email / Password incorrectas");
                    //email.setError("Revisar");
                    passwd.setError("Revisar");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast(e.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            goToMain();
        }
    }

    private void goToMain() {
        Intent toMain = new Intent(Login.this, Main.class);
        toMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMain);
    }

    private void setError() {
        email.setErrorEnabled(false);
        passwd.setErrorEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}