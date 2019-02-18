package com.example.imageviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageViewPager imageViewPager = findViewById(R.id.image_pager);
        imageViewPager.setData(Meta.getImageUrls());
        imageViewPager.setRolling(true);
        imageViewPager.setOnImageChangeListener(new ImageViewPager.OnImageChangeListener() {
            @Override
            public void onChange(int position) {
                Log.d("position", "" + position);
            }
        });
    }
}
