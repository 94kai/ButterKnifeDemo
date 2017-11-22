package com.xk.butterknifedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.xk.annotation_lib.BindView;

public class MainActivity1 extends AppCompatActivity {
    @BindView(R.id.withText)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }
}


