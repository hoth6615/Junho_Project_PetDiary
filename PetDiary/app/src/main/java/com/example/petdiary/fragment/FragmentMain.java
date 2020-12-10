package com.example.petdiary.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.petdiary.adapter.CustomAdapter;
import com.example.petdiary.Data;
import com.example.petdiary.R;
import com.example.petdiary.info.FriendInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentMain extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Data> arrayList;
    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    BottomNavigationView bottomNavigationView;

    private ArrayList<String> bookmark = new ArrayList<String>();
    private ArrayList<String> like = new ArrayList<String>();

    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);

        moveTop();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // User 객체를 담을 어레이 리스트 (어댑터쪽으로)

        setInfo();

        adapter = new CustomAdapter(arrayList, getContext());
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

        //새로고침
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setInfo();
                // 동글동글 도는거 사라짐
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){

        } else {
            moveTop();
        }
    }

    private void moveTop(){
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.tab1){
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    private void setInfo(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> mainSource = new ArrayList<>();

        arrayList.clear();
        bookmark.clear();
        like.clear();
        mainSource.clear();
        mainSource.add(uid);

        mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    mainSource.add(postSnapshot.getKey());
                }
                db.collection("user-checked/"+uid+"/bookmark")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        bookmark.add(document.getData().get("postID").toString());
                                    }
                                    db.collection("user-checked/"+uid+"/like")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            like.add(document.getData().get("postID").toString());
                                                        }
                                                        db.collection("post")
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                                                                final Data dataList = new Data();
                                                                                for(int j=0; j<mainSource.size(); j++){
                                                                                    if(mainSource.get(j).equals(document.getData().get("uid").toString())){
                                                                                        dataList.setBookmark(false);
                                                                                        for(int i=0; i<bookmark.size(); i++){
                                                                                            if(bookmark.get(i).equals(document.getId())){
                                                                                                dataList.setBookmark(true);
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                        dataList.setLike(false);
                                                                                        for(int i=0; i<like.size(); i++){
                                                                                            if(like.get(i).equals(document.getId())){
                                                                                                dataList.setLike(true);
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                        dataList.setPostID(document.getId());
                                                                                        dataList.setUid(document.getData().get("uid").toString());
                                                                                        dataList.setContent(document.getData().get("content").toString());
                                                                                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                                                                                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                                                                                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                                                                                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                                                                                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                                                                                        dataList.setNickName(document.getData().get("nickName").toString());
                                                                                        dataList.setCategory(document.getData().get("category").toString());
                                                                                        dataList.setDate(document.getData().get("date").toString());
                                                                                        dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
                                                                                        arrayList.add(0, dataList);
                                                                                    }
                                                                                }
                                                                            }
                                                                            adapter.notifyDataSetChanged();
                                                                        } else {
                                                                            Log.d("###", "Error getting documents: ", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                        adapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d("###", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
