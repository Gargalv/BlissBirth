package com.ama.blissbirth.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ama.blissbirth.Adapter.ProductAdapterFB;
import com.ama.blissbirth.Adapter.ProductFB;
import com.ama.blissbirth.AniadirCumpleanos;
import com.ama.blissbirth.CumpleanosDetalle;
import com.ama.blissbirth.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //
    private RecyclerView mRecyclerView;
    private ProductAdapterFB mProductAdapterFB;
    private FloatingActionButton aniadirProd;
    FirebaseFirestore firebaseFirestore;
    SearchView search_view;

    public Home_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Home_Fragment newInstance(String param1, String param2) {
        Home_Fragment fragment = new Home_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Creamos el rootView
        View rootView = inflater.inflate(R.layout.fragment_home_, container, false);
        // Instanciamos el Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        //Instanciamos nuestros elementos
        aniadirProd = rootView.findViewById(R.id.addProduct);
        SearchView searchView = rootView.findViewById(R.id.searchView);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        int textColor = ContextCompat.getColor(requireContext(), R.color.black);
        searchEditText.setTextColor(textColor);
        // Inicializa el RecyclerView
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Query query = firebaseFirestore.collection("bdaysHome");
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
                intent.putExtra("idCumple", clickedProduct.getName());
                // Puedes usar Intent para iniciar un nuevo Activity, pasando la información necesaria
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mProductAdapterFB);
        //Search view
        search_view = rootView.findViewById(R.id.searchView);
        search_view();
        //Accion para añadir producto
        aniadirProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addProducto = new Intent(requireActivity(), AniadirCumpleanos.class);
                startActivity(addProducto);
            }
        });
        return rootView;
    }

    private void search_view() {
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                textSearch(s);
                return false;
            }
        });
    }
    public void textSearch(String s){
        Query query = firebaseFirestore.collection("bdaysHome");
        FirestoreRecyclerOptions<ProductFB> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<ProductFB>()
                        .setQuery(query.orderBy("name")
                                .startAt(s), ProductFB.class).build();
        mProductAdapterFB = new ProductAdapterFB(firestoreRecyclerOptions);
        mProductAdapterFB.startListening();
        mProductAdapterFB.setOnItemClickListener(new ProductAdapterFB.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                // Obtén el modelo de producto correspondiente al documento
                ProductFB clickedProduct = documentSnapshot.toObject(ProductFB.class);
                // Implementa la lógica para abrir el nuevo Activity aquí
                Intent intent = new Intent(getContext(), CumpleanosDetalle.class);
                intent.putExtra("idCumple", clickedProduct.getName());
                // Puedes usar Intent para iniciar un nuevo Activity, pasando la información necesaria
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mProductAdapterFB);
    }

    //Revisar
    /*@Override
    public void onStart() {
        super.onStart();
        mProductAdapterFB.startListening();
    }*/

    @Override
    public void onPause() {
        super.onPause();
        mProductAdapterFB.stopListening();
    }
    //Revisar
    /*@Override
    public void onResume() {
        super.onResume();
        mProductAdapterFB.startListening();
    }*/

    @Override
    public void onStop() {
        super.onStop();
        mProductAdapterFB.stopListening();
    }

}