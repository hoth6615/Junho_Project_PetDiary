package com.example.petdiary.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.activity.ProfileEditActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FragmentMy2 extends Fragment {

    FirebaseUser user;

    private static final String TAG = "MyPage_Fragment";
    String profileImg;   // 프로필이 로딩되면 null값이 아니게 됨


    ImageView profileEditImg;

    //    TextView emailTextView;
//    TextView nickNameTextView;
//    TextView toolbarNickName;
//    ImageView user_profileImage_ImageView;
    ViewGroup viewGroup;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.kon_fragment_mypage, container, false);

        profileEditImg = viewGroup.findViewById(R.id.profile_image);
        final TextView profileName = viewGroup.findViewById(R.id.profile_name);
        final TextView profileMemo = viewGroup.findViewById(R.id.profile_memo);

        Bundle bundle = getArguments();

        if(bundle != null){
            profileName.setText(bundle.getString("nickName"));
            setProfileImg(bundle.getString("profile"));
        } else {
            //  유저
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document();
            DocumentReference documentUserReference = db.collection("users").document(uid);

            if (user == null) {
                //myStartActivity(SignUpActivity.class);
            } else {

            }

            // 프로필 이미지, 닉네임, 메모 가져오기,
            documentUserReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                setImg();
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                //emailTextView.setText(document.getData().get("email").toString());
                                //nickNameTextView.setText(document.getData().get("nickName").toString());
                                profileName.setText(document.getData().get("nickName").toString());
                                profileMemo.setText(document.getData().get("memo").toString());

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                if (document.getData().get("users").toString().length() > 0) {
                                    //   setImg();
                                }
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

//                            emailTextView.setText(document.getData().get("email").toString());
//                            nickNameTextView.setText(document.getData().get("nickName").toString());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }

                }
            });
        }

//        viewGroup.findViewById(R.id.memberInfoInitButton).setOnClickListener(onClickListener);
//        viewGroup.findViewById(R.id.setProfileImg).setOnClickListener(onClickListener);
//
//        emailTextView = viewGroup.findViewById(R.id.emailEditText);
//        nickNameTextView = viewGroup.findViewById(R.id.nickNameEditText);
//        toolbarNickName = viewGroup.findViewById(R.id.toolbar_nickName);
//
//        user_profileImage_ImageView = viewGroup.findViewById(R.id.user_profileImage_ImageView);
//

        ////////////// irang Start
//        GridView gridView = viewGroup.findViewById(R.id.gridView);
//        GridListAdapter adapter = new GridListAdapter();
//        gridView.setAdapter(adapter);
//
//        for (int i = 0; i < imgs.length; ++i) {
//            adapter.addItem(new MypageGridItem(imgs[i]));
//        }

        ImageView addPetBtn = viewGroup.findViewById(R.id.profile_petAddBtn);

        // 메모가져오는 연습
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("users").document();


//
//
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getContext(), position + "번째 아이템이 클릭되었다!!", Toast.LENGTH_SHORT).show();
//            }
//        });


        // 프로필뷰 관련코드.
        ImageView petAddBtn = viewGroup.findViewById(R.id.profile_petAddBtn);
        final ImageView profileImage = viewGroup.findViewById(R.id.profile_image);
        final TextView userName = viewGroup.findViewById(R.id.profile_name);
        final TextView userMemo = viewGroup.findViewById(R.id.profile_memo);


        // 프로필 이미지 수정
        profileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                setImg();
                String userId = "IAmTarget";//"IAmUser";
                String targetId = "IAmTarget";
                Intent intent = new Intent(getContext(), ProfileEditActivity.class);
                intent.putExtra("targetId", targetId);
                intent.putExtra("userId", userId);

                intent.putExtra("userImage", profileImg); // 임시로 넣은 이미지
                intent.putExtra("userName", userName.getText().toString());
                intent.putExtra("userMemo", userMemo.getText().toString());


                //    startActivity(intent);
                startActivityForResult(intent, 0);
                return true;
            }
        });


        // 펫 추가 버튼
        petAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "이제 당신이 해야 할 일은 버튼 기능을 추가하는 것 입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return viewGroup;
    }

    private void startToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(getContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    String postImgPath = data.getStringExtra("postImgPath");
                    Log.d("IR", "postImgPath: " + postImgPath);
                    if (postImgPath == null)
                        break;
                    this.profileImg = postImgPath;
                    final String[] profileImg = new String[1];

                    // 파이어베이스 스토리지에 이미지 저장
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    final StorageReference storageRef = storage.getReference();
                    final UploadTask[] uploadTask = new UploadTask[1];

                    final Uri file = Uri.fromFile(new File(postImgPath));
                    StorageReference riversRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
                    uploadTask[0] = riversRef.putFile(file);

                    uploadTask[0].addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // 파이어베이스의 스토리지에 저장한 이미지의 다운로드 경로를 가져옴
                            final StorageReference ref = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
                            uploadTask[0] = ref.putFile(file);

                            Task<Uri> urlTask = uploadTask[0].continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        profileImg[0] = downloadUri.toString();

                                        // 클라우드 파이어스토어의 users에 프로필 이미지 주소 저장
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference washingtonRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        washingtonRef
                                                .update("profileImg", profileImg[0])
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error updating document", e);
                                                    }
                                                });
                                    } else {
                                    }
                                }
                            });

                        }
                    });

                    setProfileImg(postImgPath);
                } else {
                }
                break;
        }
    }


    private void setImg() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                profileImg = uri.toString();
//                while(profileImg.length() == 0){
//                    continue;
//                }
                //Log.e("@@@!", profileImg);
                setProfileImg(profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(profileEditImg);

    }


    //irang temp -------------------------------------------------------

    public class MypageGridItem {
        int _imgName;

        public MypageGridItem(int imgName) {
            _imgName = imgName;
            Log.d("test", "imgName :  " + imgName);
        }
    }


    class GridListAdapter extends BaseAdapter {
        Context context;
        ArrayList<MypageGridItem> items = new ArrayList<MypageGridItem>();

        public void addItem(MypageGridItem item) {
            items.add(item);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            MypageGridItem listItem = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.kon_mypage_item, parent, false);
            }

            //xml의 ImageView 참조
            ImageView image = convertView.findViewById(R.id.mypage_image);

            image.setImageResource(listItem._imgName);

            return convertView;

        }
    }
//
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 0:
//                if (resultCode == RESULT_OK) {
//                    String postImgPath = data.getStringExtra("postImgPath");
//                    final String[] profileImg = new String[1];
//
//                    // 파이어베이스 스토리지에 이미지 저장
//                    FirebaseStorage storage = FirebaseStorage.getInstance();
//                    final StorageReference storageRef = storage.getReference();
//                    final UploadTask[] uploadTask = new UploadTask[1];
//
//                    final Uri file = Uri.fromFile(new File(postImgPath));
//                    StorageReference riversRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
//                    uploadTask[0] = riversRef.putFile(file);
//
//                    uploadTask[0].addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                        }
//                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                            // 파이어베이스의 스토리지에 저장한 이미지의 다운로드 경로를 가져옴
//                            final StorageReference ref = storageRef.child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg");
//                            uploadTask[0] = ref.putFile(file);
//
//                            Task<Uri> urlTask = uploadTask[0].continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                                @Override
//                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                                    if (!task.isSuccessful()) {
//                                        throw task.getException();
//                                    }
//                                    return ref.getDownloadUrl();
//                                }
//                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Uri> task) {
//                                    if (task.isSuccessful()) {
//                                        Uri downloadUri = task.getResult();
//                                        profileImg[0] = downloadUri.toString();
//
//                                        // 클라우드 파이어스토어의 users에 프로필 이미지 주소 저장
//                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
//                                        DocumentReference washingtonRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                        washingtonRef
//                                                .update("profileImg", profileImg[0])
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.w(TAG, "Error updating document", e);
//                                                    }
//                                                });
//                                    } else {
//                                    }
//                                }
//                            });
//
//                        }
//                    });
//
//                    setProfileImg(postImgPath);
//                } else {
//                }
//                break;
//        }
//    }

}