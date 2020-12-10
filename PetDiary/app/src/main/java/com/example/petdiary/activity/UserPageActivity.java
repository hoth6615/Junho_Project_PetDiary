package com.example.petdiary.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.petdiary.Data;
import com.example.petdiary.Kon_MypageAdapter;
import com.example.petdiary.Kon_Mypage_petAdapter;
import com.example.petdiary.PetData;
import com.example.petdiary.R;
import com.example.petdiary.RecyclerDecoration;
import com.example.petdiary.fragment.FragmentMy;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UserPageActivity extends AppCompatActivity {

    private static final String TAG = "UserPage";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView profileName;
    private TextView profileMemo;
    private String profileImgName;
    private ImageView profileEditImg;
    private Button addFriend;

    private String uid;
    private boolean checkFriend = false;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;

    Map<String, String> userInfo = new HashMap<>();   // 이거 여기서 선언할게 아니라 받아와야함
    //Map<String, String> petInfo = new HashMap<>();
    ArrayList<Data> postList = new ArrayList<Data>();
    ArrayList<Data> selectedPostList = new ArrayList<Data>();
    ArrayList<PetData> petList = new ArrayList<PetData>();
    int listCount = 0;


    // 사진 리사이클뷰 선언
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;


    // 펫 정보 리사이클뷰 선언
    RecyclerView petRecyclerView;
    RecyclerView.Adapter petAdapter;
    String choicePetId;

    public interface StringCallback {
        void callback(String choice);

    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("userID");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        profileEditImg = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileMemo = findViewById(R.id.profile_memo);
        addFriend = findViewById(R.id.addFriend);

        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(uid)){
            addFriend.setVisibility(View.GONE);
        } else {
            mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if(uid.equals(postSnapshot.getKey())){
                            checkFriend = true;
                            addFriend.setText("친구 삭제");
                            break;
                        }
                    }
                    addFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            firebaseDatabase = FirebaseDatabase.getInstance();
                            if(checkFriend){
                                DatabaseReference friend = firebaseDatabase.getReference("friend").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+uid);
                                FriendInfo friendInfo = new FriendInfo();
                                friend.setValue(friendInfo);
                                checkFriend = false;
                                addFriend.setText("친구 추가");
                            } else {
                                DatabaseReference friend = firebaseDatabase.getReference("friend").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+uid);
                                Hashtable<String, String> numbers = new Hashtable<String, String>();
                                numbers.put("message","없음");
                                friend.setValue(numbers);
                                checkFriend = true;
                                addFriend.setText("친구 삭제");
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }


        final ImageView profileImage = findViewById(R.id.profile_image);

        //////////////////////////////////// 유저 정보 가져오기
        getUserInfo();

        //////////////////////////////////// 애완동물 정보 가져오기
        getPetInfo();

        //////////////////////////////////// 게시물 정보 가져오기
        loadPostsAfterCheck(false);

        //////////////////////////////////// 애완동물 리사이클러뷰 setting
        setPetRecyclerView();

        //////////////////////////////////// 사진 리사이클러뷰 setting
        setPicRecyclerView();

        //////////////////////////////////// 모두 보기 버튼
        //TextView allBtn = viewGroup.findViewById(R.id.profile_allBtn);
//        allBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 펫 리사이클러뷰 데이터 변경 해주는 코드 넣기
//            }
//        });

        // 새로고침시 업데이트
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                loadPostsAfterCheck(false);
                mSwipeRefreshLayout.setRefreshing(false);  // 로딩 애니메이션 사라짐
            }
        });

    }

    //////////////////////////////////// 프로필 이미지, 닉네임, 메모 가져오기,
    private void getUserInfo() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userInfo.put("nickName", document.getString("nickName"));
                    userInfo.put("profileImg", document.getString("profileImg"));
                    userInfo.put("memo", document.getString("memo"));

                    profileName.setText(userInfo.get(("nickName")));
                    profileMemo.setText(userInfo.get(("memo")));
                    profileImgName = document.getString("profileImg");
                    if(profileImgName.length() > 0){
                        setProfileImg(profileImgName);
                    }
                    //setImg();
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }


    //////////////////////////////////// 펫 정보 로드
    private void getPetInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pets").document(uid).collection("pets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            petList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String, Object> data = document.getData();
                                // 이름 이미지 메모
                                PetData pet = new PetData(
                                        document.getId(),
                                        data.get("petName").toString(),
                                        data.get("profileImg").toString(),
                                        data.get("petMemo").toString(),
                                        data.get("master").toString());
                                petList.add(pet);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        petAdapter.notifyDataSetChanged();
                    }
                });

    }


    //////////////////////////////////// 개인 게시물 로드. 체크하게 되면 이전 게시물 개수와 비교후 업데이트,
    //////////////////////////////////// 체크 안하면 그냥 업데이트
    private void loadPostsAfterCheck(final boolean needCheck) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        postList.clear();

        Query query = db.collection("post").whereEqualTo("uid", uid);
        //query.get
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    if (needCheck)
                        if (listCount == resultCount)
                            return;

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Data dataList = new Data();
                        dataList.setUid(document.getData().get("uid").toString());
                        dataList.setContent(document.getData().get("content").toString());
                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                        dataList.setNickName(document.getData().get("nickName").toString());
                        postList.add(0, dataList);
                    }
                    adapter.notifyDataSetChanged();
                    listCount = resultCount;

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }


    ////////////////////////////////////  특정 펫 게시물 로드
    private void loadSelectedPosts(String petId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection("post").whereEqualTo("petsID", petId);
        //query.get
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    postList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Data dataList = new Data();
                        dataList.setUid(document.getData().get("uid").toString());
                        dataList.setContent(document.getData().get("content").toString());
                        dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                        dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                        dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                        dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                        dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                        dataList.setNickName(document.getData().get("nickName").toString());
                        postList.add(0, dataList);
                    }
                    adapter.notifyDataSetChanged();
                    //listCount = resultCount;

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void startToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(profileEditImg);
    }

    //////////////////////////////////// 사진 리사이클러뷰 setting
    private void setPicRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화

        int columnNum = 3;
        adapter = new Kon_MypageAdapter(postList, columnNum, getApplicationContext());
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
        layoutManager = new GridLayoutManager(getApplicationContext(), columnNum);
        recyclerView.setLayoutManager(layoutManager);

        // 리사이클러뷰 간격추가
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(10);
        recyclerView.addItemDecoration(spaceDecoration);
    }

    //////////////////////////////////// 펫 리사이클러뷰 setting
    private void setPetRecyclerView() {
        petRecyclerView = (RecyclerView) findViewById(R.id.pet_recyclerView);
        petRecyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화

        int columnNum = 3;
        petAdapter = new Kon_Mypage_petAdapter(petList, getApplicationContext(), this, new FragmentMy.StringCallback() {
            @Override
            public void callback(String choice) {
                choicePetId = choice;
                if (choice.equals(""))
                    loadPostsAfterCheck(false);
                else
                    loadSelectedPosts(choicePetId);
            }
        });
        petRecyclerView.setAdapter(petAdapter); // 리사이클러뷰에 어댑터 연결
        //layoutManager = new GridLayoutManager(getContext(), columnNum);
        //petRecyclerView.setLayoutManager(layoutManager);

        // 리사이클러뷰 간격추가
        //RecyclerDecoration spaceDecoration = new RecyclerDecoration(10);
        // petRecyclerView.addItemDecoration(spaceDecoration);
    }


    //////////////////////////////////// 화면 처음 활성화 됐을 시 행동
    @Override
    public void onResume() {
        super.onResume();
        loadPostsAfterCheck(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}