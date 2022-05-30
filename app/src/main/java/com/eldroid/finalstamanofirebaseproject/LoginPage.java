package com.eldroid.finalstamanofirebaseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPage extends AppCompatActivity {
    private TextView backButtonTextView;
    private TextInputEditText textInputEditText_Email, textInputEditText_Password;
    private Button loginButton;
    private String email, passcode;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        initialized();
        backButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(LoginPage.this, LandingPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = String.valueOf(textInputEditText_Email.getText());
                passcode = String.valueOf(textInputEditText_Password.getText());
                progressDialog = new ProgressDialog(LoginPage.this);
                progressDialog.setMessage("Logging in...");
                progressDialog.show();
                if (!(email.equals("")) && !(passcode.equals(""))) {
                    if (isNetworkConnected()) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, passcode).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(new Intent(LoginPage.this, HomePage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                }
                                else {
                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginPage.this, "Email or Password is Incorrect! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Please complete all fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initialized(){
        backButtonTextView = findViewById(R.id.backButtonTextView);
        loginButton = findViewById(R.id.loginButton);
        textInputEditText_Email = findViewById(R.id.textInputEditText_Email);
        textInputEditText_Password = findViewById(R.id.textInputEditText_Password);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(new Intent(LoginPage.this, LandingPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
}