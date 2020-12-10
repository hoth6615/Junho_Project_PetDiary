
package com.example.petdiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
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

public class ViewPageAdapter extends PagerAdapter {


    private ArrayList<String> images = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;
    private Data arrayList;

    public ViewPageAdapter(Data arrayList, String url1, String url2, String url3, String url4, String url5, Context context) {
        if (url1.length() > 0) {
            images.add(url1);
        }
        if (url2.length() > 0) {
            images.add(url2);
        }
        if (url3.length() > 0) {
            images.add(url3);
        }
        if (url4.length() > 0) {
            images.add(url4);
        }
        if (url5.length() > 0) {
            images.add(url5);
        }

        this.context = context;
        this.arrayList = arrayList;
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
    public Object instantiateItem(ViewGroup container, final int position) {
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider, container, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        Glide.with(context).load(images.get(position)).centerCrop().override(1000).into(imageView);

        v.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                goPost(position, arrayList);
            }
        });

        container.addView(v);
        return v;
    }

    private void goPost(int position, final Data arrayList) {
        final Intent intent = new Intent(context, Main_Expand_ImageView.class);

        intent.putExtra("currentItem", position);
        intent.putExtra("imageUrl1", arrayList.getImageUrl1());
        intent.putExtra("imageUrl2", arrayList.getImageUrl2());
        intent.putExtra("imageUrl3", arrayList.getImageUrl3());
        intent.putExtra("imageUrl4", arrayList.getImageUrl4());
        intent.putExtra("imageUrl5", arrayList.getImageUrl5());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }
}

