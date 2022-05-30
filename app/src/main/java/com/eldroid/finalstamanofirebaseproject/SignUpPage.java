package com.eldroid.finalstamanofirebaseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eldroid.finalstamanofirebaseproject.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpPage extends AppCompatActivity {
    private TextView alreadyHaveAnAccountTextView, backButtonTextView, signUpButton;
    private TextInputEditText textInputEditText_Email, textInputEditText_Password, textInputEditText_ConfirmPassword;
    private String userID, email, passcode, confirmPasscode;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        initialized();
        alreadyHaveAnAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(SignUpPage.this, LoginPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });
        backButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(SignUpPage.this, LandingPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = String.valueOf(textInputEditText_Email.getText());
                passcode = String.valueOf(textInputEditText_Password.getText());
                confirmPasscode = String.valueOf(textInputEditText_ConfirmPassword.getText());
                progressDialog = new ProgressDialog(SignUpPage.this);
                progressDialog.setMessage("Signing Up...");
                progressDialog.show();
                if(!email.equals("") && !passcode.equals("") && !confirmPasscode.equals("")) {
                    if (passcode.length() < 6) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Your Password must contain 6 Characters", Toast.LENGTH_SHORT).show();
                    }
                    else if (!passcode.equals(confirmPasscode)) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Please confirm your password!", Toast.LENGTH_SHORT).show();
                    } else {
                        if(isNetworkConnected()) {
                            registerUserAccount(email, passcode);
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Please complete all fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initialized() {
        alreadyHaveAnAccountTextView = findViewById(R.id.alreadyHaveAnAccountTextView);
        backButtonTextView = findViewById(R.id.backButtonTextView);
        signUpButton = findViewById(R.id.signUpButton);

        //Edit Text
        textInputEditText_Email = findViewById(R.id.textInputEditText_Email);
        textInputEditText_Password = findViewById(R.id.textInputEditText_Password);
        textInputEditText_ConfirmPassword = findViewById(R.id.textInputEditText_ConfirmPassword);
    }

    public void registerUserAccount(String email, String passcode) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,passcode).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("Users").child(userID).setValue(new User(userID,"","","",FirebaseAuth.getInstance().getCurrentUser().getEmail(),"",true)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(SignUpPage.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(SignUpPage.this, LandingPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpPage.this, "Storing in RDB Failed for " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(SignUpPage.this, LandingPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    });
                } else {
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthUserCollisionException existEmail) {
                        progressDialog.dismiss();
                        Toast.makeText(SignUpPage.this, "Email Exist!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Log.d("Signup_Page", e.getMessage());
                    }
                }
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(new Intent(SignUpPage.this, LoginPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
}