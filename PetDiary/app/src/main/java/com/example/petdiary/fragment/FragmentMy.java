package com.example.petdiary.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petdiary.Data;
import com.example.petdiary.Kon_MypageAdapter;
import com.example.petdiary.Kon_Mypage_petAdapter;
import com.example.petdiary.PetData;
import com.example.petdiary.RecyclerDecoration;
import com.example.petdiary.activity.*;
import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;

import static android.app.Activity.RESULT_OK;


public class FragmentMy extends Fragment {

    private static final String TAG = "MyPage_Fragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private ViewGroup viewGroup;

    TextView profileName;
    TextView profileMemo;
    String profileImgName;
    ImageView profileEditImg;


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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewGroup = (ViewGroup) inflater.inflate(R.layout.kon_fragment_mypage, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) viewGroup.findViewById(R.id.swipe_layout);
        profileEditImg = viewGroup.findViewById(R.id.profile_image);
        profileName = viewGroup.findViewById(R.id.profile_name);
        profileMemo = viewGroup.findViewById(R.id.profile_memo);

        ImageView petAddBtn = viewGroup.findViewById(R.id.profile_petAddBtn);
        final ImageView profileImage = viewGroup.findViewById(R.id.profile_image);


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

        //////////////////////////////////// 프로필 이미지 수정 이벤트 추가
        profileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  setImg();
                setProfileImg(profileImgName);
                String userId = "IAmTarget";//"IAmUser"
                String targetId = "IAmTarget";

                Intent intent = new Intent(getContext(), ProfileEditActivity.class);
                intent.putExtra("targetId", targetId);
                intent.putExtra("userId", userId);
                intent.putExtra("userImage", profileImgName);// userInfo.get("profileImg")); // 임시로 넣은 이미지
                intent.putExtra("userName", profileName.getText().toString());// userInfo.get("nickName"));//userName.getText().toString());
                intent.putExtra("userMemo", profileMemo.getText().toString());//userInfo.get("memo"));//userMemo.getText().toString());

                startActivityForResult(intent, 0);
                return true;
            }


        });

        //////////////////////////////////// 모두 보기 버튼
        //TextView allBtn = viewGroup.findViewById(R.id.profile_allBtn);
//        allBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 펫 리사이클러뷰 데이터 변경 해주는 코드 넣기
//            }
//        });


        // 펫 추가 버튼
        petAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), kon_AnimalProfileActivity.class);
                intent.putExtra("isAddMode", true);
                intent.putExtra("isEditMode", false);
                intent.putExtra("petId","");
                intent.putExtra("petMaster","");
                intent.putExtra("userId","");
                intent.putExtra("petImage","");

                //startActivity(intent);
                startActivityForResult(intent, 1);
            }
        });

        // 새로고침시 업데이트
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                loadPostsAfterCheck(false);
                mSwipeRefreshLayout.setRefreshing(false);  // 로딩 애니메이션 사라짐
            }
        });

        //////////////////////////////////// 최상단으로 가기 이벤트 추가
        moveTop();

        return viewGroup;
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 0:  //프로필 수정 // ProfileEditActivity에서 받아온 값
                if (resultCode == RESULT_OK) {
                    setProfileImg(data.getStringExtra("profileImg"));
                    profileName.setText(data.getStringExtra("nickName"));
                    profileMemo.setText(data.getStringExtra("memo"));
                    ImageView hambugerProfileImg = getActivity().findViewById(R.id.genter_icon);
                    Glide.with(this).load(data.getStringExtra("profileImg")).centerCrop().override(500).into(hambugerProfileImg);

                } else {
                }
                break;
            case 1: // 펫 추가, 수정
                if (resultCode == RESULT_OK) {
                    getPetInfo();
                }
                break;

        }
    }

    //////////////////////////////////// 프로필 이미지, 닉네임, 메모 가져오기,
    private void getUserInfo() {
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

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
        Log.d("로그로그로그~~~~", "펫 정보 로드 시작" );
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

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

                        Log.d("로그로그로그~~~~", "바뀌기전: ");
                        petAdapter.notifyDataSetChanged();
                        Log.d("로그로그로그~~~~", "바뀐후: ");
                    }
                });

    }


    //////////////////////////////////// 개인 게시물 로드. 체크하게 되면 이전 게시물 개수와 비교후 업데이트,
    //////////////////////////////////// 체크 안하면 그냥 업데이트
    private void loadPostsAfterCheck(final boolean needCheck) {
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
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
        //  유저
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
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
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(profileEditImg);
    }

    //////////////////////////////////// 최상단으로 가기 이벤트 추가
    private void moveTop() {
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.tab4) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    //////////////////////////////////// 사진 리사이클러뷰 setting
    private void setPicRecyclerView() {
        recyclerView = (RecyclerView) viewGroup.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화

        int columnNum = 3;
        adapter = new Kon_MypageAdapter(postList, columnNum, getContext());
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
        layoutManager = new GridLayoutManager(getContext(), columnNum);
        recyclerView.setLayoutManager(layoutManager);

        // 리사이클러뷰 간격추가
        RecyclerDecoration spaceDecoration = new RecyclerDecoration(10);
        recyclerView.addItemDecoration(spaceDecoration);
    }

    //////////////////////////////////// 펫 리사이클러뷰 setting
    private void setPetRecyclerView() {
        petRecyclerView = (RecyclerView) viewGroup.findViewById(R.id.pet_recyclerView);
        petRecyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화

        int columnNum = 3;
        petAdapter = new Kon_Mypage_petAdapter(petList, getContext(), getActivity(), new StringCallback() {
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
        getPetInfo();

    }


}