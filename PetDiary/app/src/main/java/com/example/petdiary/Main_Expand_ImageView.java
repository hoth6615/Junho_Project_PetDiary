package com.example.petdiary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class Main_Expand_ImageView extends AppCompatActivity {

    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private int currentItem;

    ViewPageAdapterDetail viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_expandimage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentItem = intent.getIntExtra("currentItem", 0);
        imageUrl1 = intent.getStringExtra("imageUrl1");
        imageUrl2 = intent.getStringExtra("imageUrl2");
        imageUrl3 = intent.getStringExtra("imageUrl3");
        imageUrl4 = intent.getStringExtra("imageUrl4");
        imageUrl5 = intent.getStringExtra("imageUrl5");

        viewPager = (ViewPager) findViewById(R.id.main_image);
        viewPageAdapter = new ViewPageAdapterDetail(false, imageUrl1, imageUrl2, imageUrl3, imageUrl4, imageUrl5, getApplicationContext());
        viewPager.setAdapter(viewPageAdapter);

        wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);
        wormDotsIndicator.setViewPager(viewPager);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(currentItem != 0){
                    viewPager.setCurrentItem(currentItem);
                }
            }
        }, 20);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

