package com.example.petdiary.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petdiary.Data;
import com.example.petdiary.R;
import com.example.petdiary.adapter.CustomAdapter;
import com.example.petdiary.info.PostInfo;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentMain2 extends Fragment {

    private static final String TAG = "MainFragment";

    private RecyclerView recyclerView;
    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    BottomNavigationView bottomNavigationView;

    private FirebaseFirestore firebaseFirestore;
    private CustomAdapter homeAdapter;
    private ArrayList<Data> postList;
    private boolean updating;
    private boolean topScrolled;

    private ArrayList<String> bookmark = new ArrayList<String>();
    private ArrayList<String> like = new ArrayList<String>();

    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);

        firebaseFirestore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        homeAdapter = new CustomAdapter(postList, getContext());

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(homeAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int firstVisibleItemPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();

                if(newState == 1 && firstVisibleItemPosition == 0){
                    topScrolled = true;
                }
                if(newState == 0 && topScrolled){
                    try {
                        postsUpdate(true);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    topScrolled = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                int lastVisibleItemPosition = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();

                if(totalItemCount - 3 <= lastVisibleItemPosition && !updating){
                    try {
                        postsUpdate(false);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if(0 < firstVisibleItemPosition){
                    topScrolled = false;
                }
            }
        });

        try {
            postsUpdate(false);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                homeAdapter.clear();
                try {
                    postsUpdate(false);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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

    private void postsUpdate(final boolean clear) throws ParseException {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> mainSource = new ArrayList<>();

        bookmark.clear();
        like.clear();
        mainSource.clear();
        mainSource.add(uid);

        updating = true;

        long now = System.currentTimeMillis();
        Date nowdate = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        final String currentDate = sdfNow.format(nowdate);

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
                                                        String date = postList.size() == 0 || clear ? currentDate : postList.get(postList.size() - 1).getDate();
                                                        CollectionReference collectionReference = firebaseFirestore.collection("post");
                                                        collectionReference.orderBy("date", Query.Direction.DESCENDING).whereLessThan("date", date).limit(10).get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            if(clear){
                                                                                postList.clear();
                                                                            }
                                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                for(int i=0; i<mainSource.size(); i++){
                                                                                    Log.e("###main2", mainSource.get(i) + " /// " + document.getData().get("uid").toString());
                                                                                    if(mainSource.get(i).equals(document.getData().get("uid").toString())){
                                                                                        postList.add(new Data(
                                                                                                document.getId(), document.getData().get("imageUrl1").toString(), document.getData().get("imageUrl2").toString(), document.getData().get("imageUrl3").toString(),
                                                                                                document.getData().get("imageUrl4").toString(), document.getData().get("imageUrl5").toString(), document.getData().get("content").toString(),
                                                                                                document.getData().get("nickName").toString(), document.getData().get("uid").toString(), document.getData().get("category").toString(),
                                                                                                document.getData().get("date").toString(), document.getData().get("email").toString()));
                                                                                        break;
                                                                                    }
                                                                                }
                                                                            }
                                                                            homeAdapter.notifyDataSetChanged();
                                                                        } else {
                                                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                                                        }
                                                                        updating = false;
                                                                    }
                                                                });
                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
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
