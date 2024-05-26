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


import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter <ProductoAdapter.ViewHolder> {

    private List<Producto> productoList;
    private Context context;

    public ProductoAdapter(List<Producto> productoList, Context context) {
        this.productoList = productoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtNombre.setText(productoList.get(position).getNom());
        holder.txtPrecio.setText(productoList.get(position).getPrec());
        Glide.with(context)
                .load(productoList.get(position).getImg())
                .centerCrop()
                .into(holder.imgFoto);
    }

    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgFoto;
        private TextView txtNombre, txtPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imagenProducto);
            txtNombre = itemView.findViewById(R.id.nombreProducto);
            txtPrecio = itemView.findViewById(R.id.precioProducto);
        }
    }

}
