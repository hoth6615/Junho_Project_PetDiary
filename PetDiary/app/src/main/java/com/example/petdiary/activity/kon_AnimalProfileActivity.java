package com.example.petdiary.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class kon_AnimalProfileActivity extends AppCompatActivity {

    RelativeLayout loaderLayout;

    ImageView moreBtn;
    ImageView petImg;
    EditText petName;
    EditText petMemo;
    String petId;
    String petMaster;

    String userId; // 뷰에 접근하는 유저 아이디

    Button saveBtn;
    Button cancelBtn;


    String postImgPath; // 보낼 이미지
    String preImage;    // 편집 전 이미지
    String preName;     // 편집 전 이름
    String preMemo;     // 편집 전 메모


    boolean isAddMode = false; // 펫 추가 버튼을 눌렀을시에만 true
    boolean isEditMode = false;
    boolean isPressedSaveBtn = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kon_activity_animal_profile_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        loaderLayout = findViewById(R.id.loaderLayout);

        //데이터 수신
        Intent intent = getIntent();
        isAddMode = intent.getBooleanExtra("isAddMode", false);
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        petId = intent.getStringExtra("petId");
        petMaster = intent.getStringExtra("petMaster");
        userId = intent.getStringExtra("userId");

        preImage = intent.getStringExtra("petImage");
        preName = intent.getStringExtra("name");
        preMemo = intent.getStringExtra("memo");

        moreBtn = findViewById(R.id.animalPage_more);
        petImg = findViewById(R.id.animalPage_Image);
        petName = findViewById(R.id.animalPage_name);
        petMemo = findViewById(R.id.animalPage_memo);
        saveBtn = findViewById(R.id.animalPage_save);
        cancelBtn = findViewById(R.id.animalPage_cancel);


        setProfileImg(preImage);
        petName.setText(preName);
        petMemo.setText(preMemo);
        postImgPath = preImage;


        moreBtn.setOnClickListener(onClickListener);
        petImg.setOnClickListener(onClickListener);
        saveBtn.setOnClickListener(onClickListener);
        cancelBtn.setOnClickListener(onClickListener);


        if (isAddMode == false && isEditMode == false) {
            // 보기 모드
            setEditIcon(true);
            setEditMode(false);

        } else if (isAddMode == false && isEditMode == true) {
            // 수정 모드
            setEditIcon(false);
            setEditMode(true);
        } else if (isAddMode == true) {
            // 추가 모드
            setEditIcon(false);
            setEditMode(true);
        }


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.animalPage_more:
                    if (moreBtn.isClickable()) {
                        final CharSequence[] items = new CharSequence[]{"Edit", "Delete"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(kon_AnimalProfileActivity.this);
                        builder.setTitle("");

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int pos) {
                                String selectedText = items[pos].toString();
                                switch (pos) {
                                    case 0:   // Edit
                                        isEditMode = true;
                                        setEditIcon(false);
                                        setEditMode(true);
                                        break;
                                    case 1:    // Delete
                                        loaderLayout.setVisibility(View.VISIBLE);
                                        deleteData();
                                        break;
                                }

                                // Toast.makeText(MainActivity.this, selectedText, Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.show();
                    }
                    break;
                case R.id.animalPage_Image:
                    if (isEditMode || isAddMode)
                        startGalleryActivity();
                    break;
                case R.id.animalPage_save:
                    isPressedSaveBtn = true;
                    isEditMode = false;

                    loaderLayout.setVisibility(View.VISIBLE);
                    if (isAddMode) {

                        // 추가 모드
                        addDataToFirebase();
                    } else {

                        saveDataToFirebase();

                        setEditIcon(true);
                        setEditMode(false);

                        preName = petName.getText().toString();
                        preMemo = petMemo.getText().toString();
                        preImage = postImgPath;

                    }
                    break;
                case R.id.animalPage_cancel:
                    // isImageEdit = false;
                    isEditMode = false;
                    postImgPath = preImage;
                    setProfileImg(preImage);
                    petName.setText(preName);
                    petMemo.setText(preMemo);
                    setEditIcon(true);
                    setEditMode(false);
                    if (isAddMode) {
                        onBackPressed();
                    }
                    break;


            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: // 갤러리에서 이미지 선택시
                if (resultCode == RESULT_OK) {
                    postImgPath = data.getStringExtra("postImgPath");

                    setProfileImg(postImgPath);
                }
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isPressedSaveBtn) {
            //Intent intent = new Intent();
            //  intent.putExtra("profileImg", postImgPath);
            //   intent.putExtra("aniName", aniName.getText().toString());
            //intent.putExtra("memo", userMemo.getText().toString());

            //setResult(RESULT_OK, intent);
            finish();
        }
        super.onBackPressed();
    }


    // 편집버튼 상태 변경 on/off
    private void setEditIcon(boolean isShown) {
        if (!petMaster.equals(userId)) {
            moreBtn.setVisibility(View.INVISIBLE);
            moreBtn.setClickable(false);
            return;
        }

        if (isShown)
            moreBtn.setVisibility(View.VISIBLE);
        else
            moreBtn.setVisibility(View.INVISIBLE);

        moreBtn.setClickable(isShown);
    }


    // 편집 버튼 상태에 따른 이름, 메모, 저장, 취소버튼 상태 변경
    private void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            //  isEdit = true;
            petName.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorAccent));
            petName.setFocusableInTouchMode(true);

            petMemo.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorAccent));
            petMemo.setFocusableInTouchMode(true);

            saveBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
        } else {
            // isEdit = false;
            petName.setBackground(null);
            petName.setFocusableInTouchMode(false);
            petName.setFocusable(false);

            petMemo.setBackground(null);
            petMemo.setFocusableInTouchMode(false);
            petMemo.setFocusable(false);

            saveBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisibility(View.INVISIBLE);
        }
    }

    // 프로필 이미지 변경 함수
    private void setProfileImg(String profileImg) {
        if (profileImg == null || profileImg.equals(""))
            return;
        Glide.with(this).load(profileImg).centerCrop().override(500).into(petImg);
    }


    // 갤러리 열기 위한 팝업생성 함수
    private void startGalleryActivity() {
        Intent intent = new Intent(getApplicationContext(), ImageChoicePopupActivity.class);
        startActivityForResult(intent, 0);
    }

    // 새 애완동물을 추가할때 사용하는 함수
    private void addDataToFirebase() {
        // pet id에 사용될 데이터 생성
        Date today = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final String petId = formatDate.format(today) + uid;

        // 추가할 애완동물 정보
        Map<String, Object> petData = new HashMap<>();
        petData.put("petName", petName.getText().toString());
        petData.put("petMemo", petMemo.getText().toString());
        petData.put("master", uid);
        petData.put("profileImg", postImgPath);

        ///////////////////////////////////// 텍스트 추가
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("pets").document(petId)
        db.collection("pets").document(uid).collection("pets").document(petId)
                .set(petData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AnimalProfile Add", "DocumentSnapshot successfully written!");
                        if(postImgPath == null || postImgPath.equals("")) {
                            setEditIcon(true);
                            setEditMode(false);
                            loaderLayout.setVisibility(View.INVISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AnimalProfile Add", "Error writing document", e);
                    }
                });

        ///////////////////////////////////// 이미지 추가
        if (postImgPath == null || postImgPath.equals(""))
            return;
        final String[] profileImg = new String[1];

        // 파이어베이스 스토리지에 이미지 저장
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final UploadTask[] uploadTask = new UploadTask[1];

        // 로컬 파일에서 업로드(스토리지)
        Log.d("로그로그로그~~~~", "1번테스트 :" + postImgPath);

        //  if(postImgPath == null||postImgPath.equals("")) {
        //      file =  Uri.parse("https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/pets%2Ficon_dog_running.png?alt=media&token=68e04147-faa1-4443-918e-f01f0cffecd2");

        final Uri file = Uri.fromFile(new File(postImgPath));


        StorageReference riversRef = storageRef.child("pets/" + petId + "_profileImage.jpg");
        uploadTask[0] = riversRef.putFile(file);

        uploadTask[0].addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // 파이어베이스의 스토리지에 저장한 이미지의 다운로드 경로를 가져옴
                final StorageReference ref = storageRef.child("pets/" + petId + "_profileImage.jpg");
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

                            postImgPath = profileImg[0];

                            // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            //  String uid = user.getUid();

                            // 클라우드 파이어스토어의 users에 프로필 이미지 주소 저장
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference documentUserReference = db.collection("pets").document(uid).collection("pets").document(petId);

                            documentUserReference
                                    .update("profileImg", profileImg[0])
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Log.d("ProfileEditActivity", "DocumentSnapshot successfully updated!");
                                            setEditIcon(true);
                                            setEditMode(false);
                                            loaderLayout.setVisibility(View.INVISIBLE);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Log.w("ProfileEditActivity", "Error updating document", e);
                                            loaderLayout.setVisibility(View.INVISIBLE);
                                        }
                                    });

                            //setProfileImg(postImgPath);
                        } else {
                        }
                    }
                });

            }
        });
        // loaderLayout.setVisibility(View.INVISIBLE);

    }

    private void deleteData() {

       // Toast.makeText(getApplicationContext(), "딜리트를 눌렀습니다", Toast.LENGTH_SHORT).show();
        loaderLayout.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        ///////////////////////////////////// Firestore 데이터 삭제
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pets").document(uid).collection("pets").document(petId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(postImgPath == null || postImgPath.equals(""))
                        {
                            loaderLayout.setVisibility(View.INVISIBLE);
                            setEditIcon(false);
                            setEditMode(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("파베 데이터 삭제 실패", "Error deleting document", e);
                        loaderLayout.setVisibility(View.INVISIBLE);
                        setEditIcon(true);
                        setEditMode(false);
                    }
                });




        ///////////////////////////////////// 스토리지, 이미지 삭제
        // Create a storage reference from our app
        if(postImgPath == null || postImgPath.equals(""))
            return;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the file to delete
        StorageReference desertRef = storageRef.child("pets/" + petId + "_profileImage");

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                loaderLayout.setVisibility(View.INVISIBLE);
                setEditIcon(false);
                setEditMode(false);
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                loaderLayout.setVisibility(View.INVISIBLE);
                // Uh-oh, an error occurred!
            }
        });




    }


    // 데이터 수정 시 불리는 함수
    private void saveDataToFirebase() {
        // 텍스트 변경시
        //if (!preMemo.equals(petMemo.getText().toString()) || !preName.equals(petName.getText().toString()))
            setProfileTextToFirebase();

        // 이미지 변경시
       // if (postImgPath.compareTo(preImage) != 0)
        if( !postImgPath.equals(""))
            setProfileImageToFirebase();
    }

    private void setProfileImageToFirebase() {
        // String postImgPath = preImage;
        final String[] profileImg = new String[1];

        // 파이어베이스 스토리지에 이미지 저장
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final UploadTask[] uploadTask = new UploadTask[1];

        // 로컬 파일에서 업로드(스토리지)
        final Uri file = Uri.fromFile(new File(postImgPath));
        StorageReference riversRef = storageRef.child("pets/" + petId + "_profileImage.jpg");
        uploadTask[0] = riversRef.putFile(file);

        uploadTask[0].addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // 파이어베이스의 스토리지에 저장한 이미지의 다운로드 경로를 가져옴
                final StorageReference ref = storageRef.child("pets/" + petId + "_profileImage.jpg");
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

                            postImgPath = profileImg[0];

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();

                            // 클라우드 파이어스토어에 프로필 이미지 주소 저장
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference documentUserReference = db.collection("pets").document(uid).collection("pets").document(petId);

                            documentUserReference
                                    .update("profileImg", profileImg[0])
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("ProfileEditActivity", "DocumentSnapshot successfully updated!");
                                            loaderLayout.setVisibility(View.INVISIBLE);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("ProfileEditActivity", "Error updating document", e);
                                            loaderLayout.setVisibility(View.INVISIBLE);
                                        }
                                    });
                            setProfileImg(postImgPath);
                        } else {
                        }
                    }
                });

            }
        });

    }

    // 닉네임, 메모 저장
    private void setProfileTextToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        //  클라우드 파이어스토어
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date date = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");


        DocumentReference documentUserReference = db.collection("pets").document(uid).collection("pets").document(petId);

        documentUserReference
                .update(
                        "petName", petName.getText().toString(),
                        "petMemo", petMemo.getText().toString()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AnimalProfileActivity", "DocumentSnapshot successfully updated!");
                        if(postImgPath == null || postImgPath.equals(""))
                            loaderLayout.setVisibility(View.INVISIBLE);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AnimalProfileActivity", "Error updating document", e);
                        loaderLayout.setVisibility(View.INVISIBLE);
                    }
                });
    }

}

