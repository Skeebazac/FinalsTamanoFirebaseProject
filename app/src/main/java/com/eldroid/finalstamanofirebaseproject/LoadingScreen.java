package com.eldroid.finalstamanofirebaseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class LoadingScreen extends AppCompatActivity {
    private Animation bottomAnimation;
    private TextView txtViewLandingPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        initialized();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home = new Intent(LoadingScreen.this, LandingPage.class);
                finish();
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(home);
            }
        }, 2500);
        txtViewLandingPage.setAnimation(bottomAnimation);
    }

    public void initialized(){
        //Hooks
        txtViewLandingPage = findViewById(R.id.txtViewLandingPage);
        bottomAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_animation);
    }
}