package com.ama.blissbirth.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ama.blissbirth.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


import java.util.List;

public class ProductoAdapter extends FirestoreRecyclerAdapter<ProductFB, ProductoAdapter.ViewHolder> {

    public ProductoAdapter(@NonNull FirestoreRecyclerOptions<ProductFB> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductFB model) {
        holder.name.setText(model.getName());
        holder.desc.setText(model.getDesc());
        holder.date.setText(model.getDate());
        Glide.with(holder.itemView.getContext())
                .load(model.getImgurl()) // Usa la URL de la imagen almacenada en el modelo
                .centerInside()
                .placeholder(R.drawable.logoregister)
                .into(holder.imgurl);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.producto_card, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc, date;
        ImageView imgurl;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nombreProducto);
            desc = itemView.findViewById(R.id.precioProducto);
            imgurl = itemView.findViewById(R.id.imagenProducto);
        }
    }
}
