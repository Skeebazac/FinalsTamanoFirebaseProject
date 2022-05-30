package com.eldroid.finalstamanofirebaseproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eldroid.finalstamanofirebaseproject.Model.Product;
import com.eldroid.finalstamanofirebaseproject.Model.ProductImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditProductPage extends AppCompatActivity {
    private TextView backButtonTextView, quantityTextView;
    private ImageView productImageIcon;
    private TextInputEditText textInputEditText_PName, textInputEditText_PPrice, textInputEditText_PDescription, textInputEditText_PProductID;
    private Button addQtyButton, deductQtyButton, addProductButton, uploadProductImageButton, removeProductImageButton;
    private ActivityResultLauncher<Intent> imageResultLauncher;
    private int qtyResult;
    private Uri selectedImage;
    private String selectedID;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product_page);
        selectedID = getIntent().getStringExtra("SELECTED_ID");
        initialized();
        retrieveDataFromThePage();
        backButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(EditProductPage.this, HomePage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });
        addQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtyResult = qtyResult + 1;
                quantityTextView.setText(String.valueOf(qtyResult));
            }
        });
        deductQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtyResult = qtyResult - 1;
                if(qtyResult < 0) {
                    qtyResult = 0;
                    quantityTextView.setText(String.valueOf(qtyResult));
                }
                else {
                    quantityTextView.setText(String.valueOf(qtyResult));
                }
            }
        });
        //Image Result Launcher
        imageResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent image = result.getData();
                    if (image.getExtras() != null) {
                        Bundle bundle = image.getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        productImageIcon.setImageBitmap(bitmap);
                        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, random(), null);
                        selectedImage = Uri.parse(path);
                    } else {
                        Uri imageData = image.getData();
                        productImageIcon.setImageURI(imageData);
                        selectedImage = imageData;
                    }
                }
            }
        });

        //Add Products to the Firebase
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!String.valueOf(textInputEditText_PName.getText()).equals("") && !String.valueOf(textInputEditText_PPrice.getText()).equals("") && !String.valueOf(textInputEditText_PDescription.getText()).equals("") && !String.valueOf(quantityTextView.getText()).equals("0")) {
                    final CharSequence[] options = { "Yes" ,"No" };
                    AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(EditProductPage.this);
                    builder.setTitle("Are you sure you want to continue?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Yes")) {
                                progressDialog = new ProgressDialog(EditProductPage.this);
                                progressDialog.setMessage("Processing...");
                                progressDialog.show();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products");
                                Map<String, Object> productHashMap = new HashMap<>();
                                productHashMap.put("product_name", String.valueOf(textInputEditText_PName.getText()));
                                productHashMap.put("product_quantity", String.valueOf(quantityTextView.getText()));
                                productHashMap.put("product_price", String.valueOf(textInputEditText_PPrice.getText()));
                                productHashMap.put("product_description", String.valueOf(textInputEditText_PDescription.getText()));
                                databaseReference.child(selectedID).updateChildren(productHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            if(selectedImage == null) {
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(EditProductPage.this, HomePage.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                finish();
                                                startActivity(intent);
                                            }
                                            else {
                                                uploadImage(selectedID);
                                            }
                                        }
                                    }
                                });
                            } else if (options[item].equals("No")) {
                                dialog.dismiss();
                            }
                        }
                    });
                    builder.show();
                }
                else {
                    Toast.makeText(EditProductPage.this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        uploadProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery", "Cancel" };
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(EditProductPage.this);
                builder.setTitle("Choose your action to add a picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            requestCameraPermission();
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            if(takePicture.resolveActivity(getPackageManager()) != null) {
                                if (ContextCompat.checkSelfPermission(EditProductPage.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    imageResultLauncher.launch(takePicture);
                                } else {
                                    Toast.makeText(EditProductPage.this, "Please enable camera permissions", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(EditProductPage.this,"There's no app that supports this action", Toast.LENGTH_SHORT).show();
                            }
                        } else if (options[item].equals("Choose from Gallery")) {
                            requestStoragePermission();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            imageResultLauncher.launch(pickPhoto);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
        removeProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productImageIcon.setImageResource(R.drawable.default_product_icon);
                Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_product_icon);
                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), myBitmap, "ProductImage", null);
                selectedImage = Uri.parse(path);
            }
        });
    }

    private void initialized() {
        //TextView
        backButtonTextView = findViewById(R.id.backButtonTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        //Edit Text
        textInputEditText_PProductID = findViewById(R.id.textInputEditText_PProductID);
        textInputEditText_PName = findViewById(R.id.textInputEditText_PName);
        textInputEditText_PPrice = findViewById(R.id.textInputEditText_PPrice);
        textInputEditText_PDescription = findViewById(R.id.textInputEditText_PDescription);
        //Buttons
        addQtyButton = findViewById(R.id.addQtyButton);
        deductQtyButton = findViewById(R.id.deductQtyButton);
        addProductButton = findViewById(R.id.addProductButton);
        uploadProductImageButton = findViewById(R.id.uploadProductImageButton);
        removeProductImageButton = findViewById(R.id.removeProductImageButton);
        //Int Values
        qtyResult = Integer.parseInt(String.valueOf(quantityTextView.getText()));
        //ImageView
        productImageIcon = findViewById(R.id.productImageIcon);
    }
    //Retrieve Data
    public void retrieveDataFromThePage() {
        FirebaseDatabase.getInstance().getReference("Products").child(selectedID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Product product = snapshot.getValue(Product.class);
                    //Edit Text
                    textInputEditText_PProductID.setText(product.getProduct_id());
                    textInputEditText_PName.setText(product.getProduct_name());
                    textInputEditText_PPrice.setText(product.getProduct_price());
                    textInputEditText_PDescription.setText(product.getProduct_description());
                    //Text Views
                    quantityTextView.setText(product.getProduct_quantity());
                    //Image Loader
                    if (product.getProduct_image().equals("")) {
                        Glide.with(getApplicationContext()).load(ProductImage.productImageDefault).diskCacheStrategy(DiskCacheStrategy.DATA).skipMemoryCache(false).into(productImageIcon);
                    } else {
                        Glide.with(getApplicationContext()).load(product.getProduct_image()).diskCacheStrategy(DiskCacheStrategy.DATA).skipMemoryCache(false).into(productImageIcon);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Request Permissions Method
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(EditProductPage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProductPage.this, new String[]{Manifest.permission.CAMERA}, 5);
        } else {

        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(EditProductPage.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProductPage.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
        } else {

        }
    }

    //Upload Image Method
    private void uploadImage(String productID)
    {
        if(selectedImage != null)
        {
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("ProductImage/"+ productID + "profile" +".jpg");
            ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()) {
                                Map<String, Object> userHashMap = new HashMap<>();
                                userHashMap.put("product_image", task.getResult().toString());
                                FirebaseDatabase.getInstance().getReference("Products").child(productID).updateChildren(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Successfully updated the product!", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(EditProductPage.this, HomePage.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            finish();
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        final CharSequence[] options = { "Yes" ,"No" };
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(EditProductPage.this);
        builder.setTitle("Are you sure you want to cancel?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Yes")) {
                    Intent intent = new Intent(EditProductPage.this, HomePage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                } else if (options[item].equals("No")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(8);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}