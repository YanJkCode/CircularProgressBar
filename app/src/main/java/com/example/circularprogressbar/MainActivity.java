package com.example.circularprogressbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.circularprogressbarview.CircularProgressBar;


public class MainActivity extends AppCompatActivity {

    private CircularProgressBar mCircularProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircularProgressBar = findViewById(R.id.progress_circular);
        findViewById(R.id.bottom01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircularProgressBar.play();
            }
        });

        findViewById(R.id.bottom02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircularProgressBar.complete();
            }
        });

        findViewById(R.id.bottom03).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircularProgressBar.stop();
            }
        });
        findViewById(R.id.bottom04).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircularProgressBar.error();
            }
        });
        mCircularProgressBar.setOnCompleteListener(new CircularProgressBar.OnLoadCompleteListener() {
            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "加载完成", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
