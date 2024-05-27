package com.ama.blissbirth.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ama.blissbirth.Edit_profile;
import com.ama.blissbirth.Login;
import com.ama.blissbirth.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile_Fragment extends Fragment {
    private Button lOut, edit;
    private ImageView profilePic;
    private TextView uName, uEmail;
    private RecyclerView mRecyclerView;
    //private ProductAdapterFB mProductAdapterFB;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    public Profile_Fragment() {
        // Required empty public constructor
    }

    public static Profile_Fragment newInstance() {
        return new Profile_Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        View rootView = inflater.inflate(R.layout.fragment_profile_, container, false);

        lOut = rootView.findViewById(R.id.logout);
        lOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        edit = rootView.findViewById(R.id.editData);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfile = new Intent(requireActivity(), Edit_profile.class);
                startActivity(editProfile);
            }
        });

        FirebaseUser user = firebaseAuth.getCurrentUser();
        uName = rootView.findViewById(R.id.profileName);
        String uid = user.getUid();
        DocumentReference userReference = firebaseFirestore.collection("user").document(uid);
        bucarUsuario(userReference);

        uEmail = rootView.findViewById(R.id.profileEmail);
        uEmail.setText(user.getEmail().toString());

        //inicializarRecyclerView(rootView, uid);

        profilePic = rootView.findViewById(R.id.profilePic);

        // Obtener la URL de la foto de perfil del usuario desde Firestore
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Obtener la URL de la foto de perfil del documento del usuario
                        String photoUrl = document.getString("fotoperfil");

                        // Cargar la foto de perfil con Glide
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(profilePic);
                        } else {
                            // Si no hay foto de perfil, muestra la imagen predeterminada
                            Glide.with(requireContext())
                                    .load(R.drawable.logoregister)
                                    .circleCrop()
                                    .into(profilePic);
                        }
                    } else {
                        // El documento del usuario no existe en Firestore
                        // Puedes manejar esto según tus necesidades
                    }
                } else {
                    // Manejar errores de lectura de Firestore si es necesario
                    Exception exception = task.getException();
                }
            }
        });

        return rootView;
    }

    private void showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Do you want to log out?");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                goToLogin();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToLogin() {
        Intent toLogin = new Intent(requireActivity(), Login.class);
        toLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toLogin);
        requireActivity().finish();
    }

    /*private void inicializarRecyclerView(View rootView, String uid) {
        mRecyclerView = rootView.findViewById(R.id.reciclerProfile);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Query query = firebaseFirestore.collection("products").whereEqualTo("userP", uid);
        FirestoreRecyclerOptions<ProductFB> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<ProductFB>().setQuery(query, ProductFB.class).build();
        mProductAdapterFB = new ProductAdapterFB(firestoreRecyclerOptions);
        mProductAdapterFB.notifyDataSetChanged();
        mProductAdapterFB.startListening();
        mProductAdapterFB.setOnItemClickListener(new ProductAdapterFB.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                // Obtén el modelo de producto correspondiente al documento
                ProductFB clickedProduct = documentSnapshot.toObject(ProductFB.class);
                // Implementa la lógica para abrir el nuevo Activity aquí
                Intent intent = new Intent(getContext(), CumpleanosDetalle.class);
                intent.putExtra("idProducto", clickedProduct.getName());
                // Puedes usar Intent para iniciar un nuevo Activity, pasando la información necesaria
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mProductAdapterFB);
    }*/

    private void bucarUsuario(DocumentReference userReference) {
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Obtener los datos del usuario
                        String username = document.getString("name");
                        uName.setText(username);
                    } else {
                        // El documento del usuario no existe en Firestore
                        // Puedes manejar esto según tus necesidades
                    }
                } else {
                    // Manejar errores de lectura de Firestore si es necesario
                    Exception exception = task.getException();
                }
            }
        });
    }

    //Revisar
    /*@Override
    public void onStart() {
        super.onStart();
        mProductAdapterFB.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        mProductAdapterFB.stopListening();
    }*/
    //Revisar
    /*@Override
    public void onResume() {
        super.onResume();
        mProductAdapterFB.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProductAdapterFB.stopListening();
    }*/
}