package com.eldroid.finalstamanofirebaseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.eldroid.finalstamanofirebaseproject.Adapters.ProductAdapter;
import com.eldroid.finalstamanofirebaseproject.Model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {
    private Button addProductButton;
    private TextView textViewUsername,logoutTextView;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private AutoCompleteTextView homeSearch;
    private List productList = new ArrayList<Product>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initialized();
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        textViewUsername.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(HomePage.this, AddProductPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });
        loadItems();

        homeSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                productList.clear();
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(v.getText().toString().equals("")) {
                        loadItems();
                    }
                    else {
                        searchProducts(v.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = { "Yes" ,"No" };
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HomePage.this);
                builder.setTitle("Are you sure you want to logout?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Yes")) {
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(new Intent(HomePage.this, LoginPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        } else if (options[item].equals("No")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void initialized() {
        addProductButton = findViewById(R.id.addProductButton);
        textViewUsername = findViewById(R.id.textViewUsername);
        productRecyclerView = findViewById(R.id.productRecyclerView);
        homeSearch = findViewById(R.id.homeSearch);
        logoutTextView = findViewById(R.id.logoutTextView);
    }

    @SuppressWarnings("unchecked")
    private void loadItems() {
        FirebaseDatabase.getInstance().getReference("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    productList.clear();
                    for(DataSnapshot dsp : snapshot.getChildren()) {
                        Product product = dsp.getValue(Product.class);
                        productList.add(product);
                    }
                    productAdapter = new ProductAdapter(HomePage.this, productList);
                    productAdapter.notifyDataSetChanged();
                    productRecyclerView.setAdapter(productAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressWarnings("unchecked")
    private void searchProducts(String keyword) {
        FirebaseDatabase.getInstance().getReference("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    productList.clear();
                    for(DataSnapshot dsp : snapshot.getChildren()) {
                        Product product = dsp.getValue(Product.class);
                        if(product.getProduct_name().toLowerCase().contains(keyword.toLowerCase()) || product.getProduct_description().toLowerCase().contains(keyword.toLowerCase())) {
                            productList.add(product);
                        }
                    }
                    productAdapter = new ProductAdapter(HomePage.this, productList);
                    productAdapter.notifyDataSetChanged();
                    productRecyclerView.setAdapter(productAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onBackPressed() {
        final CharSequence[] options = { "Yes" ,"No" };
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HomePage.this);
        builder.setTitle("Are you sure you want to logout?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Yes")) {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(HomePage.this, LoginPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                } else if (options[item].equals("No")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}