package com.example.petdiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.petdiary.Data;
import com.example.petdiary.Main_Expand_ImageView;
import com.example.petdiary.OnSingleClickListener;
import com.example.petdiary.R;

import java.util.ArrayList;

public class ViewPageAdapterDetail extends PagerAdapter {

    private ArrayList<String> images = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;
    private String url1;
    private String url2;
    private String url3;
    private String url4;
    private String url5;
    boolean check;

    public ViewPageAdapterDetail(boolean check, String url1, String url2, String url3, String url4, String url5, Context context){
        if(url1.length() > 0 ){
            images.add(url1);
        }
        if(url2.length() > 0 ){
            images.add(url2);
        }
        if(url3.length() > 0 ){
            images.add(url3);
        }
        if(url4.length() > 0 ){
            images.add(url4);
        }
        if(url5.length() > 0 ){
            images.add(url5);
        }

        this.context = context;
        this.url1 = url1;
        this.url2 = url2;
        this.url3 = url3;
        this.url4 = url4;
        this.url5 = url5;
        this.check = check;

    }

    @Override
    public int getCount() {
        //return images.length;
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.imageView);
        if(check){
            Glide.with(context).load(images.get(position)).centerCrop().override(1000).into(imageView);
            v.setOnClickListener(new OnSingleClickListener(){
                public void onSingleClick(View v){
                    Log.e("###", position+"");
                    goPost(position, url1,url2,url3,url4,url5);
                }
            });
        } else {
            Glide.with(context).load(images.get(position)).into(imageView);
        }

        container.addView(v);
        return v;
    }

    private void goPost(int currentItem, String url1, String url2, String url3, String url4, String url5){
        final Intent intent = new Intent(context, Main_Expand_ImageView.class);

        intent.putExtra("currentItem", currentItem);
        intent.putExtra("imageUrl1", url1);
        intent.putExtra("imageUrl2", url2);
        intent.putExtra("imageUrl3", url3);
        intent.putExtra("imageUrl4", url4);
        intent.putExtra("imageUrl5", url5);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }

}


