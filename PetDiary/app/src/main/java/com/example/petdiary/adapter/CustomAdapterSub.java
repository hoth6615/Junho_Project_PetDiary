package com.example.petdiary.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.petdiary.Data;
import com.example.petdiary.R;

import java.util.ArrayList;

public class CustomAdapterSub extends RecyclerView.Adapter<CustomAdapterSub.CustomViewHolder> {

    private ArrayList<Data> arrayList;
    private Context context;

    ViewPageAdapterSub viewPageAdapter;
    ViewPager viewPager;

    public CustomAdapterSub(ArrayList<Data> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    //실제 리스트뷰가 어댑터에 연결된 다음에 뷰 홀더를 최초로 만들어낸다.
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;

    }
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        viewPager = (ViewPager) holder.itemView.findViewById(R.id.main_image);
        viewPageAdapter = new ViewPageAdapterSub(arrayList.get(position),arrayList.get(position).getImageUrl1(), context);
        viewPager.setAdapter(viewPageAdapter);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;  // 핸드폰의 가로 해상도를 구함.
        // int deviceHeight = displayMetrics.heightPixels;  // 핸드폰의 세로 해상도를 구함.
        deviceWidth = (deviceWidth-60) / 3;
        holder.itemView.getLayoutParams().width = deviceWidth;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
        holder.itemView.getLayoutParams().height = deviceWidth;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
        holder.itemView.requestLayout(); // 변경 사항 적용

    }

    @Override
    public int getItemCount() {
        // 삼항 연산자
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}