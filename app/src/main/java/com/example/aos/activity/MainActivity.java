package com.example.aos.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;


import com.example.net.activity.NetworkDiagnosisActivity;
import com.example.net.config.StartUpBean;
import com.example.netping.R;

public class MainActivity extends AppCompatActivity {

    private Button domain_error, default_title_bar, custom_title_bar, no_title_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        domain_error = findViewById(R.id.domain_error);
        default_title_bar = findViewById(R.id.default_title_bar);
        custom_title_bar = findViewById(R.id.custom_title_bar);
        no_title_bar = findViewById(R.id.no_title_bar);
        domain_error.setOnClickListener(v -> {
            NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(MainActivity.this, "");
        });
        default_title_bar.setOnClickListener(v -> {
            NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(MainActivity.this, "https://www.baidu.com");
        });
        custom_title_bar.setOnClickListener(v -> {
            StartUpBean bean = new StartUpBean(R.layout.title_bar_layout, R.id.iv_back);
            //bean.setNoTitleBar();
            NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(MainActivity.this, "https://www.baidu.com", bean);
        });
        no_title_bar.setOnClickListener(v -> {
            StartUpBean bean = new StartUpBean();
            bean.setNoTitleBar();
            NetworkDiagnosisActivity.Companion.startNetworkDiagnosisActivity(MainActivity.this, "https://www.baidu.com", bean);
        });
    }
}