package com.ama.blissbirth.Adapter;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ama.blissbirth.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;


public class ProductAdapterFB extends FirestoreRecyclerAdapter<ProductFB, ProductAdapterFB.ViewHolder> implements View.OnClickListener {
    private OnItemClickListener onItemClickListener;
    private View.OnClickListener listener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ProductAdapterFB(@NonNull FirestoreRecyclerOptions<ProductFB> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductFB model) {
        holder.name.setText(model.getName());
        holder.desc.setText(model.getDesc());
        holder.price.setText(model.getPrice());
        Glide.with(holder.itemView.getContext())
                .load(model.getImgurl()) // Usa la URL de la imagen almacenada en el modelo
                .centerInside()
                .placeholder(R.drawable.logoregister)
                .into(holder.imgurl);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card_fb, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc, price;
        ImageView imgurl;
        private View.OnClickListener listener;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            desc = itemView.findViewById(R.id.productDesc);
            price = itemView.findViewById(R.id.productPrice);
            imgurl = itemView.findViewById(R.id.productImage);
            // Configurar el click en el listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                        onItemClickListener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}
