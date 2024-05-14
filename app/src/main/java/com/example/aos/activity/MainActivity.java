package com.example.aos.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;


import com.example.net.activity.NetworkDiagnosisActivity;
import com.example.netping.R;

public class MainActivity extends AppCompatActivity {

    private Button pingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pingBtn = findViewById(R.id.btn);
        pingBtn.setOnClickListener(v -> {
            NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(MainActivity.this);
        });
    }
}