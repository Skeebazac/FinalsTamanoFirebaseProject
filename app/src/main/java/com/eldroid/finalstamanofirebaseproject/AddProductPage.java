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

import com.eldroid.finalstamanofirebaseproject.Model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddProductPage extends AppCompatActivity {
    private TextView backButtonTextView, quantityTextView;
    private ImageView productImageIcon;
    private TextInputEditText textInputEditText_PName, textInputEditText_PPrice, textInputEditText_PDescription;
    private Button addQtyButton, deductQtyButton, addProductButton, uploadProductImageButton, removeProductImageButton;
    private ActivityResultLauncher<Intent> imageResultLauncher;
    private int qtyResult;
    private Uri selectedImage;
    private String productID;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_page);
        initialized();
        backButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(AddProductPage.this, HomePage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
                        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "Title", null);
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
                    AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AddProductPage.this);
                    builder.setTitle("Are you sure you want to continue?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Yes")) {
                                progressDialog = new ProgressDialog(AddProductPage.this);
                                progressDialog.setMessage("Processing...");
                                progressDialog.show();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products");
                                productID = databaseReference.push().getKey();
                                databaseReference.child(productID).setValue(new Product(productID,String.valueOf(textInputEditText_PName.getText()),String.valueOf(quantityTextView.getText()),String.valueOf(textInputEditText_PPrice.getText()),"",String.valueOf(textInputEditText_PDescription.getText()))).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            if(selectedImage == null) {
                                                Toast.makeText(getApplicationContext(), "Successfully added the product!", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(AddProductPage.this, HomePage.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                finish();
                                                startActivity(intent);
                                            }
                                            else {
                                                uploadImage(productID);
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
                    Toast.makeText(AddProductPage.this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        uploadProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery", "Cancel" };
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AddProductPage.this);
                builder.setTitle("Choose your action to add a picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            requestCameraPermission();
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            if(takePicture.resolveActivity(getPackageManager()) != null) {
                                if (ContextCompat.checkSelfPermission(AddProductPage.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    imageResultLauncher.launch(takePicture);
                                } else {
                                    Toast.makeText(AddProductPage.this, "Please enable camera permissions", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(AddProductPage.this,"There's no app that supports this action", Toast.LENGTH_SHORT).show();
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
                Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_product_icon);
                productImageIcon.setImageURI(Uri.parse(""));
                productImageIcon.setImageBitmap(icon);
                selectedImage = Uri.parse("");
            }
        });
    }

    private void initialized() {
        //TextView
        backButtonTextView = findViewById(R.id.backButtonTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        //Edit Text
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
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_product_icon);
        productImageIcon.setImageURI(Uri.parse(""));
        productImageIcon.setImageBitmap(icon);
    }

    //Request Permissions Method
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(AddProductPage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddProductPage.this, new String[]{Manifest.permission.CAMERA}, 5);
        } else {

        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(AddProductPage.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddProductPage.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
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
                                            Toast.makeText(getApplicationContext(), "Successfully added the product!", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(AddProductPage.this, HomePage.class);
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
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AddProductPage.this);
        builder.setTitle("Are you sure you want to cancel?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Yes")) {
                    Intent intent = new Intent(AddProductPage.this, HomePage.class);
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
}