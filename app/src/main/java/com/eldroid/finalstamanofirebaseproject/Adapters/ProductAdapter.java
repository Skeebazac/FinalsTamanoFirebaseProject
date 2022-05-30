package com.eldroid.finalstamanofirebaseproject.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eldroid.finalstamanofirebaseproject.EditProductPage;
import com.eldroid.finalstamanofirebaseproject.Model.Product;
import com.eldroid.finalstamanofirebaseproject.Model.ProductImage;
import com.eldroid.finalstamanofirebaseproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;


import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> productList;
    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;

    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_product_item, parent, false);
        return new ProductAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        Product product = productList.get(position);
        if(product.getProduct_name().length() > 14) {
            holder.productItemName.setText(product.getProduct_name().substring(0,14));
        }
        else {
            holder.productItemName.setText(product.getProduct_name());
        }
        holder.productItemStock.setText("Stock: " + product.getProduct_quantity());
        if(product.getProduct_price().length() > 15) {
            holder.productItemName.setTextSize(10);
            holder.productItemPrice.setText("Price: ₱" + product.getProduct_price());
        }
        else {
            holder.productItemPrice.setText("Price: ₱" + product.getProduct_price());
        }
        //Image View
        if (product.getProduct_image().equals("")) {
            Glide.with(context).load(ProductImage.productImageDefault).diskCacheStrategy(DiskCacheStrategy.DATA).skipMemoryCache(false).into(holder.productImage);
        } else {
            Glide.with(context).load(product.getProduct_image()).diskCacheStrategy(DiskCacheStrategy.DATA).skipMemoryCache(false).into(holder.productImage);
        }
        //Buttons
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity)context).finish();
                Intent intent = new Intent(context, EditProductPage.class);
                intent.putExtra("SELECTED_ID", product.getProduct_id());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = {"Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure do you want to remove this product?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Yes")) {
                            FirebaseDatabase.getInstance().getReference("Products").child(product.getProduct_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(context.getApplicationContext(), "Successfully remove the product!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else if (options[item].equals("No")) {
                            Toast.makeText(context, "Cancelled!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView productItemName,productItemStock,productItemPrice;
        ImageView productImage;
        Button editButton,deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);
            //TextViews
            productItemName = itemView.findViewById(R.id.productItemName);
            productItemStock = itemView.findViewById(R.id.productItemStock);
            productItemPrice = itemView.findViewById(R.id.productItemPrice);
            //ImageView
            productImage = itemView.findViewById(R.id.productImage);
            //Buttons
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        }
    }
}