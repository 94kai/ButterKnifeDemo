package com.xk.butterknifedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.xk.annotation_lib.BindView;
import com.xk.butterknifelib.InjectUtil;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.text)
    public TextView textView;
    @BindView(R.id.text)
    TextView textView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectUtil.bind(this);

        Toast.makeText(this, "xxx" + textView, Toast.LENGTH_SHORT).show();
    }
}


