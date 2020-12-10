package com.example.petdiary.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petdiary.info.BlockFriendInfo;
import com.example.petdiary.adapter.CustomAdapterSub;
import com.example.petdiary.Data;
import com.example.petdiary.ItemTouchHelperCallback;
import com.example.petdiary.R;
import com.example.petdiary.RecyclerViewDecoration;
import com.example.petdiary.adapter.SearchUserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FragmentSub extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Data> arrayList;
    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;
    BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sub, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);

        moveTop();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화

        layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new RecyclerViewDecoration(5));
        //recyclerView.addItemDecoration(new RecyclerDecorationWidth(30));

        searchView = view.findViewById(R.id.search);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override // 검색 버튼 눌렀을 때
            public boolean onQueryTextSubmit(String s) {
                if(s.charAt(0) == '#'){
                    layoutManager = new GridLayoutManager(getContext(), 3);
                    recyclerView.setLayoutManager(layoutManager);
                    setInfo(s);
                } else {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                    setSearch(s);
                    //recyclerView.addItemDecoration(new RecyclerViewDecoration(1));
                }

                return false;
            }
            @Override // 글자 변경시
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        arrayList = new ArrayList<>(); // User 객체를 담을 어레이 리스트 (어댑터쪽으로)

        setInfo();

        //새로고침
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(searchView.getQuery().length() > 0){
                    if(searchView.getQuery().charAt(0) == '#'){
                        layoutManager = new GridLayoutManager(getContext(), 3);
                        recyclerView.setLayoutManager(layoutManager);
                        setInfo(searchView.getQuery()+"");
                    } else {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        setSearch(searchView.getQuery()+"");
                        //recyclerView.addItemDecoration(new RecyclerViewDecoration(1));
                    }
                } else {
                    layoutManager = new GridLayoutManager(getContext(), 3);
                    recyclerView.setLayoutManager(layoutManager);
                    setInfo();
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
                if(menuItem.getItemId() == R.id.tab2){
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    private void setInfo(){
        arrayList.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> block = new ArrayList<>();
        db.collection("blockFriends/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                block.add(document.getData().get("friendUid").toString());
                            }
                            db.collection("post")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    int chk = 0;
                                                    for(int i=0; i<block.size(); i++){
                                                        if(block.get(i).equals(document.getData().get("uid").toString())){
                                                            chk++;
                                                            break;
                                                        }
                                                    }
                                                    if(chk == 0){
                                                        Data dataList = new Data();
                                                        dataList.setPostID(document.getId());
                                                        dataList.setUid(document.getData().get("uid").toString());
                                                        dataList.setContent(document.getData().get("content").toString());
                                                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                                                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                                                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                                                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                                                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                                                        dataList.setNickName(document.getData().get("nickName").toString());
                                                        //dataList.setDate(document.getData().get("date").toString());
                                                        dataList.setCategory(document.getData().get("category").toString());
                                                        dataList.setEmail(document.getData().get("email").toString());
                                                        dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
                                                        arrayList.add(0, dataList);
                                                    }
                                                }
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Log.d("###", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                            adapter = new CustomAdapterSub(arrayList, getContext());
                            recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
                        } else {
                            Log.d("###", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setInfo(final String s){
        arrayList.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> block = new ArrayList<>();
        block.clear();
        db.collection("blockFriends/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                block.add(document.getData().get("friendUid").toString());
                            }
                            db.collection("post")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    int chk = 0;
                                                    for(int i=0; i<block.size(); i++){
                                                        if(block.get(i).equals(document.getData().get("uid").toString())){
                                                            chk++;
                                                            break;
                                                        }
                                                    }
                                                    if(chk == 0){
                                                        String temp = document.getData().get("hashTag").toString();
                                                        if(temp.length() > 0){
                                                            if(temp.contains(s)){
                                                                Data dataList = new Data();
                                                                dataList.setPostID(document.getId());
                                                                dataList.setUid(document.getData().get("uid").toString());
                                                                dataList.setContent(document.getData().get("content").toString());
                                                                dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                                                                dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                                                                dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                                                                dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                                                                dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                                                                dataList.setNickName(document.getData().get("nickName").toString());
                                                                dataList.setDate(document.getData().get("date").toString());
                                                                dataList.setCategory(document.getData().get("category").toString());
                                                                dataList.setEmail(document.getData().get("email").toString());
                                                                dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
                                                                arrayList.add(0, dataList);
                                                            }
                                                        }
                                                    }
                                                }
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Log.d("###", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                            adapter = new CustomAdapterSub(arrayList, getContext());
                            recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
                        } else {
                            Log.d("###", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void setSearch(final String s){
        arrayList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            SearchUserAdapter adapter = new SearchUserAdapter(getContext());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("nickName").toString().contains(s)){
                                    String friendUid = document.getId();
                                    adapter.addItem(new BlockFriendInfo(friendUid));
                                }
                            }
                            recyclerView.setAdapter(adapter);
                            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                            helper.attachToRecyclerView(recyclerView);
                        } else {
                            Log.d("FragmentSub", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}