package com.oozapps.phoneroll.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.oozapps.phoneroll.R;

public class DeniedActivity extends Activity {
    TextView extraText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denied);
        extraText = findViewById(R.id.extraTextDenied);
        Intent intent = getIntent();
        String extraMessageString = intent.getStringExtra("extraTextInfo");
        extraText.setText(extraMessageString);


    }
}
