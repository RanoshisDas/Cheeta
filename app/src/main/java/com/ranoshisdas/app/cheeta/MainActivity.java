package com.ranoshisdas.app.cheeta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.ranoshisdas.app.cheeta.auth.LoginActivity;
import com.ranoshisdas.app.cheeta.dashboard.DashboardActivity;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            if (FirebaseUtil.auth().getCurrentUser() != null) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}